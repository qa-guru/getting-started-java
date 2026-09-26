import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SeleniumSearchTests {

    @Test
    void successfulSearchTest() {
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

        driver.get("https://github.com/search");
        driver.findElement(By.cssSelector("[aria-label='Search GitHub']"))
                .sendKeys("qa.guru", Keys.RETURN);

        WebElement searchResults = driver.findElement(By.cssSelector("[data-testid='results-list']"));
        assertTrue(searchResults.getText().contains("QA.GURU"));

        driver.quit();
    }
}
