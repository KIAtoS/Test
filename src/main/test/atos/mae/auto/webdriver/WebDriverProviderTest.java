package atos.mae.auto.webdriver;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import atos.mae.auto.plugins.requirement.WebDriverProvider;
import atos.mae.auto.utils.Exceptions.*;
import atos.mae.auto.utils.enums.EnvironnementExecutionEnum;

public class WebDriverProviderTest {

	@Autowired
	private WebDriverProvider webDriverProvider;

	/*
	 *
	 * OK
	 *
	 */

	@Test
	public void EnvExec_Local() {
		this.webDriverProvider.setEnvExec(1);
		EnvironnementExecutionEnum EEE = this.webDriverProvider.getEnvExec();
		Assert.assertEquals(EEE,EnvironnementExecutionEnum.LOCAL);
		this.webDriverProvider.setEnvExec(10);
		EEE = this.webDriverProvider.getEnvExec();
		Assert.assertEquals(EEE,EnvironnementExecutionEnum.LOCAL);
		this.webDriverProvider.setEnvExec(-9);
		EEE = this.webDriverProvider.getEnvExec();
		Assert.assertEquals(EEE,EnvironnementExecutionEnum.LOCAL);
	}

	@Test
	public void EnvExec_Remote() {
		this.webDriverProvider.setEnvExec(2);
		EnvironnementExecutionEnum EEE = this.webDriverProvider.getEnvExec();
		Assert.assertEquals(EEE,EnvironnementExecutionEnum.REMOTE);
	}

	@Test
	public void EnvExec_BrowserStack() {
		this.webDriverProvider.setEnvExec(3);
		EnvironnementExecutionEnum EEE = this.webDriverProvider.getEnvExec();
		Assert.assertEquals(EEE,EnvironnementExecutionEnum.BROWSERSTACK);

	}

	/*
	@Test
	public void getWebDriver_Local() {
		this.webDriverProvider.setEnvExec(1);
		this.webDriverProvider.setWebDriver("Mozilla");
		WebDriver wd = this.webDriverProvider.getWebDriver();
		Assert.assertTrue(wd instanceof FirefoxDriver);
		// closeDriver

	}

	@Test
	public void getWebDriver_Remote() {
		this.webDriverProvider.setEnvExec(2);
		String url = "http://";
		this.webDriverProvider.setUrlRemoteDriver(url);
		Assert.assertEquals(url,this.webDriverProvider.getUrlRemoteDriver());
		// getUrlRemoteDriver
		// setUrlRemoteDriver
		// closeDriver

	}




	@Test
	public void getJavascriptExecutor() {
		fail("Not yet implemented");
	}

	@Test
	public void getWait() {
		fail("Not yet implemented");
	}

	@Test
	public void Capabilities() {
		fail("Not yet implemented");
	}



	@Test
	public void takeScreenshot() {
		fail("Not yet implemented");
	}
	*/


	/*
	 *
	 * KO
	 *
	 */

	@Test(expected = NoDriverDefineException.class)
	public void WebDriver_NoDefinedDriver() {
		this.webDriverProvider.getWebDriver();
	}

	@Test(expected = NoDriverDefineException.class)
	public void JavascriptExecutor_NoDefinedDriver() {
		this.webDriverProvider.getJavascriptExecutor();
	}

	@Test(expected = NoDriverDefineException.class)
	public void getWait_NoDefinedDriver() {
		this.webDriverProvider.getWait();
	}

	@Test(expected = UrlRemoteDriverUndefined.class)
	public void UrlRemoteDriver_Undefined() {
		this.webDriverProvider.getUrlRemoteDriver();
	}


}
