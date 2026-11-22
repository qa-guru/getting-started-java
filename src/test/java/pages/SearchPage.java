package pages;

import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class SearchPage {

    private final SelenideElement searchInput = $("[aria-label='Search GitHub']");

    // Selenide.open is called explicitly: this class's own open() would shadow a static import
    public SearchPage open() {
        Selenide.open("/search");
        return this;
    }

    public SearchPage openRepositoriesSearch() {
        // type=repositories is set up front: for "repo:" queries GitHub otherwise picks the Issues tab
        Selenide.open("/search?type=repositories");
        return this;
    }

    public SearchResultsPage searchFor(String query) {
        searchInput.setValue(query).pressEnter();
        return new SearchResultsPage();
    }
}
