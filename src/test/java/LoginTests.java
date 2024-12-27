import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

public class LoginTests {
    String url = Paths.get("src/test/resources/login.html").toUri().toString();

    @Test
    void successfulLoginTest() {
        open(url);

        $("#username").setValue("testuser");
        $("#password").setValue("password123");
        $("button[type='submit']").click();

        // Проверяем успешный результат (например, наличие сообщения)
        $("h1").shouldHave(text("Добро пожаловать!")); // Замените селектор/текст на ваш реальный
    }

    @Test
    void emptyUsernameShowsErrorTest() {
        open(url);

        $("#password").setValue("password123");
        $("button[type='submit']").click();

        // Проверяем, что отображается сообщение об ошибке
        $(".error-message").shouldHave(text("Имя пользователя обязательно")); // Замените селектор/текст на ваш реальный
    }
}