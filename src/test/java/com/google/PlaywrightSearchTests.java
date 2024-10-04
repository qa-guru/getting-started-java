package com.google;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class PlaywrightSearchTests {

    @Test
    void successfulSearchTest() {
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false));
        Page page = browser.newPage();

        page.navigate("https://www.google.com/");

        page.locator("[name=q]").fill("selenide");
        page.locator("[name=q]").press("Enter");

        assertThat(page.locator("#search")).containsText("https://selenide.org");
    }
}