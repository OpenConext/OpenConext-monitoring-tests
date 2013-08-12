package nl.surfnet.coin.monitoring;

import com.sun.org.apache.xerces.internal.util.XMLCatalogResolver;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.net.URI;

import static org.junit.Assert.assertTrue;

public class Tester {

  private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Tester.class);
  private final URI serverBaseUri;
  private WebDriver driver;

  private Mujina mujina;
  private Engineblock engineblock;

  public Tester(String conextDomain, URI serverBaseUri) {

//    this.driver = new FirefoxDriver();
    this.serverBaseUri = serverBaseUri;
    mujina = new Mujina(driver, serverBaseUri);
    engineblock = new Engineblock("https://engine." + conextDomain);
  }

  public void runTests() {

    loginFlow();
//    restartBrowser();
    metadata();

    driver.quit();
  }

  public void restartBrowser() {
    driver.quit();
    driver = new FirefoxDriver();
    mujina.setDriver(driver);
  }

  public void metadata() {
    InputStream metadata = engineblock.getMetadata();
    try {

      // First validate using basic schema validation
      validateXml(metadata);

      //


    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   *
   * Validate the XML using the (local) schemata for basic SAML and the metadata extensions.
   *
   * @param metadata the metadata inputstream
   */
  public void validateXml(InputStream metadata) throws Exception {
    InputStream schemaFile = getClass().getResourceAsStream("/schema/saml-schema-protocol-2.0.xsd");
    InputStream uiInfoSchemaFile = getClass().getResourceAsStream("/schema/sstc-saml-metadata-ui-v1.0.xsd");

    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    schemaFactory.setResourceResolver(new XMLCatalogResolver(new String[]{getClass().getResource("/schema-catalog.xml").toString()}));

    Schema schema = schemaFactory.newSchema(new Source[] {
            new StreamSource(schemaFile),
            new StreamSource(uiInfoSchemaFile)
    });
    Validator validator = schema.newValidator();
    try {

      validator.validate(new StreamSource(metadata));
    } catch (SAXException e) {
      LOG.info("XML is NOT valid");
      throw new RuntimeException(e);
    }
  }

  public void loginFlow() {
    mujina.spHome();

    LOG.debug("url: {}", driver.getCurrentUrl());

    mujina.protectedPage();

    assertTrue(driver.getCurrentUrl().contains("/idp/"));

    mujina.login("monitor-user", "somepass");

    assertTrue(driver.getCurrentUrl().contains("/sp/"));
    assertTrue(driver.getPageSource().contains("John Doe"));

  }

}
