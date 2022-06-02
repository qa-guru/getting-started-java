package pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selectors.byName;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class MainPage {
    SelenideElement searchInput = $(byName("q"));

    public MainPage openPage() {
        open("https://www.google.com/");

        return this;
    }

    public SearchPage typeSearch(String value) {
        searchInput.setValue(value).pressEnter();

        return new SearchPage();
    }
}
