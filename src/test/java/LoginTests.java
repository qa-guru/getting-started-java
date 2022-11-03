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
    void successfulLoginWithCommentsTest() {
//        Configuration.holdBrowserOpen = true;
//        Configuration.browser = "firefox";

//        Открыть форму авторизации
//        https://qa.guru/cms/system/login
//        Ввести адрес электронной почты
//        Ввести пароль
//        Нажать кнопку "Войти"
//        Нажать на кнопку "Личный кабинет"
//        Проверить успешную авторизацию


//        Открыть форму авторизации
//        open("https://qa.gurun");
//        $(".main-header__login").click();
        open("https://qa.guru/cms/system/login");
        $(".login-form").shouldHave(text("Войти"));

//        Ввести адрес электронной почты
        $("[name=email]").setValue("qagurubot@gmail.com");
//        Ввести пароль
        $("[name=password]").setValue("qagurupassword");
//        Нажать кнопку "Войти"
        $(".btn-success").click();

//        Перейти "Личный кабинет"
        $(".main-header__login").click();
//        open("https://qa.guru/cms/system/login");

        //        Проверить успешную авторизацию
        $(".logined-form").shouldHave(text("Здравствуйте, QA_GURU_BOT"));
    }
}
