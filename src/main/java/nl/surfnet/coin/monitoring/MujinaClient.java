/*
 * Copyright 2013 SURFnet bv, The Netherlands
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.surfnet.coin.monitoring;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.net.URI;

import static org.junit.Assert.assertTrue;

public class MujinaClient {
    private String contextPath = "/sp";
    private WebDriver driver;
    private final URI serverBaseUri;

    public MujinaClient(WebDriver driver, URI serverBaseUri, String contextPath) {
        this.driver = driver;
        this.serverBaseUri = serverBaseUri;
        this.contextPath = contextPath;
    }

    public void spHome() {

        driver.get(serverBaseUri + contextPath + "/index.jsp");
        assertTrue(driver.getCurrentUrl().contains(contextPath + "/"));
    }

    public void protectedPage() {
        driver.get(serverBaseUri + contextPath + "/user.jsp");
    }

    public void login(String user, String pass) {
        driver.findElement(By.name("j_username")).sendKeys(user);
        driver.findElement(By.name("j_password")).sendKeys(pass);
        driver.findElement(By.name("login")).submit(); // this is the form
    }

    public void deleteCookies() {
        driver.manage().deleteAllCookies();
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }
}
