package guru.qa.niffler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.*;


public class SelenideLoginTests {

    @AfterEach
    void tearDown() {
        closeWebDriver();
    }

    @Test
    void successfulLoginTest() {
        open("https://niffler.qa.guru");
        $("[name=username]").setValue("qaguru31");
        $("[name=password]").setValue("1234");
        $("[class=form__submit]").click();

        $("[id=stat]").shouldHave(text("Statistics"));
        $("[id=spendings]").shouldHave(text("History of Spendings"));
    }

    @Test
    void successfulLoginWithPressEnterTest() {
        open("https://niffler.qa.guru");
        $("[name=username]").setValue("qaguru31");
        $("[name=password]").setValue("1234").pressEnter();

        $("[id=stat]").shouldHave(text("Statistics"));
        $("[id=spendings]").shouldHave(text("History of Spendings"));
    }

    @Test
    void successfulLoginWithUsernameCheckTest() { // BAD PRACTICE
        open("https://niffler.qa.guru");
        $("[name=username]").setValue("qaguru31");
        $("[name=password]").setValue("1234").pressEnter();

        $("[id=stat]").shouldHave(text("Statistics"));
        $("[id=spendings]").shouldHave(text("History of Spendings"));

        // move to another test
        open("https://niffler.qa.guru/profile");
        $("[id=username]").shouldHave(value("qaguru31"));
    }

    @Test
    void wrongCredentialsLoginTest() {
        open("https://niffler.qa.guru");
        $("[name=username]").setValue("qaguru31");
        $("[name=password]").setValue("1234dsfbgsdghdfstgh");
        $("[class=form__submit]").click();

        $("[class=form__error]").shouldHave(text("Неверные учетные данные пользователя"));
    }

    @Test
    @Disabled
    void emptyLoginTest() {
        open("https://niffler.qa.guru");
        $("[name=password]").setValue("1234dsfbgsdghdfstgh");
        $("[class=form__submit]").click();

        $("[class=form__error]").shouldHave(text(""));
    }
}
