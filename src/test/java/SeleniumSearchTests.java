import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SeleniumSearchTests {

    @Test
    void successfulSearchTest() {
        WebDriver driver = new ChromeDriver();

        driver.get("https://www.google.com/");

        WebElement searchBox = driver.findElement(By.name("q"));
        searchBox.sendKeys("selenide");
        searchBox.submit();

        WebElement searchResults = driver.findElement(By.id("search"));
        assertTrue(searchResults.getText().contains("https://selenide.org"), "Search result does not contain expected text.");
    }
}