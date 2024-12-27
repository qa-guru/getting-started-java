import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class SearchExtendedTests {
    @Test
    void successfulSearchTest() {
        openSite();
        $("[name=q]").setValue("qa.guru");
        $("[name=btnK]").click();
        checkResult("https://qa.guru");
    }

    @Test
    void successfulSearchWithEnterTest() {
        openSite();
        $("[name=q]").setValue("qa.guru").pressEnter();
        checkResult("https://qa.guru");
    }



    void openSite() {
        open("https://www.google.com/");
    }

    void checkResult(String value) {
        $("[id=search]").shouldHave(text(value));
    }
}
