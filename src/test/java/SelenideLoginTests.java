
import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.*;
import java.nio.file.Paths;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

public class SelenideLoginTests {

    private final String url = Paths.get("src/test/resources/login.html").toUri().toString();

    @BeforeEach
    void setupTest() {
        Configuration.browser = "chrome";
//        Configuration.headless = true;
        Configuration.timeout = 5000;
        open(url);
    }

    @AfterEach
    void teardown() {
        closeWebDriver();
    }

    @Test
    void successfulLoginTest() {
        $("#username").setValue("admin");
        $("#password").setValue("admin");
        $("#loginButton").click();

        $("#message").shouldHave(text("Login successful!")).shouldHave(cssClass("success"));
        $(".login-container").shouldNotBe(visible);
        $("#greeting").shouldHave(text("Welcome, admin!"));
        $("#logoutButton").shouldBe(visible);
    }

    @Test
    void successfulLoginWithEnterTest() {
        $("#username").setValue("admin");
        $("#password").setValue("admin").pressEnter();

        $("#message").shouldHave(text("Login successful!")).shouldHave(cssClass("success"));
        $(".login-container").shouldNotBe(visible);
        $("#greeting").shouldHave(text("Welcome, admin!"));
        $("#logoutButton").shouldBe(visible);
    }

    @Test
    void emptyUsernameTest() {
        $("#password").setValue("admin");
        $("#loginButton").click();

        $("#message").shouldHave(text("Username is required.")).shouldHave(cssClass("error"));
    }

    @Test
    void emptyPasswordTest() {
        $("#username").setValue("admin");
        $("#loginButton").click();

        $("#message").shouldHave(text("Password is required.")).shouldHave(cssClass("error"));
    }

    @Test
    void emptyUsernameAndPasswordTest() {
        $("#loginButton").click();

        $("#message").shouldHave(text("Username and Password are required.")).shouldHave(cssClass("error"));
    }

    @Test
    void invalidCredentialsTest() {
        $("#username").setValue("admin");
        $("#password").setValue("wrongpassword");
        $("#loginButton").click();

        $("#message").shouldHave(text("Invalid username or password.")).shouldHave(cssClass("error"));
    }

    @Test
    void logoutTest() {
        $("#username").setValue("admin");
        $("#password").setValue("admin");
        $("#loginButton").click();

        $("#logoutButton").shouldBe(visible).click();
        $(".login-container").shouldBe(visible);
        $("#greeting").shouldBe(empty);
        $("#logoutButton").shouldNotBe(visible);
    }
}
