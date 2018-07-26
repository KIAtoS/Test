package atos.mae.auto.excel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.UnhandledAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.relevantcodes.extentreports.LogStatus;

import atos.mae.auto.factory.WebElement;
import atos.mae.auto.model.DataColumnModel;
import atos.mae.auto.model.WebElementModel;
import atos.mae.auto.model.StepModel;
import atos.mae.auto.model.TestStepModel;
import atos.mae.auto.plugins.requirement.StepReturn;
import atos.mae.auto.plugins.requirement.StepReturnEnum;
import atos.mae.auto.plugins.requirement.WebDriverProvider;
import atos.mae.auto.utils.ExcelUtils;
import atos.mae.auto.utils.Liste;
import atos.mae.auto.utils.Exceptions.ActionUndefined;
import atos.mae.auto.utils.Exceptions.DriverNotExistException;
import atos.mae.auto.utils.Exceptions.NoDriverDefineException;
import atos.mae.auto.utils.Exceptions.StepNameUndefined;
import atos.mae.auto.utils.Exceptions.StepReturnUndefined;
import atos.mae.auto.utils.Exceptions.TestNameUndefined;
import atos.mae.auto.utils.Exceptions.TypeNotFoundException;
import atos.mae.auto.utils.Exceptions.TypeUndefined;
import atos.mae.auto.utils.Exceptions.UrlRemoteDriverUndefined;
import atos.mae.auto.utils.Exceptions.checked.AbortException;
import atos.mae.auto.utils.Exceptions.checked.StoredVariableNotFound;

@Component
public class TestStepService {
	/**
	 * Logger.
	 */
	private static Logger Log = LoggerFactory.getLogger(TestStepService.class);

	@Value("${testLibraryPath:TestLibrary}")
	private String testLibraryPath;

	@Value("${testStepSheetName:TestSteps}")
	private String testStepSheetName;

	@Value("${timingBetweenStep:250}")
	private int timingBetweenStep;

	@Autowired
	private Liste liste;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private WebDriverProvider webDriverProvider;

	@Autowired
	private TestStepReporting testStepReporting;	
	
	/*@Autowired
	private StepModel stepModel;*/
	
	
	/**
	 * List to contain data column result.
	 */
	private List<Integer> dataList = null; //code changed by Princi for WI 57182	
	/**
	 * Map to store the result of test step on the bases of data column.
	 */
	private HashMap<String,List<Integer>> testStepStoredMap = new HashMap<String,List<Integer>>();
	
