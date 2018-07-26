package atos.mae.auto.excel;

import org.junit.Before;
import org.mockito.Spy;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import atos.mae.auto.utils.Liste;

@ContextConfiguration(locations = { "file:src/main/resources/config.xml" })
public class TestStepLoaderTest extends AbstractJUnit4SpringContextTests{

	@Spy
	private TestStepLoader tslMock;

	@Spy
	private Liste listeMock;

	@Before
	public void Before(){
		this.setApplicationContext( new ClassPathXmlApplicationContext("config.xml"));
		//ReflectionTestUtils.setField(myObject, "onekey", "one");
	}
}
