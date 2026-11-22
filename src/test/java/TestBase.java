import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.Selenide;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public class TestBase {

    @BeforeEach
    void setUpBrowser() {
        Configuration.baseUrl = System.getProperty("baseUrl", "https://github.com");
        Configuration.browser = System.getProperty("browser", "chrome");
        Configuration.browserSize = System.getProperty("browserSize", "1920x1080");
        Configuration.headless = Boolean.parseBoolean(System.getProperty("headless", "false"));

        System.out.printf("Test config: baseUrl=%s, browser=%s, browserSize=%s, headless=%s%n",
                Configuration.baseUrl, Configuration.browser,
                Configuration.browserSize, Configuration.headless);
    }

    @AfterEach
    void closeBrowser() {
        Selenide.closeWebDriver();
    }
}
