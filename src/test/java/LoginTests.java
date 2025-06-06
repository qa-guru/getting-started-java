import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

public class LoginTests {
    @Test
    void successfulLoginTest() {
        Configuration.holdBrowserOpen = true;
        Configuration.timeout = 10000;

        open("https://niffler.qa.guru");
        $("[name=username]").setValue("qaguru31");
        $("[name=password]").setValue("1234").pressEnter();
        $("[id=stat]").shouldHave(text("Statis1tics"));

    }

    @Test
    void badCredentialsLoginTest() {
        Configuration.holdBrowserOpen = true;

        open("https://niffler.qa.guru");
        $("[name=username]").setValue("stas");
        $("[name=password]").setValue("wrongpassword").pressEnter();
        $("[class=form__error]").shouldHave(text("Bad credentials"));

    }

}
