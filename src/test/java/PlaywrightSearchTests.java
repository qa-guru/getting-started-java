import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PlaywrightSearchTests {

    @Test
    void successfulSearchTest() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false));
            Page page = browser.newPage();

            page.navigate("https://github.com/search");
            page.locator("[aria-label='Search GitHub']").fill("qa.guru");
            page.locator("[aria-label='Search GitHub']").press("Enter");

            assertThat(page.locator("[data-testid='results-list']")).containsText("QA.GURU");

            browser.close();
        }
    }
}
