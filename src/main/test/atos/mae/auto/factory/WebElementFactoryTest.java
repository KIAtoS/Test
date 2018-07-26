package atos.mae.auto.factory;

import static org.junit.Assert.*;
import atos.mae.auto.model.WebElementModel;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations = { "file:src/main/resources/config.xml" })
public class WebElementFactoryTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private WebElementFactory identifierFactory;

	@Test
	public void Button() {
		final WebElementModel im = new WebElementModel();
		im.setObjectName("IM1");
		final Object button = this.identifierFactory.MakeIdentifier(im);
		assertTrue(button instanceof WebElement);
	}



}
