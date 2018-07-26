package atos.mae.auto.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import atos.mae.auto.hpalm.HpAlm;
import atos.mae.auto.model.DataColumnModel;
import atos.mae.auto.model.StepModel;
import atos.mae.auto.model.TestStepModel;
import atos.mae.auto.model.WebElementModel;
import atos.mae.auto.plugins.requirement.StepReturn;
import atos.mae.auto.plugins.requirement.StepReturnEnum;
import atos.mae.auto.plugins.requirement.WebDriverProvider;
import atos.mae.auto.utils.ExcelUtils;
import atos.mae.auto.utils.Report;
import atos.mae.auto.utils.Exceptions.checked.AbortException;

@Component
public class TestStepReporting {

	/**
	 * Logger.
	 */
	private static Logger Log = LoggerFactory.getLogger(TestStepReporting.class);

	@Autowired
	private Report report;

	@Autowired
	private HpAlm hpAlm;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private WebDriverProvider webDriverProvider;

	@Value("${timingForDemo:0}")
	private int timingForDemo;

	@Value("${borderSizeForScreenshot:5}")
	private int borderSizeForScreenshot;


	/**
	 * reporting of testStep
	 * @param tsm TestStepModel
	 * @param sm StepModel
	 * @param sr StepReturn 
	 * @param testStepsSheet sheet of step
	 * @param screenCapturePath screen capture path
	 * @throws AbortException return exception if the name is not found
	 */
	public void report(TestStepModel tsm, StepModel sm, StepReturn sr, XSSFSheet testStepsSheet,String screenCapturePath) throws AbortException{
		// reporting html
		if(sm.getModuleCall() != null && sr.getStepReturn() == StepReturnEnum.PASS)
			this.ManageReporting(new StepReturn(StepReturnEnum.DONOTLOG), sm, tsm, testStepsSheet,screenCapturePath);
		else
			this.ManageReporting(sr, sm, tsm, testStepsSheet, screenCapturePath);
	}

	/**
	 * Manage reporting
	 * @param sr StepReturn
	 * @param sm StepModel
	 * @param tsm TestStepModel
	 * @param testStepsSheet Sheet of step
	 * @param screenCapturePath screen capture path
	 * @throws AbortException error if step name not found
	 */
	private void ManageReporting(StepReturn sr, StepModel sm, TestStepModel tsm, XSSFSheet testStepsSheet,String screenCapturePath) throws AbortException {
		sm.setStepReturn(sr);
        if(sr.getStepReturn() != StepReturnEnum.PASS && sr.getStepReturn() != StepReturnEnum.DONOTLOG){
        	if (sr.getStepReturn() == StepReturnEnum.WARN || !sm.isAbortOnFail())
        		tsm.setGlobalSepReturn(StepReturnEnum.WARN);
        	else
        		tsm.setGlobalSepReturn(StepReturnEnum.FAIL);
        }

        double ExcelReturn;

        //Log.debug("return code is : " + sr.getStepReturn().toString());
        //Log.debug("I'am a module : " + tsm.isModule());
        if(!tsm.isModule()){
        	switch(sr.getStepReturn()){
        	case PASS:
        		ExcelReturn = 1;
        		break;
        	case DONOTLOG:
        		ExcelReturn = 1;
        		break;        		
        	case WARN:
        		ExcelReturn = 2;
        		break;
        	default: // FAILED
        		ExcelReturn = 3;
        		break;
        	}

        	//Reporting excel
            //final Cell cell = sm.getCellReporting();
        	final Cell cell = this.excelUtils.getCellByRowCol(testStepsSheet, sm.getRow(), sm.getCol());
        	//Log.debug("Cell row : " + cell.getRowIndex());
            cell.setCellType(Cell.CELL_TYPE_NUMERIC);
            cell.setCellValue(ExcelReturn);
        }

        //Reporting html
        this.report.setStep(sr, sm.getName(), sm.getStepDescription(), sm.isAbortOnFail(), screenCapturePath);

        //Reporting hpalm
        this.hpAlm.addRunStep(sm);



    	if (sr.isFail() && sm.isAbortOnFail()) {
    		throw new AbortException();
    	}
	}

	/**
	 * add border around element 
	 * @param sm StepModel
	 * @return border
	 */
	public String addBorderForScreenShot(StepModel sm) {
		if(sm.getWebObject() instanceof WebElementModel){
			final WebElement we = ((WebElementModel)sm.getWebObject()).getWebElement();
			if(we == null){
				Log.warn("WebElement not found to add border for ScreenShot");
				return "";
			}
			try{
				final String border = we.getCssValue("border");
				//scroll to the webelement where we add border : http://stackoverflow.com/questions/3401343/scroll-element-into-view-with-selenium
				this.webDriverProvider.getJavascriptExecutor().executeScript("arguments[0].scrollIntoView(true);", we);
				this.webDriverProvider.getJavascriptExecutor().executeScript("arguments[0].style.border='" + this.borderSizeForScreenshot + "px solid red'", we);
				return border;
			}catch(StaleElementReferenceException e){
				Log.error("",e);
			}

		}

		return "";
	}

	/**
	 * remove border around element
	 * @param sm StepModel
	 * @param border border
	 */
	public void removeBorderForScreenShot(StepModel sm, String border) {
		if(sm.getWebObject() instanceof WebElementModel){
			final WebElement we = ((WebElementModel)sm.getWebObject()).getWebElement();
			if(we == null)
				return;
			try{
				this.webDriverProvider.getJavascriptExecutor().executeScript("arguments[0].style.border='" + border + "'", we);
			}catch(StaleElementReferenceException e){
				Log.error("",e);
			}
		}
	}

	/**
	 * Take ScreenShot for the test
	 * @param screenCapture if we want to screen
	 * @param stepName Step of name
	 * @return name of screenShot
	 */
	public String takeScreenShot(boolean screenCapture, String stepName){
	 	//Wait For Demo
        try {
			Thread.sleep(this.timingForDemo);
		} catch (InterruptedException e) {
			Log.error("",e);
		}

		final String screenPath = this.report.takeScreenShot(screenCapture, stepName);

		return screenPath;
	}

	public void startTest(TestStepModel tsm, DataColumnModel dcm){
		this.report.startTest(tsm.getTestName() + " - " + dcm.getDataColName(), tsm.getTestDescription());
	}

	public void endTest(TestStepModel tsm){
		if(!tsm.isModule())
			this.report.endtest();
	}
}
