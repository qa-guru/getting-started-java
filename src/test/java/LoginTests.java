import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

public class LoginTests {

    @BeforeAll
    static void setup() {
        Configuration.baseUrl = "https://school.qa.guru";
//        Configuration.browser = "firefox"; // mozilla
        Configuration.browserVersion = "120.0";
        Configuration.browserSize = "1920x1080";
    }

    @AfterEach
    void teardown() {
        closeWebDriver();
    }

    @Test
    void successfulLoginTest() {
        open("/");
        $("[name=email]").setValue("qagurubot@gmail.com");
        $("[name=password]").setValue("somepasshere").pressEnter();

        $(".page-header").shouldHave(text("Список тренингов"));

        open("/cms/system/login");
        $(".logined-form").shouldHave(text("QA_GURU_BOT"));
    }

    @Test
    void wrongPasswordTest() {
        open("/");
        $("[name=email]").setValue("qagurubot@gmail.com");
        $("[name=password]").setValue("some1a3241passhere").pressEnter();

        $(".btn-error").shouldHave(text("Неверный пароль"));
    }

    @Test
    void missedPasswordTest() {
        open("/");
        $("[name=email]").setValue("qagurubot@gmail.com").pressEnter();

        $(".btn-error").shouldHave(text("Не заполнено поле Пароль"));
    }

    @Test
    void missedLoginTest() {
        open("/");
        $("[name=password]").setValue("some1a3241passhere").pressEnter();

        $(".btn-error").shouldHave(text("Не заполнено поле E-Mail"));
    }
}
