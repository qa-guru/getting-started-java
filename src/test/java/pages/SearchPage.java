package pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;

public class SearchPage {
    SelenideElement searchResults = $("[data-testid='results-list']");

    public void checkResult(String value) {
        searchResults.shouldHave(text(value));
    }
}
