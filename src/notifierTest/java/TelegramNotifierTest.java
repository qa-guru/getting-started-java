import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramNotifierTest {

    private static final String TOKEN = "123456:SECRET-TOKEN-must-never-be-printed";
    private static final String CHAT_ID = "987654321";
    private static final String FIXTURES = "src/notifierTest/resources/junit/";

    private HttpServer server;
    private final List<String> requests = new ArrayList<>();
    private final List<String> paths = new ArrayList<>();

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    /** Mock Telegram API: records the request, answers with the given status/body after an optional delay. */
    private void respond(int status, String body, long delayMs) {
        server.createContext("/", exchange -> {
            paths.add(exchange.getRequestURI().getPath());
            requests.add(URLDecoder.decode(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8),
                    StandardCharsets.UTF_8));
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException ignored) {
            }
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
    }

    private Map<String, String> env(String fixture) {
        Map<String, String> env = new HashMap<>();
        env.put("TELEGRAM_BOT_TOKEN", TOKEN);
        env.put("TELEGRAM_CHAT_ID", CHAT_ID);
        env.put("TELEGRAM_API_URL", "http://127.0.0.1:" + server.getAddress().getPort());
        env.put("TELEGRAM_TIMEOUT_SECONDS", "2");
        env.put("CI_NAME", "GitHub Actions");
        env.put("PROJECT_NAME", "AlinaKotlyarova/getting-started-java");
        env.put("BRANCH", "homework");
        env.put("COMMIT_SHA", "a33b300eacf8a4bc67cdbe4122d1a32572469e09");
        env.put("RUN_URL", "https://github.com/AlinaKotlyarova/getting-started-java/actions/runs/1");
        env.put("BUILD_OUTCOME", "success");
        env.put("TESTS_OUTCOME", "success");
        env.put("JUNIT_RESULTS_DIR", FIXTURES + fixture);
        return env;
    }

    private record Run(int exitCode, String output) {
    }

    private Run run(Map<String, String> env) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int code = TelegramNotifier.run(env, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        String output = buffer.toString(StandardCharsets.UTF_8);
        assertFalse(output.contains(TOKEN), "token leaked to output:\n" + output);
        assertFalse(output.contains("SECRET-TOKEN"), "token part leaked to output:\n" + output);
        assertFalse(output.contains(CHAT_ID), "chat id leaked to output:\n" + output);
        return new Run(code, output);
    }

    private String sentText() {
        assertEquals(1, requests.size(), "exactly one request expected");
        assertEquals("/bot" + TOKEN + "/sendMessage", paths.get(0));
        String body = requests.get(0);
        assertTrue(body.startsWith("chat_id=" + CHAT_ID + "&text="), body);
        return body;
    }

    // ---------- statistics and status from JUnit XML ----------

    @Test
    void success() {
        respond(200, "{\"ok\":true,\"result\":{}}", 0);
        Run run = run(env("success"));
        assertEquals(0, run.exitCode());
        String text = sentText();
        assertTrue(text.contains("✅ PASSED — AlinaKotlyarova/getting-started-java"), text);
        assertTrue(text.contains("CI: GitHub Actions"), text);
        assertTrue(text.contains("Branch: homework @ a33b300"), text);
        assertTrue(text.contains("Tests: 3 total · 3 passed · 0 failed · 0 skipped"), text);
        assertTrue(text.contains("Run: https://github.com/AlinaKotlyarova/getting-started-java/actions/runs/1"), text);
        assertTrue(run.output().contains("Telegram notification sent"), run.output());
    }

    @Test
    void failure() {
        respond(200, "{\"ok\":true}", 0);
        assertEquals(0, run(env("failure")).exitCode());
        String text = sentText();
        assertTrue(text.contains("❌ FAILED"), text);
        assertTrue(text.contains("Tests: 3 total · 2 passed · 1 failed · 0 skipped"), text);
    }

    @Test
    void error() {
        respond(200, "{\"ok\":true}", 0);
        run(env("error"));
        String text = sentText();
        assertTrue(text.contains("❌ FAILED"), text);
        assertTrue(text.contains("Tests: 2 total · 1 passed · 1 failed (1 errors) · 0 skipped"), text);
    }

    @Test
    void skipped() {
        respond(200, "{\"ok\":true}", 0);
        run(env("skipped"));
        String text = sentText();
        assertTrue(text.contains("✅ PASSED"), text);
        assertTrue(text.contains("Tests: 3 total · 2 passed · 0 failed · 1 skipped"), text);
    }

    @Test
    void multipleFilesAndNestedSuitesAreNotDoubleCounted() {
        respond(200, "{\"ok\":true}", 0);
        run(env("multiple"));
        String text = sentText();
        // 2 + 3 testcases; summing nested <testsuite tests="3"> attributes would give 2 + 3*3 = 11
        assertTrue(text.contains("❌ FAILED"), text);
        assertTrue(text.contains("Tests: 5 total · 2 passed · 2 failed (1 errors) · 1 skipped"), text);
    }

    @Test
    void missingXmlIsNotSuccess() {
        respond(200, "{\"ok\":true}", 0);
        run(env("does-not-exist"));
        String text = sentText();
        assertTrue(text.contains("⚠️ NO TEST RESULTS"), text);
        assertTrue(text.contains("Note: No JUnit XML found"), text);
    }

    @Test
    void brokenXmlIsNotSuccess() {
        respond(200, "{\"ok\":true}", 0);
        Run run = run(env("broken"));
        String text = sentText();
        assertTrue(text.contains("⚠️ NO TEST RESULTS"), text);
        assertTrue(text.contains("Note: Unreadable JUnit XML: TEST-tests.SearchTests.xml"), text);
        assertFalse(run.output().contains("[Fatal Error]"), run.output());
    }

    @Test
    void zeroTestsIsNotSuccess() {
        respond(200, "{\"ok\":true}", 0);
        run(env("zero"));
        String text = sentText();
        assertTrue(text.contains("⚠️ NO TEST RESULTS"), text);
        assertTrue(text.contains("Note: 0 tests executed"), text);
    }

    @Test
    void allSkippedIsNotSuccess() {
        respond(200, "{\"ok\":true}", 0);
        run(env("all-skipped"));
        String text = sentText();
        assertTrue(text.contains("⚠️ NO TEST RESULTS"), text);
        assertTrue(text.contains("Note: No tests passed (all skipped)"), text);
    }

    @Test
    void errorBeforeTestsIsBuildErrorEvenIfOldXmlExists() {
        respond(200, "{\"ok\":true}", 0);
        Map<String, String> env = env("success"); // stale green XML must not be reported
        env.put("BUILD_OUTCOME", "failure");
        env.put("TESTS_OUTCOME", "skipped");
        run(env);
        String text = sentText();
        assertTrue(text.contains("🛑 BUILD ERROR — tests did not run"), text);
        assertTrue(text.contains("Tests: not run"), text);
        assertTrue(text.contains("Note: Compilation step: failure"), text);
    }

    @Test
    void stepsSkippedBeforeCompilationIsBuildError() {
        respond(200, "{\"ok\":true}", 0);
        Map<String, String> env = env("success");
        env.put("BUILD_OUTCOME", "skipped"); // e.g. Chrome setup failed, compile never ran
        run(env);
        assertTrue(sentText().contains("🛑 BUILD ERROR"));
    }

    @Test
    void greenXmlButFailedTestStepIsFailure() {
        respond(200, "{\"ok\":true}", 0);
        Map<String, String> env = env("success");
        env.put("TESTS_OUTCOME", "failure");
        run(env);
        String text = sentText();
        assertTrue(text.contains("❌ FAILED"), text);
        assertTrue(text.contains("Note: Test step outcome: failure"), text);
    }

    // ---------- secrets and Telegram API errors ----------

    @Test
    void noSecretsSkipsWithoutRequest() {
        respond(200, "{\"ok\":true}", 0);
        Map<String, String> env = env("failure");
        env.remove("TELEGRAM_BOT_TOKEN");
        env.put("TELEGRAM_CHAT_ID", "");
        env.put("GITHUB_ACTIONS", "true");
        Run run = run(env);
        assertEquals(0, run.exitCode());
        assertTrue(requests.isEmpty());
        assertTrue(run.output().contains("::warning::Telegram notification skipped"), run.output());
    }

    @Test
    void httpErrorFails() {
        respond(500, "Internal Server Error", 0);
        Run run = run(env("success"));
        assertEquals(1, run.exitCode());
        assertTrue(run.output().contains("Telegram notification failed: HTTP 500, ok=false"), run.output());
    }

    @Test
    void okFalseWithErrorStatusFails() {
        respond(400, "{\"ok\":false,\"error_code\":400,\"description\":\"Bad Request: chat not found\"}", 0);
        Run run = run(env("success"));
        assertEquals(1, run.exitCode());
        assertTrue(run.output().contains("HTTP 400, ok=false, description: Bad Request: chat not found"), run.output());
    }

    @Test
    void okFalseWithHttp200Fails() {
        respond(200, "{\"ok\":false,\"description\":\"Unexpected\"}", 0);
        Run run = run(env("success"));
        assertEquals(1, run.exitCode());
        assertTrue(run.output().contains("HTTP 200, ok=false"), run.output());
    }

    @Test
    void timeoutFails() {
        respond(200, "{\"ok\":true}", 4000);
        Map<String, String> env = env("success");
        env.put("TELEGRAM_TIMEOUT_SECONDS", "1");
        long start = System.nanoTime();
        Run run = run(env);
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertEquals(1, run.exitCode());
        assertTrue(run.output().contains("HttpTimeoutException"), run.output());
        assertTrue(elapsedMs < 3500, "timeout not applied, took " + elapsedMs + "ms");
    }

    @Test
    void malformedTokenIsNotLeakedInErrorMessage() {
        // A secret pasted with a space/newline makes URI.create throw with the full URL (token included)
        String badToken = "123456:SECRET-TOKEN pasted-with-space";
        Map<String, String> env = env("success");
        env.put("TELEGRAM_BOT_TOKEN", badToken);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int code = TelegramNotifier.run(env, new PrintStream(buffer, true, StandardCharsets.UTF_8));
        String output = buffer.toString(StandardCharsets.UTF_8);
        assertEquals(1, code);
        assertTrue(output.contains("Telegram notification failed: IllegalArgumentException"), output);
        assertFalse(output.contains("SECRET-TOKEN"), "token leaked:\n" + output);
        assertFalse(output.contains("pasted-with-space"), "token leaked:\n" + output);
    }

    @Test
    void connectionRefusedFailsWithoutLeakingToken() throws IOException {
        int closedPort;
        try (ServerSocket socket = new ServerSocket(0)) {
            closedPort = socket.getLocalPort();
        }
        Map<String, String> env = env("success");
        env.put("TELEGRAM_API_URL", "http://127.0.0.1:" + closedPort);
        Run run = run(env); // run() asserts the token is not in the output
        assertEquals(1, run.exitCode());
        assertTrue(run.output().contains("Telegram notification failed: ConnectException"), run.output());
    }
}
