import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Selectors.*;
import static com.codeborne.selenide.Condition.*;


public class SelenidRepositoriyTest {
    @Test
    void shouldFindSelenideRepositoryAtTheTop() {
        open("https://github.com");
        $("[data-test-selector=nav-search-input]").setValue("selenide").pressEnter();
        $$("ul.repo-list li").first().$("div.f4").click();
        $("#repository-container-header").shouldHave(text("selenide / selenide"));
    }
    @Test
    void BestContributors(){
        open("https://github.com/selenide/selenide");
        $(".BorderGrid").$(byText("Contributors")).ancestor(".BorderGrid-row");
        $$("ul li").first().hover();
        $$(".Popover .Popover-message").findBy(visible).shouldHave(text("Andrei Solntsev"));
    }
}
