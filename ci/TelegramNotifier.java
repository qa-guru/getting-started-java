import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Sends a test run summary to Telegram. Run as a single-file program (JDK 11+, no dependencies):
 * <pre>java ci/TelegramNotifier.java</pre>
 *
 * All input comes from environment variables:
 * <ul>
 *   <li>TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID — secrets; if either is missing the notification is skipped (exit 0)</li>
 *   <li>BUILD_OUTCOME — outcome of compilation ("success" means tests could run)</li>
 *   <li>TESTS_OUTCOME — outcome of the test step (optional)</li>
 *   <li>JUNIT_RESULTS_DIR — directory with JUnit XML (default build/test-results/test)</li>
 *   <li>CI_NAME, PROJECT_NAME, BRANCH, COMMIT_SHA, RUN_URL — message details</li>
 *   <li>TELEGRAM_API_URL (default https://api.telegram.org), TELEGRAM_TIMEOUT_SECONDS (default 15)</li>
 * </ul>
 * Exit codes: 0 — sent or skipped (no secrets); 1 — sending failed. The token and chat id are never printed.
 */
public class TelegramNotifier {

    enum Status {
        PASSED("✅ PASSED"),
        FAILED("❌ FAILED"),
        BUILD_ERROR("🛑 BUILD ERROR — tests did not run"),
        NO_RESULTS("⚠️ NO TEST RESULTS");

        final String label;

        Status(String label) {
            this.label = label;
        }
    }

    record Stats(int total, int passed, int failed, int errors, int skipped) {
    }

    record Result(Status status, Stats stats, String note) {
    }

    private static final Pattern OK_TRUE = Pattern.compile("\"ok\"\\s*:\\s*true");
    private static final Pattern DESCRIPTION = Pattern.compile("\"description\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");

    public static void main(String[] args) {
        System.exit(run(System.getenv(), System.out));
    }

    static int run(Map<String, String> env, PrintStream out) {
        boolean github = "true".equals(env.get("GITHUB_ACTIONS"));
        String token = blankToNull(env.get("TELEGRAM_BOT_TOKEN"));
        String chatId = blankToNull(env.get("TELEGRAM_CHAT_ID"));

        Result result = evaluate(env);
        String message = formatMessage(env, result);
        out.println("Notification text:");
        out.println(message);

        if (token == null || chatId == null) {
            warn(out, github, "Telegram notification skipped: TELEGRAM_BOT_TOKEN and/or TELEGRAM_CHAT_ID are not set");
            return 0;
        }

        try {
            String error = send(env, token, chatId, message);
            if (error == null) {
                out.println("Telegram notification sent");
                return 0;
            }
            warn(out, github, "Telegram notification failed: " + sanitize(error, token, chatId));
        } catch (Exception e) {
            // Exception messages may contain the request URL with the token — always sanitize.
            warn(out, github, "Telegram notification failed: " + e.getClass().getSimpleName()
                    + (e.getMessage() == null ? "" : ": " + sanitize(e.getMessage(), token, chatId)));
        }
        return 1;
    }

    static Result evaluate(Map<String, String> env) {
        String buildOutcome = env.getOrDefault("BUILD_OUTCOME", "");
        if (!"success".equals(buildOutcome)) {
            String detail = buildOutcome.isBlank() ? "unknown" : buildOutcome;
            return new Result(Status.BUILD_ERROR, null, "Compilation step: " + detail);
        }

        Path dir = Path.of(env.getOrDefault("JUNIT_RESULTS_DIR", "build/test-results/test"));
        List<Path> files;
        try {
            files = junitFiles(dir);
        } catch (IOException e) {
            return new Result(Status.NO_RESULTS, null, "Cannot read " + dir);
        }
        if (files.isEmpty()) {
            return new Result(Status.NO_RESULTS, null, "No JUnit XML found in " + dir);
        }

        Stats stats;
        try {
            stats = countTests(files);
        } catch (Exception e) {
            return new Result(Status.NO_RESULTS, null, "Unreadable JUnit XML: " + e.getMessage());
        }

        if (stats.total() == 0) {
            return new Result(Status.NO_RESULTS, stats, "0 tests executed");
        }
        if (stats.failed() + stats.errors() > 0) {
            return new Result(Status.FAILED, stats, null);
        }
        String testsOutcome = env.getOrDefault("TESTS_OUTCOME", "");
        if (!testsOutcome.isBlank() && !"success".equals(testsOutcome)) {
            // XML looks green but the test step failed (e.g. a crashed fork) — do not report success
            return new Result(Status.FAILED, stats, "Test step outcome: " + testsOutcome);
        }
        if (stats.passed() == 0) {
            return new Result(Status.NO_RESULTS, stats, "No tests passed (all skipped)");
        }
        return new Result(Status.PASSED, stats, null);
    }

    static List<Path> junitFiles(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) {
            return List.of();
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.startsWith("TEST-") && name.endsWith(".xml");
                    })
                    .sorted()
                    .toList();
        }
    }

    /** Counts &lt;testcase&gt; elements (not testsuite attributes), so nested testsuites are never double-counted. */
    static Stats countTests(List<Path> files) throws Exception {
        DocumentBuilder builder = secureFactory().newDocumentBuilder();
        // DefaultHandler: rethrows fatal errors, prints nothing (the JDK default writes "[Fatal Error]" to stderr)
        builder.setErrorHandler(new org.xml.sax.helpers.DefaultHandler());
        int total = 0, failed = 0, errors = 0, skipped = 0;
        for (Path file : files) {
            Document doc;
            try {
                doc = builder.parse(file.toFile());
            } catch (Exception e) {
                throw new Exception(file.getFileName().toString(), e);
            }
            NodeList cases = doc.getElementsByTagName("testcase");
            for (int i = 0; i < cases.getLength(); i++) {
                Element testCase = (Element) cases.item(i);
                total++;
                if (hasChild(testCase, "failure")) {
                    failed++;
                } else if (hasChild(testCase, "error")) {
                    errors++;
                } else if (hasChild(testCase, "skipped")) {
                    skipped++;
                }
            }
        }
        return new Stats(total, total - failed - errors - skipped, failed, errors, skipped);
    }

    private static boolean hasChild(Element element, String name) {
        for (Node n = element.getFirstChild(); n != null; n = n.getNextSibling()) {
            if (n.getNodeType() == Node.ELEMENT_NODE && name.equals(n.getNodeName())) {
                return true;
            }
        }
        return false;
    }

    private static DocumentBuilderFactory secureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }

    static String formatMessage(Map<String, String> env, Result result) {
        List<String> lines = new ArrayList<>();
        lines.add(result.status().label + " — " + env.getOrDefault("PROJECT_NAME", "unknown project"));
        lines.add("CI: " + env.getOrDefault("CI_NAME", "unknown"));
        String sha = env.getOrDefault("COMMIT_SHA", "");
        lines.add("Branch: " + env.getOrDefault("BRANCH", "unknown")
                + (sha.isBlank() ? "" : " @ " + sha.substring(0, Math.min(7, sha.length()))));
        Stats s = result.stats();
        if (s != null) {
            lines.add("Tests: " + s.total() + " total · " + s.passed() + " passed · "
                    + (s.failed() + s.errors()) + " failed"
                    + (s.errors() > 0 ? " (" + s.errors() + " errors)" : "")
                    + " · " + s.skipped() + " skipped");
        } else {
            lines.add("Tests: not run");
        }
        if (result.note() != null) {
            lines.add("Note: " + result.note());
        }
        String runUrl = env.getOrDefault("RUN_URL", "");
        if (!runUrl.isBlank()) {
            lines.add("Run: " + runUrl);
        }
        return String.join("\n", lines);
    }

    /** Returns null on success, otherwise an error description (never containing the token). */
    static String send(Map<String, String> env, String token, String chatId, String text)
            throws IOException, InterruptedException {
        String apiUrl = env.getOrDefault("TELEGRAM_API_URL", "https://api.telegram.org");
        long timeoutSeconds = Long.parseLong(env.getOrDefault("TELEGRAM_TIMEOUT_SECONDS", "15"));

        String body = "chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                + "&disable_web_page_preview=true";
        HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl + "/bot" + token + "/sendMessage"))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(Math.min(10, timeoutSeconds)))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        int code = response.statusCode();
        String responseBody = response.body() == null ? "" : response.body();
        boolean ok = OK_TRUE.matcher(responseBody).find();
        if (code / 100 == 2 && ok) {
            return null;
        }
        var description = DESCRIPTION.matcher(responseBody);
        return "HTTP " + code + ", ok=" + ok
                + (description.find() ? ", description: " + description.group(1) : "");
    }

    static String sanitize(String text, String... secrets) {
        String result = text;
        for (String secret : secrets) {
            if (secret != null && !secret.isEmpty()) {
                result = result.replace(secret, "***");
            }
        }
        return result;
    }

    private static void warn(PrintStream out, boolean github, String message) {
        out.println((github ? "::warning::" : "WARNING: ") + message);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
