import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.url;

public class SearchTests extends TestBase {
    @Test
    void successfulSearchTest() {
        open("/search");
        $("[aria-label='Search GitHub']").setValue("qa.guru").pressEnter();
        $("[data-testid='results-list']").shouldHave(text("QA.GURU"));
    }

    @Test
    void searchByRepoQualifierOpensRepository() {
        // type=repositories is set up front: for "repo:" queries GitHub otherwise picks the Issues tab
        open("/search?type=repositories");
        $("[aria-label='Search GitHub']").setValue("repo:selenide/selenide").pressEnter();

        $("[data-testid='results-list'] a[href='/selenide/selenide']").click();

        webdriver().shouldHave(url(Configuration.baseUrl + "/selenide/selenide"));
        $("#repo-title-component").shouldHave(text("selenide"));
    }

    @Test
    void impossibleStarsFilterShowsEmptyState() {
        // stars:<0 is a qualifier, and a star count can never be negative
        open("/search?type=repositories");
        $("[aria-label='Search GitHub']").setValue("stars:<0").pressEnter();

        $$("h3").findBy(text("Your search did not match any repositories")).shouldBe(visible);
        $("[data-testid='results-list']").shouldNot(exist);
    }
}
