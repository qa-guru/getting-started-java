package tests;

import org.junit.jupiter.api.Test;
import pages.MainPage;
import pages.SearchPage;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byName;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class SearchTests {
    @Test
    void successfulSearchTest() {
        open("https://www.google.com/");
        $(byName("q")).setValue("selenide").pressEnter();
        $("#search").shouldHave(text("https://selenide.org"));
    }

    @Test
    void successfulSearchWithNewPageObjectsTest() {
        new MainPage().openPage();
        new MainPage().typeSearch("selenide");
        new SearchPage().checkResult("https://selenide.org");
    }

    @Test
    void successfulSearchWithPageObjectsTest() {
        MainPage mainPage = new MainPage();
        SearchPage searchPage = new SearchPage();

        mainPage.openPage();
        mainPage.typeSearch("selenide");
        searchPage.checkResult("https://selenide.org");
    }

    @Test
    void successfulSearchWithFluentTest() {
        MainPage mainPage = new MainPage();

        mainPage.openPage()
                .typeSearch("selenide")
                .checkResult("https://selenide.org");
    }
}
