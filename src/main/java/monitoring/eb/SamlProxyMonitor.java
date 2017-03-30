package monitoring.eb;

import monitoring.Monitor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static org.junit.Assert.assertTrue;

@Component
public class SamlProxyMonitor implements Monitor{

    private WebDriver driver;
    private String mujinaServiceProviderBaseUrl;
    private String idpEntityId;
    private String userName;

    public SamlProxyMonitor(@Value("${eb.mujina_sp_base_url}") String mujinaServiceProviderBaseUrl,
                            @Value("${eb.mujina_idp_entity_id}") String idpEntityId,
                            @Value("${eb.mujina_username}") String userName) {
        this.mujinaServiceProviderBaseUrl = mujinaServiceProviderBaseUrl;
        this.userName = userName;
        this.idpEntityId = idpEntityId;
        driver = new HtmlUnitDriver(false);

    }

    @Override
    public void monitor() throws Exception {
        driver.manage().deleteAllCookies();
        
        driver.get(mujinaServiceProviderBaseUrl + "/user.html");

        assertTrue("Expecting a WAYF. URL was: " + driver.getCurrentUrl(),
            driver.getPageSource().contains("Select an institution to login to the service"));

        WebElement login = driver.findElement(
            By.xpath(String.format("//input[@data-entityid=\"%s\"]",
            StringEscapeUtils.escapeXml11(idpEntityId))));
        login.click();

        driver.findElement(By.xpath("//input[@value=\"Submit\"]")).click();

        driver.findElement(By.name("username")).sendKeys(userName);
        driver.findElement(By.name("password")).sendKeys("secret");
        driver.findElement(By.xpath("//input[@value=\"Log in\"]")).submit();

        driver.findElement(By.xpath("//input[@value=\"Continue\"]")).click();

        //consent
        if (driver.getPageSource().contains("Yes, share this data")) {
            driver.findElement(By.id("accept_terms_button")).submit();
        }

        driver.findElement(By.xpath("//input[@value=\"Submit\"]")).click();

        String currentUrl = driver.getCurrentUrl();

        assertTrue("should be on SP, while current URL is: " + currentUrl,
            currentUrl.contains("/user.html"));
        assertTrue("Should contain j.doe@example.com",
            driver.getPageSource().contains("j.doe@example.com"));
    }
}
