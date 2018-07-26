package atos.mae.auto.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import atos.mae.auto.json.JsonLoader;
import atos.mae.auto.model.TestSetModel;
import atos.mae.auto.model.TestStepModel;
import atos.mae.auto.plugins.requirement.WebDriverProvider;
import atos.mae.auto.utils.ExcelUtils;
import atos.mae.auto.utils.Report;
import atos.mae.auto.utils.Exceptions.PlatformNotFound;
import atos.mae.auto.utils.enums.EnvironnementExecutionEnum;

@Component
public class TestSetLoader {

	/**
	 * Logger.
	 */
	private static Logger Log = Logger.getLogger(TestSetLoader.class);

	@Value("${resourcePath:}")
	private String resourcePath;

	@Value("${testSetSheetName:TestSet}")
	private String testSetSheetName;

	@Value("${testSetConfigSheetName:Config}")
	private String testSetConfigSheetName;

	@Value("${testSetRangeName:TestName}")
	private String testSetRangeName;

	@Value("${testSetRangeDescription:TestDescription}")
	private String testSetRangeDescription;

	@Value("${testSetRangeRunTest:RunTest}")
	private String testSetRangeRunTest;

	@Value("${testSetRangePlannedExecution:PlannedExecution}")
	private String testSetRangePlannedExecution;

	@Value("${testSetRangeDataStart:Data1}")
	private String testSetRangeDataStart;

	@Value("${testSetConfigRangeDriverSelect:DriverSelect}")
	private String testSetConfigRangeDriverSelect;

	@Value("${testSetConfigRangeEnvironment:Environment}")
	private String testSetConfigRangeEnvironment;

	@Value("${testSetConfigRangeUrlRemoteDriver:UrlRemoteDriver}")
	private String testSetConfigRangeUrlRemoteDriver;

	@Value("${testSetScreenCapture:SystematicScreenCapture}")
	private String testSetScreenCapture;

	@Value("${testSetBrowserStackCapabilitiesList:BrowserStackCapabilitiesList}")
	private String testSetBrowserStackCapabilitiesList;

	@Value("${excelFalse:O}")
	private String excelFalse;

	@Value("${excelTrue:P}")
	private String excelTrue;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private WebDriverProvider webDriverProvider;

	@Autowired
	private JsonLoader jsonLoader;

	@Autowired
	private TestStepLoader testStepLoader;
	
	private int testSetRowStart;
	private int testSetNameCol;
	private int testSetDescriptionCol;
	private int testSetRunCol;
	private int testSetPlannedExecutionCol;
	private int testSetDataStartCol;
	private Cell testSetBrowserStackCapabilitiesStart;

