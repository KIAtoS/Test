package atos.mae.auto.factory;


import atos.mae.auto.model.WebElementModel;
import atos.mae.auto.model.StepModel;
import atos.mae.auto.plugins.requirement.StepReturn;
import atos.mae.auto.plugins.requirement.StepReturnEnum;

public class WebElement extends WebElementModel {

	public WebElement (WebElementModel im){
		super(im);
	}

	public StepReturn click(){
		return this.action.click(this);
	}



	public StepReturn hover(){
		return this.action.hover(this);
	}

	public StepReturn selectByLabel(StepModel sm){
		return this.action.selectByLabel(this, sm.getData());
	}

	public StepReturn selectByValue(StepModel sm){
		return this.action.selectByValue(this, sm.getData());
	}

	public StepReturn setText(StepModel sm){
		return this.action.setText(this, sm.getData());
	}

	public StepReturn clear(){
		return this.action.clear(this);
	}


	public StepReturn storeAttribut(StepModel sm){
		final String[] dataSplited = sm.getData().split("\\|");
		if(dataSplited.length != 2)
			return new StepReturn(StepReturnEnum.ERROR, "Expected : 2 arguments separated by a pipe ('|').<br/>Actual : " + dataSplited.length + " arguments found.");

		return this.action.storeAttribut(this, dataSplited[0], dataSplited[1] );
	}

	public StepReturn storeChecked(StepModel sm){
		return this.action.storeChecked(this, sm.getData());
	}

	public StepReturn storeBodyText(StepModel sm){
		return this.action.storeBodyText(this, sm.getData());
	}

	public StepReturn storeSelectedLabels(StepModel sm){
		return this.action.storeSelectedLabels(this, sm.getData());
	}

	public StepReturn storeSelectedValues(StepModel sm){
		return this.action.storeSelectedValues(this, sm.getData());
	}

	public StepReturn storeTextInTable(StepModel sm){
		final String[] dataSplited = sm.getData().split("\\|");
		if(dataSplited.length != 3)
			return new StepReturn(StepReturnEnum.ERROR, "Expected : 3 arguments separated by a pipe ('|').<br/>Actual : " + dataSplited.length + " arguments found.");

		int iRow, iCol;
		try{
			iRow = Integer.parseInt(dataSplited[1]);
			iCol = Integer.parseInt(dataSplited[2]);
		}catch(NumberFormatException e){
			return new StepReturn(StepReturnEnum.ERROR,"Expected : 3 arguments : the variable name where to store value (string), the row index (number) and the column index (number).<br/>Actual : " + sm.getData());
		}
		return this.action.storeTextInTable(this, dataSplited[0], iRow, iCol);
	}





	public StepReturn verifySelectedValue(StepModel sm){
		return this.action.verifySelectedValue(this, sm.getData());
	}

	public StepReturn verifySelectedLabel(StepModel sm){
		return this.action.verifySelectedLabel(this, sm.getData());
	}

	public StepReturn verifyElementInTable(StepModel sm){
		return this.action.verifyElementInTable(this, sm.getData());
	}

	// start code change by Cyril for 36016
	public StepReturn verifyTextFormat(StepModel sm){
		return this.action.verifyTextFormat(this, sm.getData());
	}	
	// end code change by Cyril for 36016
	
	public StepReturn verifyTextInObject(StepModel sm){
		return this.action.verifyTextInObject(this, sm.getData());
	}


	public StepReturn verifyChecked(StepModel sm){
		return this.action.verifyChecked(this, sm.getData());
	}
	
	
	// code change by Vivek for 29005
	public StepReturn verifyElementExist(StepModel sm){
		return this.action.verifyElementExist(this, sm.getData());
	}
	
	// END : code change by Vivek for 29005

	//public StepReturn verifyElementExist(StepModel sm){
		//return this.action.verifyElementExist(this);
	//}
	
	
	// code change by Vivek for 55317
	
	public StepReturn getIndexInTable(StepModel sm){
		return this.action.getIndexInTable(this, sm.getData());
	}
	
	// code change by Vivek for 55317
	
	// code change by Vivek for 57107
	
		public StepReturn getCoordinatesInTable(StepModel sm){
			return this.action.getCoordinatesInTable(this, sm.getData());
		}
		
	// code change by Vivek for 57107

	public StepReturn verifyAttribute(StepModel sm){
		return this.action.verifyAttribute(this, sm.getData());
	}

	public StepReturn verifyVisible(StepModel sm){
		return this.action.verifyVisible(this, sm.getData());
	}
	
	public StepReturn getElementByLabelAndClick(StepModel sm){
		return this.action.getElementByLabelAndClick(this, sm.getData());
	}
	
	//start of code changed by Princi for WI - 76628
		public StepReturn displayValue(StepModel sm){			
			return this.action.displayValue(this);			
		}
		// end of code changed by Princi for WI - 76628
		
		// start code changed by Princi for WI -76630 
		public StepReturn getPixelCoordinate(StepModel sm){
			return this.action.getPixelCoordinate(this, sm.getData());
		}
		// end code changed by Princi for WI -76630
		
		 // start code changed by Princi for WI -38836 
		public StepReturn verifyValueInTable(StepModel sm){
			return this.action.verifyValueInTable(this, sm.getData());
		}
// end code changed by Princi for WI -38836 
}
