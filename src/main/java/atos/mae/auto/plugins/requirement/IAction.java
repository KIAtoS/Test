package atos.mae.auto.plugins.requirement;


public interface IAction {

	/**
	 * Enable main jar to give webDriver to plugin.
	 * @param webDriver WebDriver use to interact with selenium
	 */
	public void setWebDriver(WebDriverProvider webDriver);
}