	/**
	 * Load TestSet excel file.
	 * @throws PlatformNotFound.
	 *
	 */
	final public TestSetModel load(String TestSetName, String env) throws PlatformNotFound{
		final TestSetModel tsm = new TestSetModel();
		// Open the Excel file
		if(!TestSetName.endsWith(".xlsm")){
			final StringBuilder sb = new StringBuilder(TestSetName);
			sb.append(".xlsm");
			TestSetName = sb.toString();
		}

		tsm.setName(TestSetName);

        XSSFSheet testSetSheet;
        XSSFSheet testSetConfigSheet;
        Workbook wb;
        try {
        	// Take Sheet
        	final File fileTestSet = new File(Paths.get(this.resourcePath,TestSetName).toString());
        	// changed by Princi for WI 57182
        	Log.info("TestSet file Path --> "+fileTestSet);
        	tsm.setTestStepfile(fileTestSet);
        	//end of changed by Princi for WI 57182
    		final FileInputStream testSetFIS = new FileInputStream(fileTestSet);
            wb = new XSSFWorkbook(testSetFIS);
        	testSetSheet = (XSSFSheet) wb.getSheet(this.testSetSheetName);
        	testSetConfigSheet = (XSSFSheet) wb.getSheet(this.testSetConfigSheetName);
        	testSetFIS.close();
	        wb.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

        this.testStepLoader.setTestSetFirst(true);
        this.setRunConfiguration(tsm, wb);
        tsm.setCol(this.testSetDataStartCol);  // changed by Princi for 57182
        tsm.setRow(this.testSetRowStart);     // changed by Princi for 57182
        tsm.setRunTest(this.testSetRunCol);     // changed by Princi for 57182
        tsm.setStartCol(testSetNameCol);
        this.setExecEnv(testSetConfigSheet, wb);


    	// Load BrowserStack Capabilities if BrowserStack activated
        if(this.webDriverProvider.getEnvExec() == EnvironnementExecutionEnum.BROWSERSTACK){
        	this.loadBrowserStackCapabilities(tsm,testSetConfigSheet);
        }


    	// Load env
        final Cell environmentCell = this.excelUtils.getCellByName(wb, this.testSetConfigRangeEnvironment);
        if(env.isEmpty() && environmentCell != null && environmentCell.toString() != null && !environmentCell.toString().trim().isEmpty()){
	    	final String platform = environmentCell.toString();
	    	this.jsonLoader.LoadEnv(platform,true);
        } else {
        	this.jsonLoader.LoadEnv(env,true);
        }

    	this.loadTests(tsm, testSetSheet, env);

    	return tsm;

	}

	/**
	 * Load browserStack capabilities
	 * @param tsm testSetModel
	 * @param testSetConfigSheet configuration sheet of excel file
	 */
	private void loadBrowserStackCapabilities(TestSetModel tsm, XSSFSheet testSetConfigSheet) {
		for(int count = this.testSetBrowserStackCapabilitiesStart.getRowIndex();count<=testSetConfigSheet.getLastRowNum();count++){
			final XSSFRow row = testSetConfigSheet.getRow(count);

			if(row.getCell(0) != null && (row.getCell(0).toString().trim().isEmpty() || row.getCell(0).toString().compareTo(this.excelTrue) != 0))
				continue;

			//if new capability line
			if((row.getCell(1) != null && !row.getCell(1).toString().trim().isEmpty())){
				DesiredCapabilities caps = new DesiredCapabilities();
				caps.setCapability("os", row.getCell(1).toString());
				caps.setCapability("os_version", row.getCell(2).toString());
		        caps.setCapability("browser", row.getCell(3).toString());
		        caps.setCapability("browser_version", row.getCell(4).toString());
		        caps.setCapability("browserstack.debug", "true");
				Log.info("os : " + row.getCell(1).toString());
				Log.info("os_version : " + row.getCell(2).toString());
				Log.info("browser : " + row.getCell(3).toString());
				Log.info("browser_version : " + row.getCell(4).toString());
		        tsm.addCapability(caps);
			}else if (row.getCell(6) != null && !row.getCell(6).toString().trim().isEmpty()){
				DesiredCapabilities caps = new DesiredCapabilities();
				caps.setCapability("browserName", row.getCell(6).toString());
				caps.setCapability("platform", row.getCell(7).toString());
				caps.setCapability("device", row.getCell(8).toString());
				Log.info("browserName : " + row.getCell(6).toString());
				Log.info("platform : " + row.getCell(7).toString());
				Log.info("device : " + row.getCell(8).toString());
		        caps.setCapability("browserstack.debug", "true");
		        tsm.addCapability(caps);

			}
		}
	}

	/**
	 * load testStep after loading configuration.
	 */
	final public void loadTests(TestSetModel tsm,XSSFSheet testSetSheet,String env)
	{

		// Loop through all rows in the sheet
        // Start at row 1 as row 0 is our header row
		Log.info("get LAst row number ---> "+testSetSheet.getLastRowNum());
        for(int count = this.testSetRowStart;count<=testSetSheet.getLastRowNum();count++){

            final XSSFRow row = testSetSheet.getRow(count);
            Log.info("count --> "+count);

            // break reading if no name found, end test campaign
            if(row.getCell(this.testSetNameCol) == null || row.getCell(this.testSetNameCol).toString() == null || row.getCell(this.testSetNameCol).toString().trim().isEmpty())
            	break;
            final String Name = row.getCell(this.testSetNameCol).toString().trim();                     
            final String Run = row.getCell(this.testSetRunCol).toString().trim();
            Log.info("Run Test --> "+Run);
            if(Run.equals("P"))
            {
            final TestStepModel tstepm = (TestStepModel)this.testStepLoader.load(Name, env);

            if (row.getCell(this.testSetRunCol).toString().equals(this.excelFalse))
            {
            	tstepm.setRun(false);
            }else{
            	tstepm.setRun(true);            	
            	tsm.addTestStepModel(tstepm); // code changed by Princi for WI- 57182, this will load only those test steps(excel) which user wants to execute            	
            } 
            }           
            
         //tsm.addTestStepModel(tstepm); //code changed by Princi for WI- 57182            
            
        }
	}

	/**
	 * Set environment where it is execute
	 * @param testSetConfigSheet configuration sheet from testSet Excel file
	 * @param wb
	 */
	private void setExecEnv(XSSFSheet testSetConfigSheet, Workbook wb) {
		// check execution environnement  (local, remote, browserstack)
        final Cell DriverSelectCell = this.excelUtils.getCellByName(wb, this.testSetConfigRangeDriverSelect);
        final int env = (int)DriverSelectCell.getNumericCellValue();
    	this.webDriverProvider.setEnvExec(env);


    	switch(this.webDriverProvider.getEnvExec()){
    		case REMOTE:
    			// get Url
    	        final Cell UrlRemoteCell = this.excelUtils.getCellByName(wb, this.testSetConfigRangeUrlRemoteDriver);
    	        if(UrlRemoteCell != null){
	    	        final String url = UrlRemoteCell.toString();
	    	        Log.info("Url for remote driver : " + url);
	    	    	this.webDriverProvider.setUrlRemoteDriver(url);
    	        }
    			break;
    		case BROWSERSTACK:
    			break;
			default:
    	}
	}

	/**
	 * Get TestStep configuration.
	 * @param wb workbook
	 */
	private void setRunConfiguration(TestSetModel tsm, Workbook wb) {
		// Update Config with named range
		this.testSetRowStart = this.excelUtils.getRowByName(wb, this.testSetRangeName);
		this.testSetNameCol = this.excelUtils.getColByName(wb, this.testSetRangeName);
		this.testSetDescriptionCol = this.excelUtils.getColByName(wb, this.testSetRangeDescription);
		this.testSetRunCol = this.excelUtils.getColByName(wb, this.testSetRangeRunTest);
		this.testSetPlannedExecutionCol = this.excelUtils.getColByName(wb, this.testSetRangePlannedExecution);
		this.testSetDataStartCol = this.excelUtils.getColByName(wb, this.testSetRangeDataStart);
		this.testSetBrowserStackCapabilitiesStart = this.excelUtils.getCellByName(wb, this.testSetBrowserStackCapabilitiesList);
		final String screenCapture = this.excelUtils.getCellByName(wb, this.testSetScreenCapture).toString();
		//code changed by Princi for 57182
		Log.info("Test Set Row Starts from  --> "+testSetRowStart);
		Log.info("Test Name written in the column --> "+testSetNameCol);
		Log.info("Run Test Column number --> "+testSetRunCol);
		Log.info("Planned Exceution column number --> "+testSetPlannedExecutionCol);
		Log.info("Result column starts from --> "+testSetDataStartCol);
		Log.info("Test should execute or not, If yes then value should be 'P' otherwise 'O' --> "+testSetBrowserStackCapabilitiesStart);
		//end of code changed by Princi for 57182
		this.testStepLoader.setScreenCaptureNeeded(screenCapture.equals(this.excelTrue));
	}
}
