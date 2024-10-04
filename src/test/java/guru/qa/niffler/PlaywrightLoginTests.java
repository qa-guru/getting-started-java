package guru.qa.niffler;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PlaywrightLoginTests {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void globalSetup() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false));
    }

    @AfterAll
    static void globalTeardown() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void successfulLoginTest() {
        page.navigate("https://niffler.qa.guru");
        page.fill("[name='username']", "qaguru31");
        page.fill("[name='password']", "1234");
        page.click(".form__submit");

        assertThat(page.locator("#stat")).hasText("Statistics");
        assertThat(page.locator("#spendings")).containsText("History of Spendings");
    }

    @Test
    void successfulLoginWithPressEnterTest() {
        page.navigate("https://niffler.qa.guru");
        page.fill("[name='username']", "qaguru31");
        page.fill("[name='password']", "1234");
        page.press("[name='password']", "Enter");

        assertThat(page.locator("#stat")).hasText("Statistics");
        assertThat(page.locator("#spendings")).containsText("History of Spendings");
    }

    @Test
    void successfulLoginWithUsernameCheckTest() { // Плохая практика
        page.navigate("https://niffler.qa.guru");
        page.fill("[name='username']", "qaguru31");
        page.fill("[name='password']", "1234");
        page.press("[name='password']", "Enter");

        assertThat(page.locator("#stat")).hasText("Statistics");
        assertThat(page.locator("#spendings")).containsText("History of Spendings");

        page.navigate("https://niffler.qa.guru/profile");
        assertThat(page.locator("#username")).hasValue("qaguru31");
    }

    @Test
    void wrongCredentialsLoginTest() {
        page.navigate("https://niffler.qa.guru");
        page.fill("[name='username']", "qaguru31");
        page.fill("[name='password']", "1234dsfbgsdghdfstgh");
        page.click(".form__submit");

        assertThat(page.locator(".form__error")).hasText("Неверные учетные данные пользователя");
    }

    @Test
    @Disabled("Тест отключен по причине ...") // Укажите причину отключения
    void emptyLoginTest() {
        page.navigate("https://niffler.qa.guru");
        page.fill("[name='password']", "1234dsfbgsdghdfstgh");
        page.click(".form__submit");

        assertThat(page.locator(".form__error")).hasText("");
    }
}