	/**
	 * Execute test step model to loop Datas (Data1, Data2, etc...)
	 * @param tsm
	 * @return
	 */
	public StepReturnEnum execute(TestStepModel tsm){
		String stepResult="";	 // code changed by Princi for 57182
		dataList = new ArrayList<Integer>(); // code changed by Princi for 57182
		tsm.setGlobalSepReturn(StepReturnEnum.PASS);

		String actionName;
		FileInputStream testStepsFIS;
        Workbook wb;
                
        try {
        	testStepsFIS = new FileInputStream(tsm.getFile());
        	wb = new XSSFWorkbook(testStepsFIS);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
        final XSSFSheet testStepsSheet = (XSSFSheet) wb.getSheet(this.testStepSheetName);

		for (final DataColumnModel dcm : tsm.getDataColumnList()) {	
			int storedResult=0;  // changed by Princi for 57182			
			this.init(tsm, testStepsSheet, dcm);
			try{
				for (final StepModel sm : dcm.getStepList()) {
					StepReturn sr;
					try{

						try {
							Thread.sleep(this.timingBetweenStep);
						} catch (InterruptedException e) {
							Log.error("",e);
						}

						// Try to found WebElement on page if IdentifierModel, not if EmptyIdentifier only when action name is not VerifyElementExist
						
						// code change by Vivek for 29005
						
						
						if (sm.getMethodCall() != null) {
							if (sm.getMethodCall().getName() == null) {
								actionName = "";
								Log.info("Unknow Action Name in Excel Template");
							}	
							else {
								actionName = sm.getMethodCall().getName();
								Log.info("Test Step Action Name : " + sm.getMethodCall().getName());
							}
						}
						else
							actionName = "Module";
						
						// Verify if we need to ignore step execution (sm.getData() = "null"   by CPD
						if (sm.getData() != null)
							if (sm.getData().equalsIgnoreCase("null"))
							{
								if (sm.getName() == null) {
										Log.info("Step ignored : because data = 'null'");
								}		
								else {
										Log.info("Step '" + sm.getName() + "' : ignored : because data = 'null'");
								}	
								sm.setStepReturn(new StepReturn(StepReturnEnum.DONOTLOG));
							}
						
						//TODO erreur à l'utilisation des modules qui n'ont pas de getMethodCall
						if(actionName.equals("verifyElementExist"))
						{
							sm.setStepReturn(new StepReturn(StepReturnEnum.PASS));
							//Fix no such element issue when VerifyElementExist is used with multi Data column
							WebElement we = (WebElement)sm.getWebObject();
							we.setWebElement(null);
							we.setWebElements(null);
							sm.setWebObject(we);
						}
						
						// code changed by Princi for WI - 54875
						if(actionName.equals("compute"))
						{
							sm.setStepReturn(new StepReturn(StepReturnEnum.PASS));	
						}
						// end of code changed by Princi for WI - 54875
						
						// code changed by Princi for WI - 76628
						if(actionName.equals("displayValue"))
						{
							sm.setStepReturn(new StepReturn(StepReturnEnum.PASS));	
						}
						// end of code changed by Princi for WI - 76628
						
						else
						{
							
							// END :code change by Vivek for 29005
							
							if (sm.getStepReturn().getStepReturn() == null && sm.getWebObject() instanceof WebElementModel) {
								Log.info("No Return Status and webelement identified");
								sr = ((WebElementModel)sm.getWebObject()).tryGetWebElement(sm.getDefaultIndex());
								if(sr.getStepReturn() != StepReturnEnum.PASS){
									sm.setStepReturn(sr);
								}
							}
						}	

						final String border = this.testStepReporting.addBorderForScreenShot(sm);
						final String screenCapturePath = this.testStepReporting.takeScreenShot(sm.isScreenCaptureNeeded(), sm.getName());
						this.testStepReporting.removeBorderForScreenShot(sm, border);

						if(sm.getStepReturn().getStepReturn() == null || sm.getStepReturn().getStepReturn() == StepReturnEnum.PASS || sm.getStepReturn().getStepReturn() == StepReturnEnum.WARN || sm.getStepReturn().getStepReturn() == StepReturnEnum.FAIL)
							sr = this.executeStep(sm,tsm.isModule());
						else{
							sr = sm.getStepReturn();
						}
						this.testStepReporting.report(tsm, sm, sr, testStepsSheet, screenCapturePath);

					}catch(RuntimeException e){
						sr = new StepReturn();
						sr.setStepReturn(StepReturnEnum.ERROR);
						stepResult =LogStatus.ERROR.toString().toUpperCase();
						sr.setException(e);
						Log.error("Exception Raised in step "+sm.getName()+"    "+e);
						this.testStepReporting.report(tsm, sm, sr, testStepsSheet,null);						
						//throw e;
					}
					// code changed by Princi for 57182
					Log.info("Step Return value --> "+sr.getStepReturn());											
					switch (sr.getStepReturn()) {
					case PASS:
						stepResult = LogStatus.PASS.toString().toUpperCase();
						break;				
					
					case FAIL:
						if(sm.isAbortOnFail())
							stepResult = LogStatus.FAIL.toString().toUpperCase();
						else
							stepResult = LogStatus.WARNING.toString().toUpperCase();
						break;
						
					case ERROR:
						stepResult = LogStatus.ERROR.toString().toUpperCase();
						break;
						
					default:
						stepResult = LogStatus.UNKNOWN.toString().toUpperCase();
						break;
					}	
					
					Log.info("Step Result --> "+stepResult);
			          if("PASS".equals(stepResult) && storedResult<2)
			          {
			        	  storedResult = 1;
			          }
			          else if("FAIL".equals(stepResult)){
			        	  storedResult = 3;
			        	  break;
			          }
			          else if("WARNING".equals(stepResult) && storedResult<3){        		
			        		storedResult = 2;        	   
			        	}
			          else if("ERROR".equals(stepResult))
			          {
			        	  storedResult = 3;
			          }	         
					Log.info("Stored Result value for Test Set--> "+storedResult);									
					//end of code changed by Princi for 57182
				}				
					//this.webDriverProvider.closeWebDriver();
			
				
			}catch(AbortException | NoDriverDefineException e){
	    		Log.info("Blocking step, end of Test");
			}catch(Exception e)
			{	Log.info("Step Result in catch --> "+stepResult);			
				if(stepResult =="ERROR")
				  storedResult = 3;	
				Log.error("Exception raised --> "+e);
			}			
			this.testStepReporting.endTest(tsm);
			dataList.add(storedResult);	 // code changed by Princi for 57182
			Log.info("After end test ");			
		}
		
		// code changed by Princi for 57182
		Log.info("Data List Size --> "+dataList.size());
		Log.info("Data List --> "+dataList);
		testStepStoredMap.put(tsm.getTestName(), dataList);
		setTestStepStoredMap(testStepStoredMap);		
		//end of code changed by Princi for 57182
		try {
	        testStepsFIS.close();
	        if(!tsm.isModule()){
	        	Log.info("file output --> "+tsm.getFile());
	        	final FileOutputStream testStepsFOS = new FileOutputStream(tsm.getFile());	        	
	        	wb.write(testStepsFOS);
	        	testStepsFOS.close();
	        }
	        wb.close();
        } catch (IOException e) {
			throw new RuntimeException(e);
		} 

		return tsm.getGlobalSepReturn();

	}


	/**
	 * Execute a Step
	 * @param sm stepModel
	 * @param isModule if it is a module
	 * @return stepReturn
	 */
	private StepReturn executeStep(StepModel sm,boolean isModule) {


		try {
			sm.setData(this.liste.CheckStoredVariable(sm.getData()));
		} catch (StoredVariableNotFound e) {
			return new StepReturn(StepReturnEnum.FAIL,e);
		}

		if(sm.getMethodCall() == null){
			return new StepReturn(this.execute(sm.getModuleCall()));
		}else{
			return this.CallMethod(sm);
		}
	}

	/**
	 * Init test.
	 */
	private void init(TestStepModel tsm, XSSFSheet testStepsSheet, DataColumnModel dcm){
		tsm.setGlobalSepReturn(StepReturnEnum.PASS);
    	if(!tsm.isModule()){
    		this.webDriverProvider.leaveDriver();
    		this.testStepReporting.startTest(tsm, dcm);
    		this.excelUtils.clearRange(testStepsSheet, dcm.getDataColIndex());
    	}
	}


	/**
	 * Call method of row 
	 * @param sm Step Model
	 * @return StepReturn
	 */
	private StepReturn CallMethod(StepModel sm){
		try {
	    	 //if WebElement found, try to call method
			// start of code changed by Princi for WI - 76628
			if(sm.getActionName().equals("displayValue") && (sm.getData() == null || sm.getData().trim().isEmpty())){
	    		return (StepReturn) sm.getMethodCall().invoke(sm.getWebObject(),sm);
	    	}
			//end of code changed by Princi for WI - 76628
			else if (sm.getData() == null || sm.getData().trim().isEmpty())
	    		return (StepReturn) sm.getMethodCall().invoke(sm.getWebObject());	    	 
	    	else
	    		return (StepReturn) sm.getMethodCall().invoke(sm.getWebObject(), sm);

	    } catch(ActionUndefined | DriverNotExistException | StepNameUndefined | StepReturnUndefined  | TestNameUndefined | TypeNotFoundException | TypeUndefined | UrlRemoteDriverUndefined e){
			return new StepReturn (StepReturnEnum.ERROR,e);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
			return new StepReturn (StepReturnEnum.ERROR,e);
		}
	}
	
	// code changed by Princi for 57182
	public List<Integer> getDataList() {
		return dataList;
	}

	public void setDataList(List<Integer> dataList) {
		this.dataList = dataList;
	}

	public HashMap<String, List<Integer>> getTestStepStoredMap() {
		return testStepStoredMap;
	}

	public void setTestStepStoredMap(HashMap<String, List<Integer>> testStepStoredMap) {
		this.testStepStoredMap = testStepStoredMap;
	}	
	//end of code changed by Princi for 57182

}
