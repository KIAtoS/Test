package atos.mae.auto.utils;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.util.ReflectionTestUtils;

import atos.mae.auto.factory.WebElementFactory;
import atos.mae.auto.model.WebElementModel;
import atos.mae.auto.model.WebElementModelList;
import atos.mae.auto.utils.Exceptions.checked.StoredVariableNotFound;
import static org.junit.Assert.*;

@ContextConfiguration(locations = { "file:src/main/resources/config.xml" })
public class ListeTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private Liste liste;

	@Mock
	private WebElementFactory webElementFactoryMock;

	@Mock
	private Report report;

	@Before
	public void setUp(){
		//when(this.webElementFactoryMock.MakeIdentifier(any(WebElementModel.class))).thenReturn(wem);
		ReflectionTestUtils.setField(this.liste, "report", this.report);
	}

	@Test
	public void testCheckStoredVariable() throws StoredVariableNotFound{
		//Arrange
		this.liste.getStoredValues().put("varTest", "varTestResponse");
		String data = "ex ${varTest} ex";
		String dataAssert = "ex varTestResponse ex";
		//when(this.webElementFactoryMock.MakeIdentifier(any(WebElementModel.class))).thenReturn(wem);
		//ReflectionTestUtils.setField(this.liste, "webElementFactory", this.webElementFactoryMock);

		//Act
		final String response = this.liste.CheckStoredVariable(data);

		//Assert
		assertEquals(response,dataAssert);
	}

	@Test(expected=StoredVariableNotFound.class)
	public void testCheckStoredVariable_StoredValueNotFound() throws StoredVariableNotFound{
		//Arrange
		String data = "ex ${varTest} ex";
		this.liste.getStoredValues().remove("varTest");

		//Act
		final String response = this.liste.CheckStoredVariable(data);
	}

	@Test
	public void testCheckStoredVariable_DataNull() throws StoredVariableNotFound{
		//Arrange
		String data = null;
		//when(this.webElementFactoryMock.MakeIdentifier(any(WebElementModel.class))).thenReturn(wem);
		//ReflectionTestUtils.setField(this.liste, "webElementFactory", this.webElementFactoryMock);

		//Act
		final String response = this.liste.CheckStoredVariable(data);

		//Assert
		assertNull(response);
	}

	@Test
	public void Identifiers() {
		final WebElementModel IM1 = new WebElementModel();
		IM1.setObjectName("IM1");
		IM1.setXPath("XPath1");
		final WebElementModel IM2 = new WebElementModel();
		IM2.setObjectName("IM2");
		IM2.setXPath("XPath2");
		final WebElementModel[] IMtab = {IM1,IM2};
		final WebElementModelList IMList = new WebElementModelList();
		IMList.List = IMtab;
		final WebElementModelList[] IMLList = {IMList};
		final WebElementModelList Base = new WebElementModelList();
		Base.Base = IMLList;
		this.liste.setIdentifiers(Base);
		assertEquals("XPath1",((WebElementModel)this.liste.getIdentifiers().get("IM1")).getXPath());
		assertEquals("XPath2",((WebElementModel)this.liste.getIdentifiers().get("IM2")).getXPath());
	}





}
