package nl.surfnet.coin.monitoring;

import com.sun.org.apache.xerces.internal.util.XMLCatalogResolver;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.net.URI;

public class TesterTest {

  private static final Logger LOG = LoggerFactory.getLogger(TesterTest.class);

  @Test
  public void runAll() {
    String conextDomain = "demo.openconext.org";
    new Tester(conextDomain, URI.create("http://localhost:8080")).runTests();
  }

  @Test
  public void metadata() {
    String conextDomain = "demo.openconext.org";
    new Tester(conextDomain, URI.create("http://localhost:8080")).metadata();
  }

  @Test
  public void loadSchemas() throws SAXException {
    InputStream schemaFile = getClass().getResourceAsStream("/schema/saml-schema-protocol-2.0.xsd");
    InputStream uiInfoSchemaFile = getClass().getResourceAsStream("/schema/sstc-saml-metadata-ui-v1.0.xsd");

    SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    LOG.debug("catalog: " + getClass().getResource("/schema-catalog.xml").toString());
    schemaFactory.setResourceResolver(new XMLCatalogResolver(new String[]{getClass().getResource("/schema-catalog.xml").toString()}));

    Schema schema = schemaFactory.newSchema(new Source[] {
            new StreamSource(schemaFile),
            new StreamSource(uiInfoSchemaFile)
    });
    schema.newValidator();
  }

  @Test
  public void validateLocalMetadata() throws Exception {
    new Tester("", URI.create("http://foobar")).validateXml(getClass().getResourceAsStream("/metadata.xml"));
  }
}
