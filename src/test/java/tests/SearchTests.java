package tests;

import org.junit.jupiter.api.Test;
import pages.MainPage;
import pages.SearchPage;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class SearchTests {
    @Test
    void successfulSearchTest() {
        open("https://github.com/search");
        $("[aria-label='Search GitHub']").setValue("qa.guru").pressEnter();
        $("[data-testid='results-list']").shouldHave(text("QA.GURU"));
    }

    @Test
    void successfulSearchWithNewPageObjectsTest() {
        new MainPage().openPage();
        new MainPage().typeSearch("qa.guru");
        new SearchPage().checkResult("QA.GURU");
    }

    @Test
    void successfulSearchWithPageObjectsTest() {
        MainPage mainPage = new MainPage();
        SearchPage searchPage = new SearchPage();

        mainPage.openPage();
        mainPage.typeSearch("qa.guru");
        searchPage.checkResult("QA.GURU");
    }

    @Test
    void successfulSearchWithFluentTest() {
        MainPage mainPage = new MainPage();

        mainPage.openPage()
                .typeSearch("qa.guru")
                .checkResult("QA.GURU");
    }
}
