import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;

public class LoginSeleniumTests {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(4));
        driver.manage().window().maximize();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void successLoginTest() {
        driver.get("https://niffler.qa.guru");
        driver.findElement(By.id("username")).sendKeys("stas");
        driver.findElement(By.id("password")).sendKeys("12345");
        driver.findElement(By.id("login-button")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("spendings")));
        WebElement spendings = driver.findElement(By.id("spendings"));
        Assertions.assertTrue(spendings.getText().contains("History of Spendings"));
    }

    @Test
    void successLoginWithPressEnterTest() {
        driver.get("https://niffler.qa.guru");
        driver.findElement(By.id("username")).sendKeys("stas");
        driver.findElement(By.id("password")).sendKeys("12345" + Keys.ENTER);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("spendings")));
        WebElement spendings = driver.findElement(By.id("spendings"));
        Assertions.assertTrue(spendings.getText().contains("History of Spendings"));
    }

    @Test
    void successLoginAsUserTest() {
        driver.get("https://niffler.qa.guru");
        driver.findElement(By.id("username")).sendKeys("stas");
        driver.findElement(By.id("password")).sendKeys("12345" + Keys.ENTER);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("spendings")));
        Assertions.assertTrue(driver.findElement(By.id("spendings")).getText().contains("History of Spendings"));

        driver.get("https://niffler.qa.guru/profile");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        String username = driver.findElement(By.id("username")).getAttribute("value");
        Assertions.assertEquals("stas", username);
    }

    @Test
    void wrongCredentialsTest() {
        driver.get("https://niffler.qa.guru");
        driver.findElement(By.id("username")).sendKeys("stas");
        driver.findElement(By.id("password")).sendKeys("wrong_pass" + Keys.ENTER);

        WebElement error = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".form__error")));
        Assertions.assertTrue(error.getText().contains("Bad credentials"));
    }
}