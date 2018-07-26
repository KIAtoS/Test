package atos.mae.auto.excel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import atos.mae.auto.model.TestSetModel;
import atos.mae.auto.model.TestStepModel;
import atos.mae.auto.plugins.requirement.WebDriverProvider;
import atos.mae.auto.utils.ExcelUtils;

@Component
public class TestSetService {

	/**
	 * Logger.
	 */
	private static Logger Log = Logger.getLogger(TestSetService.class);	

	/**
	 * Execute TestStep class.
	 */
	@Autowired
	private TestStepService testStepService;

	@Autowired
	private WebDriverProvider webDriverProvider;
	
	@Autowired
	private ExcelUtils excelUtils; // changed by Princi for 57182

	/**
	 * Execute the TestSet
	 * @param tsm TestSetModel
	 */
	public void execute(TestSetModel tsm){
		//code changed by Princi for WI 57182
		HashMap<String, List<Integer>> resultMap;
				FileInputStream testStepsFIS;		
		        Workbook wb;
		        try {
					testStepsFIS = new FileInputStream(tsm.getTestStepfile());					
					wb = new XSSFWorkbook(testStepsFIS);
					XSSFSheet sheet = (XSSFSheet)wb.getSheetAt(0);
		// end of code changed by Princi for WI 57182	
		int loop = tsm.getDesiredCapabilitiesList().size();

		if(loop == 0)
			loop = 1;

		for(int index = 0; index < loop; index++){
			if(tsm.getDesiredCapabilitiesList().size() > 0)
				this.webDriverProvider.setDesiredCapabilities(tsm.getDesiredCapabilitiesList().get(index));

			for (final TestStepModel tStepM : tsm.getTestStepModelList()) {
				this.testStepService.execute(tStepM);
				// code changed by Princi for 57182				
				resultMap = this.testStepService.getTestStepStoredMap();				
	    		Log.info("Last row number of test set ---> "+sheet.getLastRowNum());
	    		for(int count = tsm.getRow();count<=sheet.getLastRowNum();count++){
	                final XSSFRow row = sheet.getRow(count);	
	                //System.out.println("test name check "+(row.getCell(tsm.getStartCol()).toString().trim()).equals(tStepM.getTestName()));
	              //  System.out.println("run test check "+(row.getCell(tsm.getRunTest()).toString().trim()).equals("P"));
	                if(null != row.getCell(tsm.getStartCol()).toString()  && row.getCell(tsm.getStartCol()).toString() !="" ){
	                if(((row.getCell(tsm.getStartCol()).toString().trim()).equals(tStepM.getTestName())) && ((row.getCell(tsm.getRunTest()).toString().trim()).equals("P"))){
	                	Log.info("Row Number to display the result --> "+row.getRowNum());
	                	tsm.setRow(row.getRowNum());
	                	Log.info("Column Number starts to display the result ---> "+tsm.getCol());	                	
	                	for (HashMap.Entry<String, List<Integer>> entry : resultMap.entrySet()) {
	                	    String key = entry.getKey();
	                	    if(key.equals(tStepM.getTestName())){
	                	    List<Integer> dataValList = entry.getValue();
	                	    for(int dataResult : dataValList){
	                	        Log.info("key : " + key + " value : " + dataResult);
	                	        Log.info("Row to write the result---> "+tsm.getRow());
	            				Log.info("Column to write the result---> "+tsm.getCol());
	                	        final Cell cell = this.excelUtils.getCellByRowCol(sheet, tsm.getRow(), tsm.getCol());
	                	        cell.setCellType(Cell.CELL_TYPE_NUMERIC);
	                	        cell.setCellValue(dataResult);
	                	        tsm.setCol(tsm.getCol()+1);
	                	    }
	                	   }	                	    
	                	}	    				
	                } 
	                }
	                tsm.setCol(4);
	              //end of code changed by Princi for WI 57182  	    			
	    		}
			}
		}
		//code changed by Princi for WI 57182
				testStepsFIS.close();
				Log.info("file output --> "+tsm.getTestStepfile());
		    	FileOutputStream testStepsFOS;		
					testStepsFOS = new FileOutputStream(tsm.getTestStepfile());					
			    	wb.write(testStepsFOS);
			    	testStepsFOS.close();
		        }catch (IOException e) {			
					Log.error("Error in closing test set file --> "+e);
				}
	   //end of code changed by Princi for WI 57182        
	}
}
