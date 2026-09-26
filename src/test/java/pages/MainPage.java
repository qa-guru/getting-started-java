package pages;

import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class MainPage {
    SelenideElement searchInput = $("[aria-label='Search GitHub']");

    public MainPage openPage() {
        open("https://github.com/search");

        return this;
    }

    public SearchPage typeSearch(String value) {
        searchInput.setValue(value).pressEnter();

        return new SearchPage();
    }
}
