package pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Selenide.$;

public class SearchPage {

    private final SelenideElement searchInput = $("[aria-label='Search GitHub']");

    // Selenide.open is called explicitly: this class's own open() would shadow a static import
    @Step("Open GitHub search")
    public SearchPage open() {
        Selenide.open("/search");
        return this;
    }

    @Step("Open GitHub repositories search")
    public SearchPage openRepositoriesSearch() {
        // type=repositories is set up front: for "repo:" queries GitHub otherwise picks the Issues tab
        Selenide.open("/search?type=repositories");
        return this;
    }

    @Step("Search for \"{query}\"")
    public SearchResultsPage searchFor(String query) {
        searchInput.setValue(query).pressEnter();
        return new SearchResultsPage();
    }
}
