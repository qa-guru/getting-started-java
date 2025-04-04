import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Condition.text;
import static com.codeborne.selenide.Selenide.*;

public class YaSearchTests {
    @Test
    void successfulSearchTest() {
        open("https://ya.ru");
        $("[name=text]").setValue("selenide").pressEnter();
        $("[id=search-result]").shouldHave(text("selenide.org"));
    }
}
