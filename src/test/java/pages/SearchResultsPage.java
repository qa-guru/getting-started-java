package pages;

import com.codeborne.selenide.SelenideElement;
import io.qameta.allure.Step;

import static com.codeborne.selenide.Condition.exist;
import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

public class SearchResultsPage {

    private final SelenideElement resultsList = $("[data-testid='results-list']");
    private final SelenideElement emptyStateHeading =
            $$("h3").findBy(text("Your search did not match any repositories"));

    @Step("Results should contain \"{expectedText}\"")
    public SearchResultsPage shouldHaveResult(String expectedText) {
        resultsList.shouldHave(text(expectedText));
        return this;
    }

    @Step("Results should be empty")
    public SearchResultsPage shouldBeEmpty() {
        // order matters: shouldNot(exist) passes instantly before the page has rendered
        emptyStateHeading.shouldBe(visible);
        resultsList.shouldNot(exist);
        return this;
    }

    @Step("Open repository {fullName} from results")
    public RepositoryPage openRepository(String fullName) {
        $("[data-testid='results-list'] a[href='/" + fullName + "']").click();
        return new RepositoryPage();
    }
}
