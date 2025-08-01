import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginPlaywrightTests {

    @Test
    void successfulLoginTest() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            page.navigate("https://niffler.qa.guru");
            page.fill("#username", "stas");
            page.fill("#password", "12345");
            page.click("#login-button");

            page.waitForSelector("#spendings");
            String spendingsText = page.textContent("#spendings");

            assertTrue(spendingsText.contains("History of Spendings"));

            browser.close();
        }
    }
}
