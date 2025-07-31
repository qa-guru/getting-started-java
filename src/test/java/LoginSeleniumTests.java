import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginSeleniumTests {

    @Test
    void successfulLoginTest() {
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get("https://niffler.qa.guru");

        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement loginButton = driver.findElement(By.id("login-button"));

        usernameInput.sendKeys("stas");
        passwordInput.sendKeys("12345");
        loginButton.click();

        WebElement spendingsHeader = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("spendings")));
        String headerText = spendingsHeader.getText();

        assertTrue(headerText.contains("History of Spendings"), "Expected text not found!");

        driver.quit();
    }
}