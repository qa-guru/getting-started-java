import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

//
public class Learn {

    @Test
    void loginTest() {
        // записать шаги теста коментариями
        //  искать по плейсхолдеру и дата-тест-селектор
        // "[имя-атрибута=значение-атрибута]"
        // pressenter
        //# -айдишки, .-класс
        //setTimeout(function () {debugger},4000); в браузере замораживает экран, через консоль
        open("https://portal.servers.com");
        $(".CybotCookiebotDialogBodyButton").click();
        $(".lj16mvx").click();
        $(".policy_and_terms").setValue("true");
        $("[name=email]").setValue("myemail+ask_ev@gmail.com");
        $(".Join").click();
        // $(".login-form").shouldHave(text("Войти"));
        //  $("[name=email]").setValue("qagurubot@gmail.com");
        // $("[name=password]").setValue("qagurupassword");
        //  $(".btn-success").click();
        //  $(".main-header__login").click();
        //  $(".logined-form").shouldHave(text("Здравствуйте, QA_GURU_BOT"));
    }
}