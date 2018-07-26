package atos.mae.auto.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import atos.mae.auto.action.Action;

@Component
public class GlobalWebElementFactory {

	@Autowired
	private Action action;

	public Object MakeIdentifier(){
		final GlobalWebElement ei = new GlobalWebElement();
		ei.setAction(this.action);
		return ei;
	}
}
