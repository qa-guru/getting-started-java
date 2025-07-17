import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.*;

public class LoginSelenideTests {

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }

    @Test
    void successLoginTest() {
        open("https://niffler.qa.guru");
        $("[id=username]").setValue("stas");
        $("[id=password]").setValue("12345");
        $("[id=login-button]").click();
        $("[id=spendings]").shouldHave(text("History of Spendings"));
    }

    @Test
    void successLoginWithPressEnterTest() {
        open("https://niffler.qa.guru");
        $("[id=username]").setValue("stas");
        $("[id=password]").setValue("12345").pressEnter();
        $("[id=spendings]").shouldHave(text("History of Spendings"));
    }

    @Test
    void successLoginAsUserTest() {
        open("https://niffler.qa.guru");
        $("[id=username]").setValue("stas");
        $("[id=password]").setValue("12345").pressEnter();
        $("[id=spendings]").shouldHave(text("History of Spendings"));

        open("https://niffler.qa.guru/profile");
        $("[id=username]").shouldHave(value("stas"));
    }

    @Test
    void wrongCredentialsTest() {
        open("https://niffler.qa.guru");
        $("[id=username]").setValue("stas");
        $("[id=password]").setValue("wrong_pass").pressEnter();
        $("[class=form__error]").shouldHave(text("Bad credentials"));
    }
}
