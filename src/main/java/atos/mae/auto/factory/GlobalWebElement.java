package atos.mae.auto.factory;

import atos.mae.auto.action.Action;
import atos.mae.auto.model.StepModel;
import atos.mae.auto.plugins.requirement.StepReturn;


public class GlobalWebElement {

	private Action action;

	public void setAction(Action action){
		this.action = action;
	}


	public StepReturn openBrowser(StepModel sm){
		return this.action.openBrowser(sm.getData());
	}

	public StepReturn closeBrowser(){
		return this.action.closeBrowser();
	}

	public StepReturn navigate(StepModel sm){
		return this.action.navigate(sm.getData());
	}

	public StepReturn backupDatabase(StepModel sm){
		return this.action.backupDatabase(sm.getData());
	}

	public StepReturn restoreDatabase(StepModel sm){
		return this.action.restoreDatabase();
	}




	public StepReturn ssh(StepModel sm){
		return this.action.ssh(sm.getData(),sm.getPath());
	}

	public StepReturn soap(StepModel sm){
		return this.action.soap(sm.getData(),sm.getPath());
	}

	public StepReturn sqlSelect(StepModel sm){
		return this.action.sqlSelect(sm.getData());
	}

	public StepReturn sqlSelectAndStore(StepModel sm){
		return this.action.sqlSelectAndStore(sm.getData());
	}

	public StepReturn closeAlert(StepModel sm){
		return this.action.closeAlert(sm.getData());
	}

	public StepReturn verifyPageTitle(StepModel sm){
		return this.action.verifyPageTitle(sm.getData());
	}

	public StepReturn verifyTextOnPage(StepModel sm){
		return this.action.verifyTextOnPage(sm.getData());
	}
	
	public StepReturn switchScreen(StepModel sm){
		return this.action.switchScreen(sm.getData());
	}
	
	public StepReturn scroll(StepModel sm){
		return this.action.scroll(sm.getData());
	}
	
	public StepReturn storeTextBetweenDelimiters(StepModel sm){
		return this.action.storeTextBetweenDelimiters(sm.getData());
	}
	
	public StepReturn wait(StepModel sm) throws InterruptedException{
		return this.action.wait(sm.getData());
	}
	// code changed by Princi for WI - 54875
	public StepReturn compute(StepModel sm){
		return this.action.compute(sm.getData());
	}
	// end of code changed by Princi for WI - 54875
	
	// start code changed by Princi for WI - 76654
	public StepReturn clickSikuli(StepModel sm){
		return this.action.clickSikuli(sm.getData());
	}
	// end code changed by Princi for WI - 76654
	
	//start of code changed by Princi for WI - 76628
	public StepReturn displayValue(StepModel sm){
		return this.action.displayValue(sm.getData());
	}
	// end of code changed by Princi for WI - 76628
	
	// code changed by Princi for WI - 76643
		public StepReturn verifyValue(StepModel sm){
			return this.action.verifyValue(sm.getData());
		}
	// end of code changed by Princi for WI - 76643
}
