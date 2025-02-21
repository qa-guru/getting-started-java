import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selectors.byText;
import static com.codeborne.selenide.Selenide.*;

public class LoginTest {

    @AfterEach
    void teardown() {
        closeWebDriver();
    }

    @Test
    void successLogin() {
//        Configuration.browser = "firefox";
//        Configuration.browserSize = "1920x1080";
//        Configuration.holdBrowserOpen = true;

        open("https://niffler.qa.guru");
        $("[name=username]").setValue("java34");
        $("[name=password]").setValue("pass34");
        $("button[type=submit]").click();

        $("[id=spendings]").shouldHave(text("History of Spendings"));
    }

    @Test
    void successLoginWithPresEnter() {
        open("https://niffler.qa.guru");
        $("[name=username]").setValue("java34");
        $("[name=password]").setValue("pass34").pressEnter();

        $("[id=spendings]").shouldHave(text("History of Spendings"));
    }

    @Test
    void successLogout() {
        open("https://niffler.qa.guru");
        $("[name=username]").setValue("java34");
        $("[name=password]").setValue("pass34").pressEnter();
        $("[id=spendings]").shouldHave(text("History of Spendings"));
        $("button[aria-label=Menu]").click();
        $("[role=menu]").$(byText("Sign out")).click();
        $("[role=dialog]").$(byText("Log out")).click();

        $("[class=header]").shouldHave(text("Log in"));
    }

    @Test
    void wrongCredentialsLogin() {
        open("https://niffler.qa.guru");
        $("[name=username]").setValue("java34");
        $("[name=password]").setValue("pass341");
        $("button[type=submit]").click();

        $("[class=form__error]").shouldHave(text("Bad credentials"));
    }
}
