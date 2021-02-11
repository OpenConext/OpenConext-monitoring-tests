package monitoring.eb;

import monitoring.Monitor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@Component
public class SamlProxyMonitor implements Monitor {

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
    }

    @Override
    public void monitor() {
        try {
            doMonitor();
        } catch (RuntimeException e) {
            LOG.warn("Exception occurred. Current page is {} and pageSource is {}", driver.getCurrentUrl(), driver
                    .getPageSource());
            throw e;
        }
    }

    private void doMonitor() {
        driver = new HtmlUnitDriver(false);
        driver.manage().deleteAllCookies();
        driver.manage().timeouts()
                .implicitlyWait(5, TimeUnit.MINUTES)
                .pageLoadTimeout(5, TimeUnit.MINUTES)
                .setScriptTimeout(5, TimeUnit.MINUTES);

        driver.get(mujinaServiceProviderBaseUrl + "/user.html");

        boolean wayf = driver.getPageSource().contains("Select an institution to login");
        if (wayf) {
            String escapedId = StringEscapeUtils.escapeXml11(idpEntityId);
            String xpath = String.format("//input[@value=\"%s\"]/parent::form//button", escapedId);
            WebElement button = driver.findElement(By.xpath(xpath));
            button.click();
        }

        WebElement form = driver.findElement(By.xpath("//form"));
        form.submit();

        driver.findElement(By.name("username")).sendKeys(userName);
        driver.findElement(By.name("password")).sendKeys("secret");
        driver.findElement(By.xpath("//input[@value=\"Log in\"]")).submit();

        driver.findElement(By.xpath("//input[@value=\"Continue\"]")).click();

        //consent
        if (driver.getPageSource().contains("Review your information")) {
            form = driver.findElement(By.xpath("//form"));
            form.submit();
        }
        form = driver.findElement(By.xpath("//form"));
        form.submit();

        //force the time-out to wait for the page load
        driver.findElement(By.className("attributes"));

        String pageSource = driver.getPageSource();
        assertTrue(String.format("Page should contain %s j.doe@example.com", pageSource),
                pageSource.contains("j.doe@example.com"));
    }
}
