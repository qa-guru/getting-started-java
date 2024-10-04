package guru.qa.niffler;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SeleniumLoginTests {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void successfulLoginTest() {
        driver.get("https://niffler.qa.guru");

        WebElement usernameInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("[name='username']")));
        usernameInput.sendKeys("qaguru31");

        WebElement passwordInput = driver.findElement(By.cssSelector("[name='password']"));
        passwordInput.sendKeys("1234");

        WebElement submitButton = driver.findElement(By.cssSelector("[class='form__submit']"));
        submitButton.click();

        WebElement searchResults = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("stat")));
        assertEquals("Statistics", searchResults.getText());

        WebElement spendingsResults = driver.findElement(By.id("spendings"));
        assertEquals("History of Spendings", spendingsResults.getText());
    }

    @Test
    void successfulLoginWithPressEnterTest() {
        driver.get("https://niffler.qa.guru");

        WebElement usernameInput = driver.findElement(By.cssSelector("[name='username']"));
        usernameInput.sendKeys("qaguru31");

        WebElement passwordInput = driver.findElement(By.cssSelector("[name='password']"));
        passwordInput.sendKeys("1234" + Keys.ENTER);

        WebElement statElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat")));
        assertEquals("Statistics", statElement.getText());

        WebElement spendingsElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("spendings")));
        assertEquals("History of Spendings", spendingsElement.getText());
    }

    @Test
    void successfulLoginWithUsernameCheckTest() {
        driver.get("https://niffler.qa.guru");

        WebElement usernameInput = driver.findElement(By.cssSelector("[name='username']"));
        usernameInput.sendKeys("qaguru31");

        WebElement passwordInput = driver.findElement(By.cssSelector("[name='password']"));
        passwordInput.sendKeys("1234" + Keys.ENTER);

        WebElement statElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("stat")));
        assertEquals("Statistics", statElement.getText());

        WebElement spendingsElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("spendings")));
        assertEquals("History of Spendings", spendingsElement.getText());

        driver.get("https://niffler.qa.guru/profile");

        WebElement usernameProfile = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        assertEquals("qaguru31", usernameProfile.getAttribute("value"));
    }

    @Test
    void wrongCredentialsLoginTest() {
        driver.get("https://niffler.qa.guru");

        WebElement usernameInput = driver.findElement(By.cssSelector("[name='username']"));
        usernameInput.sendKeys("qaguru31");

        WebElement passwordInput = driver.findElement(By.cssSelector("[name='password']"));
        passwordInput.sendKeys("1234dsfbgsdghdfstgh");

        WebElement submitButton = driver.findElement(By.cssSelector("[class='form__submit']"));
        submitButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form__error")));
        assertEquals("Неверные учетные данные пользователя", errorElement.getText());
    }

    @Test
    @Disabled("Тест отключен до исправления")
    void emptyLoginTest() {
        driver.get("https://niffler.qa.guru");

        WebElement passwordInput = driver.findElement(By.cssSelector("[name='password']"));
        passwordInput.sendKeys("1234dsfbgsdghdfstgh");

        WebElement submitButton = driver.findElement(By.cssSelector("[class='form__submit']"));
        submitButton.click();

        WebElement errorElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form__error")));
        assertEquals("", errorElement.getText());
    }
}