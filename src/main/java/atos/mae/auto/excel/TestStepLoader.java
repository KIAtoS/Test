package atos.mae.auto.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.gargoylesoftware.htmlunit.javascript.host.Console;

import atos.mae.auto.factory.GlobalWebElement;
import atos.mae.auto.factory.GlobalWebElementFactory;
import atos.mae.auto.factory.WebElement;
import atos.mae.auto.hpalm.HpAlm;
import atos.mae.auto.json.JsonLoader;
import atos.mae.auto.model.DataColumnModel;
import atos.mae.auto.model.ModuleModel;
import atos.mae.auto.model.StepModel;
import atos.mae.auto.model.TestStepModel;
import atos.mae.auto.plugins.PluginsLoader;
import atos.mae.auto.plugins.requirement.IAction;
import atos.mae.auto.plugins.requirement.StepReturnEnum;
import atos.mae.auto.utils.ExcelUtils;
import atos.mae.auto.utils.Liste;
import atos.mae.auto.utils.Exceptions.RangeNameNotFoundException;

@Component
public class TestStepLoader {
	/**
	 * Logger.
	 */
	private static Logger Log = LoggerFactory.getLogger(TestStepLoader.class);

	@Value("${testLibraryPath:TestLibrary}")
	private String testLibraryPath;

	@Value("${modulePath:Modules}")
	private String modulePath;

	@Value("${testStepRangeTestDescription:TestDescription}")
	public String testStepRangeTestDescription;

	@Value("${testStepSheetName:TestSteps}")
	private String testStepSheetName;

	@Value("${testStepRangeName:StepName}")
	private String testStepRangeName;

	@Value("${testStepRangeDescription:StepDescription}")
	private String testStepRangeDescription;

	@Value("${testStepRangeRun:UseData}")
	private String testStepRangeRun;

	@Value("${testStepRangeAbortOnFail:AbortOnFailStep}")
	private String testStepRangeAbortOnFail;

	@Value("${testStepRangeIdentifier:Object}")
	private String testStepRangeIdentifier;

	@Value("${testStepRangeTestObjectIndex:Test_Object_Index}")
    private String testStepRangeTestObjectIndex;

	@Value("${testStepRangeAction_Module:Action}")
	private String testStepRangeAction_Module;

	@Value("${testStepRangeDataStart:Data1}")
	private String testStepRangeDataStart;

	@Value("${testStepConfigRangeEnvironment:Environment}")
	private String testStepConfigRangeEnvironment;

	@Value("${testStepScreenCapture:SystematicScreenCapture}")
	private String testStepScreenCapture;

	@Value("${moduleRangeParameters:Parameters}")
	private String moduleRangeParameters;

	@Value("${testStepRangeName:StepName}")
	private String moduleRangeName;

	@Value("${testStepRangeDescription:StepDescription}")
	private String moduleRangeDescription;

	@Value("${testStepRangeAbortOnFail:AbortOnFailStep}")
	private String moduleRangeAbortOnFail;

	@Value("${testStepRangeIdentifier:Object}")
	private String moduleRangeIdentifier;

	@Value("${testStepRangeAction_Module:Action}")
	private String moduleRangeAction_Module;

	@Value("${testStepRangeDataStart:Data1}")
	private String moduleRangeDataStart;

	@Value("${moduleParameterIdentifier:P}")
	private String moduleParameterIdentifier;

	@Value("${excelTrue:P}")
	private String excelTrue;

	//Excel Config
	private boolean TestSetFirst = false;

	@Autowired
	private Liste liste;

	@Autowired
	private PluginsLoader pluginsLoader;

	@Autowired
	private ExcelUtils excelUtils;

	@Autowired
	private GlobalWebElementFactory emptyIdentifierFactory;

	@Autowired
	private JsonLoader jsonLoader;

	@Autowired
	private HpAlm hpAlm;

	// contient les metadata du fichier excel actuellement traité
	private RunConfiguration runConfiguration = new RunConfiguration();

	/**
	 * permet la sauvegarde du contexte d'un Test (RunConfiguration) en vue de le restaurer après la fin du traitement d'un module
	 */
	private RunConfiguration rcTest;

	private boolean isScreenCaptureNeeded;

