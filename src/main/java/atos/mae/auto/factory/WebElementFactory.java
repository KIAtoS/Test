package atos.mae.auto.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import atos.mae.auto.action.Action;
import atos.mae.auto.model.WebElementModel;
import atos.mae.auto.plugins.requirement.WebDriverProvider;

/**
 * Factory class used to generate instance of identifier model.
 */
@Component
public class WebElementFactory {

	@Autowired
	private WebDriverProvider webDriverProvider;

	@Autowired
	private Action action;

	/**
	 * Factory method.
	 * @param im Identifier model
	 * @return instance of identifier used to call method in step
	 */
	public Object MakeIdentifier(WebElementModel im){
		final WebElement imReturn = new WebElement(im);

		imReturn.setAction(this.action);
		imReturn.setWebDriverProvider(this.webDriverProvider);
		return imReturn;
	}
}
