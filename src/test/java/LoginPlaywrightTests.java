import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginPlaywrightTests {

    private static Playwright playwright;
    private Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void beforeAll() {
        playwright = Playwright.create();
    }

    @BeforeEach
    void setUp() {
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
        if (browser != null) browser.close();
    }

    @AfterAll
    static void afterAll() {
        if (playwright != null) playwright.close();
    }

    @Test
    void successLoginTest() {
        page.navigate("https://niffler.qa.guru");
        page.fill("#username", "stas");
        page.fill("#password", "12345");
        page.click("#login-button");

        page.waitForSelector("#spendings");
        assertTrue(page.textContent("#spendings").contains("History of Spendings"));
    }

    @Test
    void successLoginWithPressEnterTest() {
        page.navigate("https://niffler.qa.guru");
        page.fill("#username", "stas");
        page.fill("#password", "12345");
        page.keyboard().press("Enter");

        page.waitForSelector("#spendings");
        assertTrue(page.textContent("#spendings").contains("History of Spendings"));
    }

    @Test
    void successLoginAsUserTest() {
        page.navigate("https://niffler.qa.guru");
        page.fill("#username", "stas");
        page.fill("#password", "12345");
        page.keyboard().press("Enter");

        page.waitForSelector("#spendings");
        assertTrue(page.textContent("#spendings").contains("History of Spendings"));

        page.navigate("https://niffler.qa.guru/profile");
        page.waitForSelector("#username");
        String usernameValue = page.getAttribute("#username", "value");
        assertEquals("stas", usernameValue);
    }

    @Test
    void wrongCredentialsTest() {
        page.navigate("https://niffler.qa.guru");
        page.fill("#username", "stas");
        page.fill("#password", "wrong_pass");
        page.keyboard().press("Enter");

        page.waitForSelector(".form__error");
        assertTrue(page.textContent(".form__error").contains("Bad credentials"));
    }
}