	/**
	 * Load testStep with the testStepName
	 * @param testStepName testStep name
	 * @return TestStepModel or ModuleModel
	 */
	public Object load(String testStepName,String env) {
		return this.load(testStepName,false,"", env);
	}


	
	/**
	 * Load testStep with testStepName 
	 * @param testStepName testStep name
	 * @param isModule is module or not
	 * @param moduleParams module parameters
	 * @return TestStepModel or ModuleModel
	 */
	public Object load(String testStepName, boolean isModule, String moduleParams, String env) {
		TestStepModel tsm;
		String[] paramSplit = new String[0];
		if(isModule){
			tsm = new ModuleModel();
			List<String> paramModuleValues = new ArrayList<String>();
			if(moduleParams != null){
				paramSplit = moduleParams.split("\\|\\|");
				Log.info(Arrays.toString(paramSplit));
				paramModuleValues = (List<String>) Arrays.asList(paramSplit);
			}
			((ModuleModel)tsm).setParamModuleValues(paramModuleValues);
		}
		else
			tsm = new TestStepModel();

		tsm.setTestName(testStepName);
		final String path = this.getExcelPath(tsm);

        final File file = new File(path);

        tsm.setFile(file);

        Workbook wb;
        XSSFSheet testStepsSheet;
        try {
		// Take Sheet
        	final FileInputStream testStepsFIS = new FileInputStream(path);
        	wb = new XSSFWorkbook(testStepsFIS);
        	testStepsSheet = (XSSFSheet) wb.getSheet(this.testStepSheetName);
        	Log.info("Before wb close --> ");
        	testStepsFIS.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Check file structure
		this.setRunConfiguration(tsm, wb);
		
		if( !isModule) {
			this.rcTest = (RunConfiguration) this.runConfiguration.clone();
			
			// Load env
	        final Cell environmentCell = this.excelUtils.getCellByName(wb, this.testStepConfigRangeEnvironment);
	        if(env.isEmpty() && environmentCell != null && environmentCell.toString() != null && !environmentCell.toString().trim().isEmpty()){
		    	final String platform = environmentCell.toString();
		    	this.jsonLoader.LoadEnv(platform,true);
	        } else {
	        	this.jsonLoader.LoadEnv(env, true);
	        }

			final Cell descCell = this.excelUtils.getCellByName(wb, this.testStepRangeTestDescription);
			if(descCell.getCellType() == XSSFCell.CELL_TYPE_STRING
			&& descCell.toString() != null)
				tsm.setTestDescription(descCell.toString());
			else
				tsm.setTestDescription("");
		}

		
		XSSFRow rowRun = testStepsSheet.getRow(this.getRunConfiguration().getRunRow());
		XSSFRow rowName = testStepsSheet.getRow(this.getRunConfiguration().getRunRow() - 1);

		logRow(rowRun, this.getRunConfiguration().getRunRow());

        if(tsm.isModule()){
        	//this.checkModuleParameters(moduleParams, testStepsSheet);
        	
        	final DataColumnModel dcm = this.loadSteps(tsm, testStepsSheet, this.getRunConfiguration().getDataStartCol(), true, env);
        	tsm.addDataColumn(dcm);
        	this.runConfiguration = (RunConfiguration) rcTest.clone();
        }
        else{
        	//int lastColNum = this.rowRun.getLastCellNum();
        	this.runConfiguration = (RunConfiguration) rcTest.clone();
        	for(int indexCol = this.getRunConfiguration().getDataStartCol();indexCol < rowRun.getLastCellNum(); indexCol+=2 ){
        		if(rowRun.getCell(indexCol + 1).toString().equals(this.excelTrue)){
        			final DataColumnModel dcm = this.loadSteps(tsm, testStepsSheet, indexCol, false, env);
        			tsm.addDataColumn(dcm);
        		}
        	}
        }

        this.hpAlm.createTestRunHierarchy(tsm);

		return tsm;
	}



	private void logRow(XSSFRow row, int numRow) {
		Log.debug("content of row before "  + numRow);
		
		String tmp = "";
		for(int indexCol = 0;indexCol < row.getLastCellNum(); indexCol++ ){
			if(row.getCell(indexCol) != null){
				tmp += row.getCell(indexCol).toString() + "; ";
			} else {
				tmp += "NULL_CELL; ";
			}
    	}
		tmp += "\n";
		Log.debug(tmp);
	}


	/**
	 * Load dataColumn by adding step in the list
	 * @param tsm TestStepModel
	 * @param testStepsSheet sheet of testStep
	 * @param indexCol index Column value
	 * @param isModule is Module or not
	 * @return a dataColumn model
	 */
	private DataColumnModel loadSteps(TestStepModel tsm,XSSFSheet testStepsSheet, int indexCol, boolean isModule, String env) {
		//TODO manage reporting if error
		final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		final Validator validator = factory.getValidator();

		final DataColumnModel dcm = new DataColumnModel();
		
		if(!isModule) {
			dcm.setDataColName(testStepsSheet.getRow(this.runConfiguration.getRunRow() - 1).getCell(indexCol).toString());
			dcm.setDataColIndex(indexCol + 1);
		}

		for(int rowCount = this.runConfiguration.getRowStart();rowCount<=testStepsSheet.getLastRowNum();rowCount++){
			StepModel sm = this.loadStep(tsm, indexCol, testStepsSheet, rowCount, isModule, env);
			
			if(sm == null)
				break;

			Log.info("Step Row : " + sm.getRow());
			Log.info("Step Column : " + sm.getCol());
			Log.info("Step name : " + sm.getName());
			Log.info("Step action : " + sm.getActionName());
			Object webObject = sm.getWebObject();
			if(webObject instanceof WebElement){
				WebElement we = (WebElement)webObject;
				Log.info("Step object : " + we.getObjectName());
			}
			else {
				Log.info("Step object : No WebElement need for this Step");
			}
			
			//CPD ignore step with "null" in data column
			if (sm.getData() != null){
				if (sm.getData().equalsIgnoreCase("null")) {
					Log.info("Step data = Null, Step ignored");
					continue;
				}
				else if(sm.getData().startsWith("NoData_S")) {
					sm.setData(null);
					Log.info("Step data : No Data for this Step");
				}
				else {
					Log.info("Step data : " + sm.getData());
				}
			}
			else {
				Log.info("Step data : No Data for this Step");
			}	
				
			sm.setPath(tsm.getFile().getParent());

			final Set<ConstraintViolation<StepModel>> constraintViolations = validator.validate(sm);

             if (constraintViolations.size() > 0 ) {
               String error = "Error while loading step :";
               for (final ConstraintViolation<StepModel> contraintes : constraintViolations) {
                 Log.error(contraintes.getRootBeanClass().getSimpleName()+
                    "." + contraintes.getPropertyPath() + " " + contraintes.getMessage());
                 error += "<br/> - " +contraintes.getMessage();
               }
               sm.getStepReturn().setInformation(error);
               if(sm.isAbortOnFail()){
            	   sm.getStepReturn().setStepReturn(StepReturnEnum.ERROR);
            	   dcm.addStep(sm);
            	   break;
               }else{
            	   sm.getStepReturn().setStepReturn(StepReturnEnum.WARN);
               }
             }
			 dcm.addStep(sm);
		}


		return dcm;
	}

	/**
	 * Load a step
	 * @param tsm TestStepModel
	 * @param indexCol index column value
	 * @param testStepsSheet sheet of testSteps excel file
	 * @param rowCount row's number
	 * @param isModule is module or not
	 * @return stepModel
	 */
	private StepModel loadStep(TestStepModel tsm,int indexCol, XSSFSheet testStepsSheet, int rowCount, boolean isModule, String env) {
		final StepModel sm = new StepModel();
		final XSSFRow row = testStepsSheet.getRow(rowCount);
		//final ArrayList<Exception> exceptionList = new ArrayList<Exception>();

		// Load step name
        Log.debug("Row in treatment : " + row.getRowNum());
        if(this.getName(row) == null || this.getName(row).trim().isEmpty()){
            int i; 
        	i = row.getRowNum() - 13; 
        	sm.setName("Step" + i);}
        else
        	sm.setName(this.getName(row));
        	
        if(sm.getName() == null || sm.getName().trim().isEmpty())
        	return null;
        
        // code change for 30640
        // Load Action name
        int actionCol = this.getRunConfiguration().getAction_ModuleCol();
        //Log.info("actionCol : " + actionCol);
        sm.setActionName(this.getActionName(row, actionCol));
        if(sm.getActionName() == null || sm.getActionName().trim().isEmpty())
        {   Log.info("No Action Found, End of Test");
        	return null;}
			
	   // end code change for 30640
        
        // Load step description
        sm.setStepDescription(this.getDescription(row));

        // Load webObject
        sm.setWebObject(this.getWebObject(row));

        sm.setDefaultIndex(getDefaultIndex(row));

        // Load boolean is abort on fail
        sm.setAbortOnFail(this.isAbortOnFail(row));

        // Load datas and action or module
        if(isModule){
        	this.setActionOrModule(sm, row, env);
        	//CPD Change for WI 71823
        	//if(sm.getMethodCall() != null && sm.getMethodCall().getParameterCount() > 0)
        		sm.setData(((ModuleModel)tsm).getNextParam());

        }else{
        	sm.setData(this.getData(row, indexCol));
        	sm.setRow(row.getRowNum());
        	sm.setCol(indexCol + 1);
        	this.setActionOrModule(sm, row, env);
        }

        sm.setScreenCaptureNeeded(this.isScreenCaptureNeeded());

		return sm;
	}

	/**
	 * Get Path depending of is module or not.
	 * @param tsm model
	 * @return excel path
	 */
	private String getExcelPath(TestStepModel tsm) {
		// Open the Excel file
		String intermediatePath = "";
		if(tsm.isModule()){
			intermediatePath = this.modulePath;
		}else{
			intermediatePath = this.testLibraryPath;
		}

		final String name = tsm.getTestName().split("\\.")[0];
		final String path = Paths.get(intermediatePath,name,name + ".xlsm").toString();

		return path;
	}

	/**
	 * Name of Step
	 * @param row row
	 * @return name of step
	 */
	private String getName(final XSSFRow row) {
		//// Code Change for 30640
		//if (row.getCell(this.rc.getNameCol()) == null  || row.getCell(this.rc.getNameCol()).getCellType() != XSSFCell.CELL_TYPE_STRING)
		//end  Code Change for 30640
			if (row.getCell(this.getRunConfiguration().getNameCol()) == null){
				Log.info("Ligne excel en cours de traitement : " + row.getRowNum());
				return null;   //TODO modify the null to generate the step name : example STEP + row.value
			}
		else
			return row.getCell(this.getRunConfiguration().getNameCol()).toString();

	}	
	
	
	// Code Change for 30640
    private String getActionName(final XSSFRow row, int columnName) {		
		if (row.getCell(columnName) == null || row.getCell(columnName).getCellType() != XSSFCell.CELL_TYPE_STRING)
			return null;
		else
		{
			
		return row.getCell(columnName).toString();
		}
			}
 // end code change for 30640

	private int getDefaultIndex(final XSSFRow row) {
        if (row.getCell(this.getRunConfiguration().getIdentifierIndexCol()) == null  || row.getCell(this.getRunConfiguration().getIdentifierIndexCol()).getCellType() != XSSFCell.CELL_TYPE_NUMERIC)
            return 0;
        else
            return Math.abs((int) row.getCell(this.getRunConfiguration().getIdentifierIndexCol()).getNumericCellValue());

    }

	/**
	 * Description of step
	 * @param row row
	 * @return Description of step
	 */
	private String getDescription(final XSSFRow row) {
		if(row.getCell(this.getRunConfiguration().getDescriptionCol()) == null || row.getCell(this.getRunConfiguration().getDescriptionCol()).getCellType() != XSSFCell.CELL_TYPE_STRING)
			return "";
		else
			return row.getCell(this.getRunConfiguration().getDescriptionCol()).toString();
	}


	private void setActionOrModule(StepModel sm, final XSSFRow row, String env) {
		if (row.getCell(this.getRunConfiguration().getAction_ModuleCol()) == null
        || (row.getCell(this.getRunConfiguration().getAction_ModuleCol()).getCellType() != XSSFCell.CELL_TYPE_STRING))
			return;

		final Object webObject = sm.getWebObject();
		if(webObject == null)
			return;
		final String actionModule = row.getCell(this.getRunConfiguration().getAction_ModuleCol()).toString().trim();

		// Check modules
		if(webObject instanceof GlobalWebElement && this.liste.getModulesAvailable().contains(actionModule) == true){
			sm.setModuleCall((ModuleModel)this.load(actionModule,true,sm.getData(), env));

			if(sm.getModuleCall() != null)
				return;

		}

		sm.setMethodCall(this.getAction(webObject, actionModule));
	}

	/**
	 * Get Action
	 * @param webObject
	 * @param actionModule
	 * @return a Method
	 */
	private Method getAction(Object webObject, String actionModule){
		// try to found from Action class
		Method m = this.searchMethod(webObject, actionModule);


		if(m != null)
			return m;

		// Try to found from Action plugin
		for (final IAction actionPlugin : this.pluginsLoader.getActionPlugins()) {
			m = this.searchMethod(actionPlugin, actionModule);

			if(m != null)
				return m;
		}

		return null;
	}

	/**
	 * Search method
	 * @param webObject webProcject
	 * @param actionModule action module
	 * @return Method
	 */
	private Method searchMethod(Object webObject, String actionModule){
		final Method[] methods = webObject.getClass().getMethods();
		for (final Method m : methods) {
			// if method found
			if (m.getName().compareTo(actionModule) == 0) {
				return m;
			}
		}

		return null;
	}

	/**
	 * Object of page (column Page Object of excel file testStep)
	 * @param row row of step
	 * @return object
	 */
	private Object getWebObject(final XSSFRow row) {
		if(row.getCell(this.getRunConfiguration().getIdentifierCol()) == null || row.getCell(this.getRunConfiguration().getIdentifierCol()).toString().trim().isEmpty()){
        	return this.emptyIdentifierFactory.MakeIdentifier();
        }else{
        	final String webObjectName = row.getCell(this.getRunConfiguration().getIdentifierCol()).toString();
        	final Object webObject = this.liste.getIdentifiers().get(webObjectName);

        	if(webObject == null){
        		//throw new WebObjectNotFoundInObjectRepository(webObjectName);
        		Log.error(webObjectName + " not found in ObjectRepository");
        	}

        	return webObject;
        }
	}

	private String getData(XSSFRow row, int dataCol){
		/*CPD temp pour eliminer step null
		if(row.getCell(dataCol) != null)
				Log.info("Valeur récupérée du fichier excel  : " + row.getCell(dataCol));*/
		if(row.getCell(dataCol) == null || row.getCell(dataCol).toString().trim().isEmpty())
			return null;
		else
			return row.getCell(dataCol).toString().trim();
	}

	private boolean isAbortOnFail(XSSFRow row){
		if (row.getCell(this.getRunConfiguration().getAbortOnFailCol()) == null || row.getCell(this.getRunConfiguration().getAbortOnFailCol()).toString().compareTo(this.excelTrue) == 0)
			return true;
		else
        	return false;
	}


	/**
	 * Get TestStep configuration.
	 * @param wb workbook
	 */
	private void setRunConfiguration(TestStepModel tsm, Workbook wb) {
		
		RunConfiguration rc = new RunConfiguration();
		
		if(tsm.isModule()) {
			// Update Config with named range for Module
			this.runConfiguration.setRowStart(this.excelUtils.getRowByName(wb, this.moduleRangeName));
			this.runConfiguration.setNameCol(this.excelUtils.getColByName(wb, this.moduleRangeName));
			this.runConfiguration.setAbortOnFailCol(this.excelUtils.getColByName(wb, this.moduleRangeAbortOnFail));
			this.runConfiguration.setIdentifierCol(this.excelUtils.getColByName(wb, this.moduleRangeIdentifier));
			this.runConfiguration.setAction_ModuleCol(this.excelUtils.getColByName(wb, this.moduleRangeAction_Module));
			//this.dataStartCol = this.excelUtils.getColByName(wb, this.moduleRangeDataStart);
		} else {
			// Update Config with named range for testStep
			this.runConfiguration.setRowStart(this.excelUtils.getRowByName(wb, this.testStepRangeName));
			this.runConfiguration.setRunRow(this.excelUtils.getRowByName(wb, this.testStepRangeRun));
			this.runConfiguration.setNameCol(this.excelUtils.getColByName(wb, this.testStepRangeName));
			this.runConfiguration.setDescriptionCol(this.excelUtils.getColByName(wb, this.testStepRangeDescription));
			this.runConfiguration.setAbortOnFailCol(this.excelUtils.getColByName(wb, this.testStepRangeAbortOnFail));
			this.runConfiguration.setIdentifierCol(this.excelUtils.getColByName(wb, this.testStepRangeIdentifier));
			try{
			    this.runConfiguration.setIdentifierIndexCol(this.excelUtils.getColByName(wb, this.testStepRangeTestObjectIndex));
			}catch(RangeNameNotFoundException e){
			    Log.info("Range " + this.testStepRangeTestObjectIndex + " not found. Default index value (0) will be use.");
			}
			this.runConfiguration.setAction_ModuleCol(this.excelUtils.getColByName(wb, this.testStepRangeAction_Module));
			this.runConfiguration.setDataStartCol(this.excelUtils.getColByName(wb, this.testStepRangeDataStart));
			if(!this.TestSetFirst){
				final String screenCapture = this.excelUtils.getCellByName(wb, this.testStepScreenCapture).toString();
				this.isScreenCaptureNeeded = screenCapture.equals(this.excelTrue);
			}
		}
	}

	public boolean isTestSetFirst() {
		return TestSetFirst;
	}

	public void setTestSetFirst(boolean testSetFirst) {
		TestSetFirst = testSetFirst;
	}



	public RunConfiguration getRunConfiguration() {
		return runConfiguration;
	}



	public void setRunConfiguration(RunConfiguration rc) {
		this.runConfiguration = rc;
	}
	
	public boolean isScreenCaptureNeeded() {
		return isScreenCaptureNeeded;
	}
	public void setScreenCaptureNeeded(boolean isScreenCaptureNeeded) {
		this.isScreenCaptureNeeded = isScreenCaptureNeeded;
	}

}
