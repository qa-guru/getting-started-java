import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

public class LoginTests {

    @Test
    void successfulSearchTest() {
        Configuration.pageLoadStrategy = "eager";
//        Configuration.browser = "mozilla";
//        Configuration.browserSize = "1920x1080";

        open("https://school.qa.guru");
        $("[name=email]").setValue("qagurubot@gmail.com");
        $("[name=password]").setValue("somepasshere").pressEnter();
        $(".page-header").shouldHave(text("Список тренингов"));
        open("https://school.qa.guru/cms/system/login");
        $(".logined-form").shouldHave(text("QA_GURU_BOT"));
        closeWebDriver();
    }

    @Test
    void wrongPasswordSearchTest() {
        Configuration.pageLoadStrategy = "eager";

        open("https://school.qa.guru");
        $("[name=email]").setValue("qagurubot@gmail.com");
        $("[name=password]").setValue("somepasssfewhere").pressEnter();
        $(".btn-success").shouldHave(text("Неверный пароль"));
        closeWebDriver();
    }

    @Test
    void missingPasswordSearchTest() {
        Configuration.pageLoadStrategy = "eager";

        open("https://school.qa.guru");
        $("[name=email]").setValue("qagurubot@gmail.com").pressEnter();
        $(".btn-success").shouldHave(text("Не заполнено поле Пароль"));
        closeWebDriver();
    }
}
