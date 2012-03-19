package com.dianping.cat.message.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.dianping.cat.configuration.ClientConfigMerger;
import com.dianping.cat.configuration.ClientConfigValidator;
import com.dianping.cat.configuration.model.IEntity;
import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Server;
import com.dianping.cat.configuration.model.transform.DefaultXmlBuilder;
import com.dianping.cat.configuration.model.transform.DefaultXmlParser;
import com.site.helper.Files;

public class ConfigTest {
	@Test
	public void testClient() throws Exception {
		Config clientConfig = loadConfig("client-config.xml");
		Config globalConfig = loadConfig("global-config.xml");

		Assert.assertEquals("client", clientConfig.getMode());

		globalConfig.accept(new ClientConfigMerger(clientConfig));
		clientConfig.accept(new ClientConfigValidator());

		// System.out.println(clientConfig);

		List<Server> servers = clientConfig.getServers();

		Assert.assertEquals(3, servers.size());
		Assert.assertEquals(2280, servers.get(0).getPort().intValue());
		Assert.assertEquals(true, servers.get(0).isEnabled());
		Assert.assertEquals(2281, servers.get(1).getPort().intValue());
		Assert.assertEquals(false, servers.get(1).isEnabled());
		Assert.assertEquals(2280, servers.get(2).getPort().intValue());
		Assert.assertEquals(true, servers.get(2).isEnabled());
	}

	private Config loadConfig(String configXml) throws IOException, SAXException {
		InputStream in = getClass().getResourceAsStream(configXml);
		String xml = Files.forIO().readFrom(in, "utf-8");
		Config clientConfig = new DefaultXmlParser().parse(xml);
		return clientConfig;
	}

	@Test
	public void testConfig() throws Exception {
		DefaultXmlParser parser = new DefaultXmlParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("config.xml"), "utf-8");
		Config root = parser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(root);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
	}

	@Test
	@Ignore
	public void testSchema() throws Exception {
		// define the type of schema - we use W3C:
		String schemaLang = "http://www.w3.org/2001/XMLSchema";

		// get validation driver:
		SchemaFactory factory = SchemaFactory.newInstance(schemaLang);

		// create schema by reading it from an XSD file:
		String path = "/" + IEntity.class.getPackage().getName().replace('.', '/');
		Schema schema = factory.newSchema(new StreamSource(getClass().getResourceAsStream(path + "/config.xsd")));
		Validator validator = schema.newValidator();

		// at last perform validation:
		validator.validate(new StreamSource(getClass().getResourceAsStream("client.xml")));
		validator.validate(new StreamSource(getClass().getResourceAsStream("server.xml")));
		validator.validate(new StreamSource(getClass().getResourceAsStream("config.xml")));
	}

	@Test
	public void testServer() throws Exception {
		InputStream in = getClass().getResourceAsStream("server.xml");
		String xml = Files.forIO().readFrom(in, "utf-8");
		Config config = new DefaultXmlParser().parse(xml);

		Assert.assertEquals("server", config.getMode());
		Assert.assertEquals("192.168.8.21", config.getBind().getIp());
		Assert.assertEquals(2280, config.getBind().getPort());
	}
}
