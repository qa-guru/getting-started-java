import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class LoginTests {

    @Test
    void successfulLoginTest() {
        open("https://qa.guru/cms/system/login");
        $(".login-form").shouldHave(text("Войти"));
        $("[name=email]").setValue("qagurubot@gmail.com");
        $("[name=password]").setValue("qagurupassword");
        $(".btn-success").click();
        $(".main-header__login").click();
        $(".logined-form").shouldHave(text("Здравствуйте, QA_GURU_BOT"));
    }

    @Test
    void wrongLoginTest() {
        open("https://qa.guru/cms/system/login");
        $(".login-form").shouldHave(text("Войти"));
        $("[name=email]").setValue("blablabla");
        $("[name=password]").setValue("qagurupassword");
        $(".btn-success").click();
        $(".btn-error").shouldHave(text("Неверный формат e-mail"));
    }

    @Test
    void wrongPassTest() {
        Configuration.holdBrowserOpen = true;
        open("https://qa.guru/cms/system/login");
        $(".login-form").shouldHave(text("Войти"));
        $("[name=email]").setValue("qagurubot@gmail.com");
        $("[name=password]").setValue("blablablablabla");
        $(".btn-success").click();
        $(".btn-error").shouldHave(text("Неверный пароль"));
    }
}