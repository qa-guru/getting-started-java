import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

public class YaSearchTests {
    @Test
    void successfulSearchTest() {
        open("https://ya.ru");
        $("#text").setValue("selenide").pressEnter();
        $("[id=search-result]").shouldHave(text("Selenide: concise UI tests in Java"));
    }

}
