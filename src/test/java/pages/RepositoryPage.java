package pages;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.webdriver;
import static com.codeborne.selenide.WebDriverConditions.url;

public class RepositoryPage {

    private final SelenideElement repositoryTitle = $("#repo-title-component");

    public RepositoryPage shouldBeOpened(String owner, String name) {
        webdriver().shouldHave(url(Configuration.baseUrl + "/" + owner + "/" + name));
        repositoryTitle.shouldHave(text(name));
        return this;
    }
}
