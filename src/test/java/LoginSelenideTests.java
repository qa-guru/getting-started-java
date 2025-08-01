import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class LoginSelenideTests {
    @Test
    void successfulLoginTest() {
        Configuration.browser = "firefox";
        open("https://niffler.qa.guru");
        $("[id=username]").setValue("stas");
        $("[id=password]").setValue("12345");
        $("[id=login-button]").click();
        $("[id=spendings]").shouldHave(text("History of Spendings"));
    }
}
