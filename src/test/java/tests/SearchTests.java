package tests;

import org.junit.jupiter.api.Test;
import pages.SearchPage;

public class SearchTests extends TestBase {

    private final SearchPage searchPage = new SearchPage();

    @Test
    void successfulSearchTest() {
        searchPage.open()
                .searchFor("qa.guru")
                .shouldHaveResult("БЛА БЛА БЛА");
    }

    @Test
    void searchByRepoQualifierOpensRepository() {
        searchPage.openRepositoriesSearch()
                .searchFor("repo:selenide/selenide")
                .openRepository("selenide/selenide")
                .shouldBeOpened("selenide", "selenide");
    }

    @Test
    void impossibleStarsFilterShowsEmptyState() {
        // stars:<0 is a qualifier, and a star count can never be negative
        searchPage.openRepositoriesSearch()
                .searchFor("stars:<0")
                .shouldBeEmpty();
    }
}
