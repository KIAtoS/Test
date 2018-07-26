package atos.mae.auto.action;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Screen;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


//import atos.mae.auto.model.StepModel;

import atos.mae.auto.model.WebElementModel;
import atos.mae.auto.plugins.requirement.Parameters;
import atos.mae.auto.plugins.requirement.StepReturn;
import atos.mae.auto.plugins.requirement.StepReturnEnum;
import atos.mae.auto.plugins.requirement.WebDriverProvider;
import atos.mae.auto.utils.Liste;
import atos.mae.auto.utils.Report;
import atos.mae.auto.utils.Exceptions.DriverNotExistException;
import atos.mae.auto.utils.Exceptions.NoDriverDefineException;
import atos.mae.auto.utils.Exceptions.checked.StoredVariableNotFound;
import atos.mae.auto.utils.enums.DatabaseDriverEnum;
import atos.mae.auto.utils.enums.FTAReservedKeyWord;
import scala.Char;

import com.google.common.base.Function;
import com.jcraft.jsch.JSchException;

/**
 * This class contain all final called methods.
 * Used for list available method with --exportaction command line too.
 */
@Component
public class Action {

	@Value("${testLibraryPath:./TestLibrary}")
	private String testLibraryPath;

	@Value("${ModulePath:./Modules}")
	private String modulePath;

	@Value("${reportPath:Report}")
	private String reportPath;
	
	// start code changed by Princi for WI - 76654
	@Value("${sikuliImagePath:Configuration/ORPicture}")
	private String sikuliImagePath;
	// end code changed by Princi for WI - 76654

	@Autowired
	private WebDriverProvider webDriverProvider;

	@Autowired
	private Liste liste;

	@Autowired
	private Report report;

	/**
	 * Logger.
	 */
	private static final Logger Log = Logger.getLogger(Action.class);

	/**
	 * Step's name, used to report.
	 */
	public String stepName;

	/**
	 * The click method (on button, link, ...)
	 *
	 * @param im IdentifierModel
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("")
	public StepReturn click(WebElementModel im){
		/*int y = im.getWebElement().getLocation().getY();
        y = y - 300;
        final JavascriptExecutor jse = (JavascriptExecutor)this.webDriverProvider.getWebDriver();
        jse.executeScript("scroll(0, " + y + ");");*/
	
		ArrayList<String> tabHandles = new ArrayList<String>(webDriverProvider.getWebDriver().getWindowHandles());
        im.getWebElement().click();

        // Verify if a new Tab is open after click and get focus on it
        ArrayList<String> newTabHandles = new ArrayList<String>(webDriverProvider.getWebDriver().getWindowHandles());
        newTabHandles.removeAll(tabHandles);
        if(newTabHandles.size() == 1){
        	this.webDriverProvider.getWebDriver().switchTo().window(newTabHandles.get(0));
        }
       
        return new StepReturn(StepReturnEnum.PASS);
	}

	
	/**
	 * The click method (on button, link, ...)
	 *
	 * @param im IdentifierModel
	 * @return state of this step (Pass, fail, ...)
	 */
	/*
	@Parameters("index")
	public StepReturn click(WebElementModel im,String indexString){
		try{
		final int index = Integer.parseInt(indexString);
		if(im.getWebElements().size() >= index)
			im.setWebElement(im.getWebElements().get(index));

		}catch(NumberFormatException e){
			this.click(im);
			return new StepReturn(StepReturnEnum.WARN, "Click on webElement by his index", "Index is not a number","",e);
		}
		return this.click(im);
	}
     */

	/**
	 * Select an item from label
	 *
	 * @param im IdentifierModel
	 * @param data Label to search
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("label")
	public StepReturn selectByLabel(WebElementModel im, String data){
		final Select sel = new Select(im.getWebElement());
		try{
			sel.selectByVisibleText(data);
			return new StepReturn(StepReturnEnum.PASS);
		}catch(NoSuchElementException e){
			final StringBuilder list = new StringBuilder();
			for (final WebElement webElem : sel.getOptions()) {
				list.append("<br/>- ").append(webElem.getText());
			}
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Select '" + data + "' label.<br/>Actual : Label not found in Select list :<br/>" + list);
		}
	}

	/**
	 * Select an item from value
	 *
	 * @param im Identifier
	 * @param data Value to search
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("value")
	public StepReturn selectByValue(WebElementModel im, String data){
		final Select sel = new Select(im.getWebElement());
		try{
			sel.selectByValue(data);
			return new StepReturn(StepReturnEnum.PASS);
		}catch(NoSuchElementException e){
			final StringBuilder list = new StringBuilder();
			for (final WebElement webElem : sel.getOptions()) {
				list.append("<br/>- ").append(webElem.getAttribute("value"));
			}
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Select '" + data + "' value.<br/>Actual : Value not found in Select list :<br/>" + list);
		}
	}

	/**
	 * the input method to write in textBox, textArea, ...
	 *
	 * @param im Identifier
	 * @param input String to write
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("text_to_type")
	public StepReturn setText(WebElementModel im, String input){
		if(input.compareTo(FTAReservedKeyWord.NULL.toString()) == 0)
			return new StepReturn(StepReturnEnum.PASS);


		if(!im.getWebElement().isEnabled())
			return new StepReturn(StepReturnEnum.FAIL, "Expected : insert '" + input + "' in '" + im.getObjectName() + "'.<br/>Actual : '" + im.getObjectName() + "' is not enable.");

		im.getWebElement().sendKeys(input);
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * Clear input
	 * @param im Identifier
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("")
	public StepReturn clear(WebElementModel im){
		if(!im.getWebElement().isEnabled())
			return new StepReturn(StepReturnEnum.FAIL, "Expected : clear '" + im.getObjectName() + "'.<br/>Actual : '" + im.getObjectName() + "' is not enable.");

		im.getWebElement().clear();
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * initialise WebDriver and open browser
	 *
	 * @param browser Name of the browser (Mozilla, Chrome, ...)
	 * @return state of this step (Pass, fail, ...)
	 * @exception DriverNotExistException if browser is not available
	 */
	@Parameters("firefox/ie/chrome/edge")
	public StepReturn openBrowser(String browser){
		this.webDriverProvider.setWebDriver(browser);
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * close WebDriver and close browser
	 *
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("")
	public StepReturn closeBrowser(){
		this.webDriverProvider.closeDriver();
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * navigate to another url.
	 *
	 * @param url url to navigate
	 * @return state of this step (Pass, fail, ...)
	 * @exception NoDriverDefineException
	 */
	@Parameters("url")
	public StepReturn navigate(String url){
		if(!url.startsWith("http://") && !url.startsWith("https://"))
			url = "http://" + url;
		this.webDriverProvider.getWebDriver().get(url);
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * Close alert by accept or dismiss.
	 *
	 * @param data Accept (true) or dismiss
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("true_or_false")
	public StepReturn closeAlert(String data){
		//accept
		final boolean accept = data.toLowerCase(Locale.getDefault()).compareTo("true") == 0 ? true : false;
		if(this.closeAlert(accept))
			return new StepReturn(StepReturnEnum.PASS);
		else
			return new StepReturn(StepReturnEnum.FAIL,"Expected : Close alert.<br/>Actual : Alert not found");
	}

	private boolean closeAlert(boolean accept) {
		  if(this.isAlertPresent()){
			  final Alert alert = this.webDriverProvider.getWebDriver().switchTo().alert();
		      if (accept) {
		        alert.accept();
		      } else {
		        alert.dismiss();
		      }
		      return true;
	      }else{
	    	  return false;
	      }
	}

	/**
	 * Store value in variable to use later.
	 *
	 * @param im IdentifierModel
	 * @param VarName Variable's name where the value will be store in
	 * @param attributeName Attribute's name to check
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("variable_Name|attribute_Name")
	public StepReturn storeAttribut(WebElementModel im, String varName, String attributeName){
		final String storedValue = im.getWebElement().getAttribute(attributeName);
		this.liste.getStoredValues().put(varName, storedValue);
		return new StepReturn(StepReturnEnum.PASS, "Store in variable '" + varName + "' the value : " + storedValue);
	}

	/**
	 * Store boolean checked in variable to use later.
	 *
	 * @param im IdentifierModel
	 * @param VarName Variable's name where the value will be store in
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("variable_Name")
	public StepReturn storeChecked(WebElementModel im, String varName){
		final String storedValue = im.getWebElement().isSelected() == true ? "true":"false";
		this.liste.getStoredValues().put(varName, storedValue);
		return new StepReturn(StepReturnEnum.PASS, "Store in variable '" + varName + "' the value : " + storedValue);
	}

	/**
	 * Store body text in variable to use later.
	 *
	 * @param im IdentifierModel
	 * @param VarName Variable's name where the value will be store in
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("variable_Name")
	public StepReturn storeBodyText(WebElementModel im, String varName){
		final String storedValue = im.getWebElement().getText();
		this.liste.getStoredValues().put(varName, storedValue);
		return new StepReturn(StepReturnEnum.PASS, "Store in variable '" + varName + "' the value : " + storedValue);
	}

	/**
	 * Store selected labels in variable to use later.
	 *
	 * @param im IdentifierModel
	 * @param VarName Variable's name where the value will be store in
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("variable_Name")
	public StepReturn storeSelectedLabels(WebElementModel im, String varName){
		String storedValue;
		final Select sel = new Select(im.getWebElement());
		if(sel.isMultiple()){
			final ArrayList<String> list = new ArrayList<>();
			for (final WebElement webElement : sel.getAllSelectedOptions()) {
				list.add(webElement.getText());
			}
			storedValue = StringUtils.join(list, "|");
		}else{
			storedValue = sel.getFirstSelectedOption().getText();
		}
		this.liste.getStoredValues().put(varName, storedValue);
		return new StepReturn(StepReturnEnum.PASS, "Store in variable '" + varName + "' the value : " + storedValue);
	}

	/**
	 * Store selected labels in variable to use later.
	 *
	 * @param im IdentifierModel
	 * @param VarName Variable's name where the value will be store in
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("variable_Name")
	public StepReturn storeSelectedValues(WebElementModel im, String varName){
		String storedValue;
		final Select sel = new Select(im.getWebElement());
		if(sel.isMultiple()){
			final ArrayList<String> list = new ArrayList<>();
			for (final WebElement webElement : sel.getAllSelectedOptions()) {
				list.add(webElement.getAttribute("v"));
			}
			storedValue = StringUtils.join(list, "|");
		}else{
			storedValue = sel.getFirstSelectedOption().getText();
		}
		this.liste.getStoredValues().put(varName, storedValue);
		return new StepReturn(StepReturnEnum.PASS, "Store in variable '" + varName + "' the value : " + storedValue);
	}

	/**
	 * Store text from table in variable to use later.
	 *
	 * @param im IdentifierModel
	 * @param VarName Variable's name where the value will be store in
	 * @param iRow Table's row number
	 * @param iCol Table's col number
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("variable_Name|row_number|column_number")
	public StepReturn storeTextInTable(WebElementModel im, String varName, int iRow, int iCol){
		String storedValue;
		//final List<WebElement> tableRows = im.getWebElement().findElements(By.tagName("tr"));
		final List<WebElement> tableRows = (List<WebElement>) this.webDriverProvider.getWait().until(new Function<WebDriver,List<WebElement>>() {
			@Override
			public List<WebElement> apply(WebDriver driver) {
				return driver.findElements(By.tagName("tr"));
			}
		});
		final WebElement Row = tableRows.get(iRow);
		if(Row == null)
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Table(" + iRow + "," + iCol + ").<br/>Actual : Row number '" + iRow + "' not found");
		//final List<WebElement> tableCol = Row.findElements(By.tagName("td"));
		final List<WebElement> tableCol = (List<WebElement>) this.webDriverProvider.getWait().until(new Function<WebDriver,List<WebElement>>() {
			@Override
			public List<WebElement> apply(WebDriver driver) {
				return driver.findElements(By.tagName("td"));
			}
		});
		final WebElement Col = tableCol.get(iCol);
		if(Col == null)
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Table(" + iRow + "," + iCol + ").<br/>Actual : Column number '" + iCol + "' not found");

		storedValue = Col.getText();
		this.liste.getStoredValues().put(varName, storedValue);
		return new StepReturn(StepReturnEnum.PASS, "Store in variable '" + varName + "' the value : " + storedValue);
	}


	/**
	 * Move the mouse on WebElement to trigger hover
	 * @param im IdentifierModel
	 * @return state of this step (Pass, fail, ...)
	 * @exception NoDriverDefineException
	 */
	@Parameters("")
	public StepReturn hover(WebElementModel im){
		// Doesn't work
		final Actions mousemove = new Actions(this.webDriverProvider.getWebDriver());
		mousemove.moveToElement(im.getWebElement());
		mousemove.build().perform();
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * Do a Backup from selected database.
	 * @param Datas DatabaseDriver|Host|User|Password|DatabaseName|PathToBackUp
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("DatabaseDriver|Host|User|Password|DatabaseName|PathToBackUp")
	public StepReturn backupDatabase(String Datas){
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * Restore selected database from sql backup.
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("Not Implemented yet")
	public StepReturn restoreDatabase(){
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * Do 'select' from sql
	 * @param Datas DatabaseDriver|Host|Port|User|Password|DatabaseName|Request
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("DatabaseDriver|Host|Port|User|Password|DatabaseName|Request")
	public StepReturn sqlSelect(String Datas){
		final String[] DataSplited = Datas.split("\\|");
		if(DataSplited.length != 7)
			return new StepReturn(StepReturnEnum.ERROR, "Expected : 7 arguments separated by a pipe ('|').<br/>Actual : " + DataSplited.length + " arguments found.");

		final DatabaseDriverEnum Driver = BDDManager.getDatabaseDriverEnum(DataSplited[0]);
		if(Driver == null)
			return new StepReturn(StepReturnEnum.ERROR, DataSplited[0] + " is not a database driver. please check the spelling");
		final String Host = DataSplited[1];
		final String Port = DataSplited[2];
		final String User = DataSplited[3];
		final String Password = DataSplited[4];
		final String DatabaseName = DataSplited[5];
		final String Request = DataSplited[6];

		final StringBuilder line = new StringBuilder();
  	  	boolean OneResult = true;
  	    ResultSet rs = null;
	      try {
	    	  rs =BDDManager.select(Driver,Host, Port, User, Password , DatabaseName ,Request);

	    	  if(!rs.isClosed() && rs.next()){
	    		  // If result is more than 1 row and more than 1 column, write it in an excel file
	    		  rs.last();
	    		  final int size = rs.getRow();
	    		  rs.beforeFirst();

		    	  if(rs.getMetaData().getColumnCount() > 1 || size > 1){
		    		  //make csv
		    		  OneResult = false;
		    		  try{
		    			  this.writeInCsv(rs);
			    	  } catch (FileNotFoundException e) {
						  return new StepReturn(StepReturnEnum.ERROR, "Error during file result creation", e);
					  }
		    	  }else{
		    		  //display in report
		    		  OneResult = true;
		    		  rs.next();
		    		  final Object oLine = rs.getObject(1);
    				  if(oLine != null)
    					  line.append(oLine.toString());
		    	  }
		    	  rs.close();
	    	  }else{
	    		  return new StepReturn(StepReturnEnum.FAIL,"No result");
	    	  }


	      } catch (SQLException e) {
	    	  return new StepReturn(StepReturnEnum.FAIL,"Error during Sql request",e);
		  } catch (ClassNotFoundException e) {
			  return new StepReturn(StepReturnEnum.FAIL,"Error during connexion with database",e);
		  }

	      if(OneResult)
	    	  return new StepReturn(StepReturnEnum.PASS,"Result : '" + line + "'");
	      else
	    	  return new StepReturn(StepReturnEnum.PASS,"Result : <a href=\"" + this.stepName + ".csv\" download>" + this.stepName + ".csv</a>");
	}






	@Parameters("DatabaseDriver|Host|Port|User|Password|DatabaseName|Request|Store_variable_name")
	public StepReturn sqlSelectAndStore(String Datas){
		final String[] DataSplited = Datas.split("\\|");
		if(DataSplited.length != 8)
			return new StepReturn(StepReturnEnum.ERROR, "Expected : 8 arguments separated by a pipe ('|').<br/>Actual : " + DataSplited.length + " arguments found.");

		final DatabaseDriverEnum Driver = BDDManager.getDatabaseDriverEnum(DataSplited[0]);
		if(Driver == null)
			return new StepReturn(StepReturnEnum.ERROR, DataSplited[0] + " is not a database driver. please check the spelling");
		final String Host = DataSplited[1];
		final String Port = DataSplited[2];
		final String User = DataSplited[3];
		final String Password = DataSplited[4];
		final String DatabaseName = DataSplited[5];
		final String Request = DataSplited[6];
		final String Store_variable_name = DataSplited[7];

		final StringBuilder line = new StringBuilder();
	    ResultSet rs = null;
			try {
				rs = BDDManager.select(Driver,Host, Port, User, Password , DatabaseName ,Request);

	    	  if(rs.next()){
	    		  rs.last();
	    		  final int size = rs.getRow();
	    		  final int col = rs.getMetaData().getColumnCount();
	    		  rs.beforeFirst();
		    	  if(col > 1 || size > 1){
		    		  return new StepReturn(StepReturnEnum.FAIL, "Expected : 1 result with 1 column.<br/>Actual : " + size + " result(s) with " + col + " column.");
		    	  }else{
		    		  //store result
		    		  rs.next();
		    		  final Object oLine = rs.getObject(1);
    				  if(oLine != null)
    					  line.append(oLine.toString());

    				  this.liste.getStoredValues().put(Store_variable_name, line.toString());

		    	  }
		    	  rs.close();
	    	  }
			} catch (SQLException e) {
				return new StepReturn(StepReturnEnum.ERROR, "Error during Sql request",e);
			}catch (ClassNotFoundException e) {
				return new StepReturn(StepReturnEnum.FAIL,"Error during connexion with database",e);
			}

	      return new StepReturn(StepReturnEnum.PASS, "Store in variable '" + Store_variable_name + "' the value : " + line);
	}



	/**
	 * Use an SSH command on server.
	 * @param sshArgs SSH_File_Path|Host|User|Password
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("SSH_File_Path|Host|User|Password")
	public StepReturn ssh(String sshArgs, String path){
		final String[] DataSplited = sshArgs.split("\\|");
		if(DataSplited.length != 4)
			return new StepReturn(StepReturnEnum.ERROR, "Expected : 4 arguments separated by a pipe ('|').<br/>Actual : " + DataSplited.length + " arguments found.");

		final String SSHPath = DataSplited[0];
		final String Host = DataSplited[1];
		final String User = DataSplited[2];
		final String Password = DataSplited[3];


		final File sshFile = new File(Paths.get(path,SSHPath).toString());

		Scanner lecteur ;
		try {
			lecteur = new Scanner(sshFile);
		} catch (FileNotFoundException e) {
			return new StepReturn(StepReturnEnum.ERROR,"SSH file not found or is a directory",e);
		}

		final SSHManager instance = new SSHManager(User, Password, Host, "");
		try{
		instance.connect();
		} catch (JSchException e){
			lecteur.close();
			return new StepReturn(StepReturnEnum.ERROR, "Enable to connect with SSH to the server '" + Host + "'",e);
		}

	    final StringBuilder CommandConcat = new StringBuilder();
		while (lecteur.hasNextLine()) {
			if(!CommandConcat.toString().trim().isEmpty())
				CommandConcat.append(" && ");
			CommandConcat.append(lecteur.nextLine());
		}

		String DataChecked;
		try {
			DataChecked = this.liste.CheckStoredVariable(CommandConcat.toString());
		} catch (StoredVariableNotFound e) {
			lecteur.close();
    	    instance.close();
        	return new StepReturn(StepReturnEnum.ERROR,"Error during stored variable check",e);
		}

        CommandConcat.replace(0, CommandConcat.length(), DataChecked);

		Log.info("SSH command : " + CommandConcat);
		final String result = instance.sendCommand(CommandConcat.toString());
	    Log.info(result);
		lecteur.close();
	    instance.close();

		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * Execute soap request from file
	 * @param soapArgs Url|Xml_file_Path
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("WebService_Url|Xml_file_Name")
	public StepReturn soap(String soapArgs, String path){
		final String[] DataSplited = soapArgs.split("\\|");
		if(DataSplited.length != 2)
			return new StepReturn(StepReturnEnum.ERROR, "Expected : 2 arguments separated by a pipe ('|').<br/>Actual : " + DataSplited.length + " arguments found.");

		final String url = DataSplited[0];
		final String xmlPath = DataSplited[1];


		final File xmlFile = new File(Paths.get(path,xmlPath).toString());

		String xml = "";
		try {
			final byte[] encoded = Files.readAllBytes(Paths.get(path,xmlPath));
			xml = new String(encoded, Charset.forName("UTF-8"));
		} catch (FileNotFoundException e) {
			return new StepReturn(StepReturnEnum.ERROR,"File '" + xmlFile + "' not found");
		} catch (IOException e) {
			return new StepReturn(StepReturnEnum.ERROR,"File '" + xmlFile + "' not found");
		}


		final SoapManager sm = new SoapManager(url);
    	return sm.requestFromString(xml,xmlFile.getName());
	}



	/*
	 * assert and verify
	 */


	/**
	 * Check if alert is present and his text if wanted.
	 *
	 * @param data is_Alert_Present(true_or_false)|alert_text_expected(can_be_empty)
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("is_Alert_Present(true_or_false)|alert_text_expected(can_be_empty)")
	public StepReturn verifyAlert(String data){
		final String[] datas = data.split("\\|");
		if(datas.length > 2)
			return new StepReturn(StepReturnEnum.ERROR, "Expected : no more than 2 arguments separated by a pipe ('|').<br/>Actual : " + datas.length + " arguments found.");

		// Check if arguments are valid
		if(datas[0].toLowerCase().compareTo("true") != 0 && datas[0].toLowerCase().compareTo("false") != 0 )
			return new StepReturn(StepReturnEnum.ERROR, "Expected : First argument is 'true' or 'false' to check if alert must be present or not.<br/>Actual : Wrong argument : '" + datas[0] + "'.");

		// Is alert present ?
		final boolean IsAlertPresentExpected = datas[0].toLowerCase().compareTo("true") == 0 ? true : false;


		if(this.isAlertPresent() != IsAlertPresentExpected)
			if(IsAlertPresentExpected)
				return new StepReturn(StepReturnEnum.FAIL, "Expected : Alert must be found.<br/>Actual : Alert not found.");
			else
				return new StepReturn(StepReturnEnum.FAIL, "Expected : Alert must not be found.<br/>Actual : Alert found.");

		// Need text to verify ?
		if (datas.length == 2){
			return this.verifyAlertText(datas[2]);
		}

		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * Check if element is checked
	 *
	 * @param im IdentifierModel
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("true_or_false")
	public StepReturn verifyChecked(WebElementModel im, String Data){
		final boolean bchecked = Data.toLowerCase().compareTo("true") == 0 ? true:false;
		if((this.isChecked(im.getWebElement()) && bchecked)
		|| (!this.isChecked(im.getWebElement()) && !bchecked))
			return new StepReturn(StepReturnEnum.PASS);
		else{
			if(bchecked)
				return new StepReturn(StepReturnEnum.FAIL, "Expected : '" + im.getObjectName() + "' Must be checked.<br/>Actual : Not checked.");
			else
				return new StepReturn(StepReturnEnum.FAIL, "Expected : '" + im.getObjectName() + "' Must not be checked.<br/>Actual : Checked.");
		}


	}


	/**
	 * check if element exist
	 * Only pass cause an exception is triggered if element not exist
	 * @param im IdentifierModel
	 * @return state of this step (Pass, fail, ...)
	 */
	//@Parameters("")
	// code change by Vivek for 30624
	
	@Parameters("true_or_false")
	
	// code change by Vivek for 30624
	//public StepReturn verifyElementExist(WebElementModel im){
		//	return new StepReturn(StepReturnEnum.PASS);
//	}
	// code change by Vivek for 29005
	
	public StepReturn verifyElementExist(WebElementModel im, String Data){
		//return new StepReturn(StepReturnEnum.PASS);	
		boolean isObjectExistes = false;
		final boolean isVerifyElementExist = Data.toLowerCase(Locale.getDefault()).compareTo("true") == 0 ? true : false;
		if(im.tryGetWebElement(0).getStepReturn() == StepReturnEnum.PASS)
		 {
			 isObjectExistes = true; 
		 }
		   
		if((isObjectExistes && isVerifyElementExist) ||(!isObjectExistes && !isVerifyElementExist) ){
			 return new StepReturn(StepReturnEnum.PASS);
		}
		else {
			return new StepReturn(StepReturnEnum.FAIL ,"The parameter is FALSE and the element exists on webpage OR The parameter is TRUE and element does not exist on the webpage");
		}
	}

	// END :code change by Vivek for 29005
	

	/**
	 * Check attribute of WebElement.
	 *
	 * @param im IdentifierModel
	 * @param Datas Attribute=Value
	 * @return Pass if value = expected otherwise Fail
	 */
	@Parameters("attribute|value_Expected")
	public StepReturn verifyAttribute(WebElementModel im, String Datas){
		final String[] DataSplited = Datas.split("\\|");
		if(DataSplited.length != 2)
			return new StepReturn(StepReturnEnum.ERROR,  "Expected : 2 arguments separated by a pipe ('|').<br/>Actual : " + DataSplited.length + " arguments found.");

		if(im.getWebElement().getAttribute(DataSplited[0]) == null)
			return new StepReturn(StepReturnEnum.ERROR, "Expected : get attribute '" + DataSplited[0] + "' from '" + im.getObjectName() + "'.<br/>Actual : Attribute not found.");
		if(im.getWebElement().getAttribute(DataSplited[0]).compareTo(DataSplited[1]) == 0)
			return new StepReturn(StepReturnEnum.PASS);
		else
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Attribute '" + DataSplited[0] + "' must have value '" + DataSplited[1] + "'.<br/>Actual : Wrong value : '" + im.getWebElement().getAttribute(DataSplited[0]) + "'.");
	}

	/**
	 * Check text in WebElement.
	 *
	 * @param im IdentifierModel
	 * @param expectedText expected text on WebElement
	 * @return Pass if Text found = expectedText otherwise Fail
	 */
	@Parameters("text_Expected")
	public StepReturn verifyTextInObject(WebElementModel im, String data){
		final String text = im.getWebElement().getText();
		return this.foundTextInElement(text, data);
		}

	//Work Item 36016 starting code
	/**
	 * Check text format in WebElement with Regex.
	 *
	 * @param im IdentifierModel
	 * @param expected Regex
	 * @return Pass if Text in web element correspond expected Regex otherwise Fail
	 */
	@Parameters("Regex_Expected")
	public StepReturn verifyTextFormat(WebElementModel im, String data){
		Log.debug("Element getText : " + im.getWebElement().getText());
		Log.debug("Element getAttributeInnerHTML : " + im.getWebElement().getAttribute("innerHTML"));
		Log.debug("Element getAttributeOuterHTML : " + im.getWebElement().getAttribute("outerHTML"));
		Log.debug("Element getAttributeTextContent : " + im.getWebElement().getAttribute("textContent"));
		final String text = im.getWebElement().getText();
		//return this.foundTextInElement(text, data);
		final Pattern pattern = Pattern.compile(data);
		final Matcher matcher = pattern.matcher(text);
		if(matcher.find())
			return new StepReturn(StepReturnEnum.PASS, "Text '" + text + "' correspond to regular expression : " + data);
		else
			return new StepReturn(StepReturnEnum.FAIL, "Text '" + text + "' do not correspond to regular expression : " + data);
		}

	//Work Item 36016 end code	
	
	/**
	 * Check if text is present on page.
	 *
	 * @param Data text to search
	 * @return Pass if text found, Fail if text not found, otherwise warn if text found more than 1 time.
	 */
	@Parameters("text_Expected")
	public StepReturn verifyTextOnPage(String data){
		final String bodyText = this.webDriverProvider.getWebDriver().findElement(By.tagName("BODY")).getText();
		return this.foundTextInElement(bodyText, data);

	}


	/**
	 * Find text in Element
	 * @param text text
	 * @param textSearch textSearch
	 * @return stepReturn
	 */
	private StepReturn foundTextInElement(String text, String textSearch){
		final int count = text.split(textSearch).length -1;
		if(count == 1)
			return new StepReturn(StepReturnEnum.PASS);
		else if(count > 1)
			return new StepReturn(StepReturnEnum.PASS, "Text '" + textSearch + "' found " + count + " times.");

		if(text.contains(textSearch))
			return new StepReturn(StepReturnEnum.PASS);
		else {
			Log.info("Expected Text on page = '" + textSearch + "' not found");
			Log.info("Actual Text on page = " + text);
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Text '" + textSearch + "' visible on page.<br/>Actual : Text not found.");		
		}
		

	}

	/**
	 * Check Selected item by label.
	 *
	 * @param im IdentifierModel
	 * @param Datas label=LabelExpected|LabelExpected|LabelExpected...
	 * @return Pass if all label was found, otherwise Fail
	 */
	@Parameters("label|label|...")
	public StepReturn verifySelectedLabel(WebElementModel im, String Datas){
		final List<String> DataSplited = new ArrayList<String>(Arrays.asList(Datas.split("\\|")));
		final Select select = new Select (im.getWebElement());
		final List<WebElement> list = select.getAllSelectedOptions();
		final ArrayList<String> listLabel = new ArrayList<String>();
		for (final WebElement we : list) {
			listLabel.add(we.getText());
		}
		for (final String item : DataSplited) {
			if(!listLabel.contains(item))
				return new StepReturn(StepReturnEnum.FAIL, "Expected : Label '" + item + "' must be selected.<br/>Actual : '" + item + "' not selected.");
		}
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * Check Selected item by label.
	 *
	 * @param im IdentifierModel
	 * @param Datas value=ValueExpected|ValueExpected|ValueExpected...
	 * @return Pass if all label was found, otherwise Fail
	 */
	@Parameters("value|value|...")
	public StepReturn verifySelectedValue(WebElementModel im, String Datas){
		final List<String> DataSplited = new ArrayList<String>(Arrays.asList(Datas.split("\\|")));
		final Select select = new Select (im.getWebElement());
		final List<WebElement> list = select.getAllSelectedOptions();
		final ArrayList<String> listValue = new ArrayList<String>();
		for (final WebElement we : list) {
			listValue.add(we.getAttribute("value"));
		}
		for (final String item : DataSplited)  {
			if(!listValue.contains(item))
				return new StepReturn(StepReturnEnum.FAIL, "Expected : Value '" + item + "' must be selected.<br/>Actual : '" + item + "' not selected..");
		}
		return new StepReturn(StepReturnEnum.PASS);
	}

	/**
	 * Check if expected text is at expected location.
	 *
	 * @param im IdentifierModel
	 * @param Datas expectedText|Row|Column
	 * @return Pass if element was found, else fail
	 */
	@Parameters("row_number|column_number|text_Expected")
	public StepReturn verifyElementInTable(WebElementModel im, String Datas){
		final String[] DataSplited = Datas.split("\\|");
		if(DataSplited.length != 3)
			return new StepReturn(StepReturnEnum.ERROR, "Expected : 3 arguments separated by a pipe ('|').<br/>Actual : " + DataSplited.length + " arguments found.");

		final int iRow = Integer.parseInt(DataSplited[0]);
		final int iCol = Integer.parseInt(DataSplited[1]);
		final String expectedText = DataSplited[2];


		//final List<WebElement> tableRows = im.getWebElement().findElements(By.tagName("tr"));
		final List<WebElement> tableRows = (List<WebElement>) this.webDriverProvider.getWait().until(new Function<WebDriver,List<WebElement>>() {
			@Override
			public List<WebElement> apply(WebDriver driver) {
				return driver.findElements(By.tagName("tr"));
			}
		});
		final WebElement Row = tableRows.get(iRow);
		if(Row == null)
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Table(" + iRow + "," + iCol + ") : '" + expectedText + "'.<br/>Actual : Row number '" + iRow + "' not found");
		//final List<WebElement> tableCol = Row.findElements(By.tagName("td"));
		final List<WebElement> tableCol = (List<WebElement>) this.webDriverProvider.getWait().until(new Function<WebDriver,List<WebElement>>() {
			@Override
			public List<WebElement> apply(WebDriver driver) {
				return driver.findElements(By.tagName("td"));
			}
		});
		final WebElement Col = tableCol.get(iCol);
		if(Col == null)
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Table(" + iRow + "," + iCol + ") : '" + expectedText + "'.<br/>Actual : Col number '" + iRow + "' not found");

		final String Text = Col.getText();

		if(Text != null && expectedText.compareTo(Text) == 0)
			return new StepReturn(StepReturnEnum.PASS);
		else
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Table(" + iRow + "," + iCol + ") : '" + expectedText + "'.<br/>Actual : '" + Text + "'.");
	}

	/**
	 * Check if WebElement is visible.
	 *
	 * @param im IdentifierModel
	 * @param Data true or false
	 * @return Pass if visible and expected
	 */
	@Parameters("true_or_false")
	public StepReturn verifyVisible(WebElementModel im, String Data){
		final boolean isVisibleExpected = Data.toLowerCase(Locale.getDefault()).compareTo("true") == 0 ? true : false;
		final boolean isVisible = im.getWebElement().isDisplayed();

		if(isVisible == isVisibleExpected)
			return new StepReturn(StepReturnEnum.PASS);
		else{
			if(isVisibleExpected)
				return new StepReturn(StepReturnEnum.FAIL, "Expected : '" + im.getObjectName() + "' must be visible.<br/>Actual : Not visible.");
			else
				return new StepReturn(StepReturnEnum.FAIL, "Expected : '" + im.getObjectName() + "' must not be visible.<br/>Actual : Visible.");
		}
	}

	/**
	 * Verify page's title.
	 * @param expectedTitle Expected title
	 * @return Pass if Title is as expected, else fail
	 */
	@Parameters("expected_Page_Title")
	public StepReturn verifyPageTitle(String expectedTitle){
		final String title = this.webDriverProvider.getWebDriver().getTitle();

		if(title.compareTo(expectedTitle) == 0)
			return new StepReturn(StepReturnEnum.PASS);
		else
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Title of page must be '" + expectedTitle + "'.<br/>Actual : Wrong title '" + title + "'.");
	}

	/**
	 * Switch to another window/tab by his name or his partial name
	 *
	 * @param pageName to switch
	 * @return state of this step (Pass, fail, ...)
	 */
	@Parameters("Page Name")
	//http://stackoverflow.com/questions/36419200/can-i-switch-between-windows-by-its-page-title-using-selenium-webdriver-in-java
	public StepReturn switchScreen(String pageName){
		boolean findTab = false;
        ArrayList<String> allTabHandles = new ArrayList<String>(webDriverProvider.getWebDriver().getWindowHandles());
        for(String handle : allTabHandles){
        	String title = this.webDriverProvider.getWebDriver().switchTo().window(handle).getTitle();
        	Log.info("Action switchTo : Page name search : " + pageName + ", name found : " + title);
    		if(title.toLowerCase().contains(pageName.toLowerCase())){
    			findTab = true;
        		break;
        	}
        }
        
		if(findTab)
    		return new StepReturn(StepReturnEnum.PASS);
		else
        	return new StepReturn(StepReturnEnum.FAIL, "Page Name : " + pageName + " not found, impossible to switch on.");
		
	}

	/*
	 * private method
	 */

	private boolean isAlertPresent() {
	    try {
	    	this.webDriverProvider.getWebDriver().switchTo().alert();
	      return true;
	    } catch (NoAlertPresentException e) {
	      return false;
	    }
	}

	private StepReturn verifyAlertText(String expectedText) {
		if(expectedText == null || expectedText.trim().isEmpty())
			return new StepReturn(StepReturnEnum.ERROR, "Expected alert text is empty");

		final Alert alert = this.webDriverProvider.getWebDriver().switchTo().alert();
		if(alert == null)
			return new StepReturn(StepReturnEnum.ERROR, "Alert not found");

    	final String alertText = alert.getText();
		if(alertText.compareTo(expectedText) != 0)
			return new StepReturn(StepReturnEnum.FAIL, "Expected : Alert text : '" + expectedText + "'.<br/>Actual : '" + alertText + "'.");

		return new StepReturn(StepReturnEnum.PASS);
	}




	private boolean isChecked(WebElement we){
		  if(we.isSelected())
			  return true;
		  else
			  return false;
	}

	private void writeInCsv(ResultSet rs) throws FileNotFoundException, SQLException{
		PrintWriter writer = null;
		writer = new PrintWriter(new FileOutputStream(Paths.get(this.reportPath,this.report.getReportName(),this.stepName + ".csv").toString(), false));

		final StringBuilder header = new StringBuilder();
		for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
			header.append(rs.getMetaData().getColumnName(i));
			if(i < rs.getMetaData().getColumnCount())
				header.append(';');
		}
		writer.println(header);
		final StringBuilder line = new StringBuilder();
		while ( rs.next() ) {
			// data;data;data;...
			line.replace(0, line.length(), "");
			for(int i = 1; i <= rs.getMetaData().getColumnCount(); i++){
				final Object oLine = rs.getObject(i);
				if(oLine != null)
					line.append(oLine.toString());
				if(i < rs.getMetaData().getColumnCount())
					line.append(';');
				}
				writer.println(line);
		}

		if(writer != null)
			writer.close();
	}


	public StepReturn getIndexInTable(WebElementModel im, String varName){
		final String xpath = im.getXPath();  	// code change by Vivek for 55317		
		int index = -1;
		if(xpath.contains("table"))
			index = getIndexInTableFromXPath(xpath);
	//this.webDriverProvider.getWebDriver().findElements(By.xpath("//table[not(descendant::table)and.//text()[contains(., '" +  + ""')]]"));
		this.liste.getStoredValues().put(varName, String.valueOf(index));
		Log.info("stored var add : " + index);
		if(index >= 0)
			return new StepReturn(StepReturnEnum.PASS);
		else
			return new StepReturn(StepReturnEnum.FAIL);
	}


	private int getIndexInTableFromXPath(String xpath) {
		//final Pattern pattern = Pattern.compile("(table/tbody/tr\\[(\\d?)\\]|table/tr|table/tr\\[(\\d?)\\]td\\[(\\d?)\\])"); 	// code change by Vivek for 55317
		final Pattern pattern = Pattern.compile("(table/tbody/tr\\[(\\d?)\\]|table/tr|table/tr\\[(\\d?)\\]td\\[(\\d?)\\])|table.*/tbody/tr\\[(\\d?)\\]/td\\[(\\d?)\\]");  // code changed by Princi
		final Matcher matcher = pattern.matcher(xpath);
		if(matcher.find()){ 	// code change by Vivek for 55317
		 // code changed by Princi 
			if(matcher.group(0) != null){
				    char intChar = '\0';
					final String indexString = matcher.group(0);
					System.out.println("matcher string --> "+indexString);
					String[] tablesplit = indexString.split("/");
					if(tablesplit[2].contains("tr"))
					{
						System.out.println(" tablesplit[2] "+tablesplit[2].charAt(3));
						intChar = tablesplit[2].charAt(3);
					}
					final int index = Character.getNumericValue(intChar);
					return index;
		  // end of code changed by Princi
			}else if(matcher.group(2) != null){
					final String indexString = matcher.group(2);
					final int index = Integer.parseInt(indexString);
					return index;
				}else
					return 0;
					
			}
			return -1;
	}


	// code change by Vivek for 57107

			@Parameters("xCoordinate|yCoordinate")
			public StepReturn getCoordinatesInTable(WebElementModel im, String varName){
				final String xpath = im.getXPath();  
				Log.info("xpath ====>  "+xpath);
				String coordinate = null;
				if(xpath.contains("table"))
					coordinate = getCoordinatesInTableFromXPath(xpath);
				//this.webDriverProvider.getWebDriver().findElements(By.xpath("//table[not(descendant::table)and.//text()[contains(., '" +  + ""')]]"));
				String coordinateArray[] = varName.split("\\|");
				String xCoordinate = coordinateArray[0];
				String yCoordinate = coordinateArray[1];
				//this.liste.getStoredValues().put(varName, String.valueOf(cordinate));
				Log.info("stored var add : " + coordinate);
				String coordinateVal[] = coordinate.split("\\|");
				String xCoordinateVal = coordinateVal[0].trim();
				String yCoordinateVal = coordinateVal[1].trim();
				Log.info("XCooridnate value ===>>  "+xCoordinateVal);
				Log.info("YCooridnate value ===>>  "+yCoordinateVal);
				this.liste.getStoredValues().put(xCoordinate, String.valueOf(xCoordinateVal));
				this.liste.getStoredValues().put(yCoordinate, String.valueOf(yCoordinateVal));				
				if(coordinate != null)
					return new StepReturn(StepReturnEnum.PASS);
				else
					return new StepReturn(StepReturnEnum.FAIL);
			}

			
			@SuppressWarnings("unused")
			private String getCoordinatesInTableFromXPath(String xpath) {
				//final Pattern pattern = Pattern.compile("((table/tbody/tr\\[(\\d?)\\]/td\\[(\\d?)\\])|table/tbody/tr\\[(\\d?)\\]|table/tr|table/tr\\[(\\d?)\\]td\\[(\\d?)\\])"); 	
				String xPathArray[] = xpath.split("\\/");
				String coordinate = null;
				int trVal=0;
				int tdVal=0;
				for(String xpathContains : xPathArray){
					if(xpathContains.contains("tr")){
						trVal = Integer.parseInt(xpathContains.replaceAll("[\\D]", ""));
						Log.info("row value ===>>  "+trVal);						
					}
					if(xpathContains.contains("td")){
						tdVal = Integer.parseInt(xpathContains.replaceAll("[\\D]", ""));
						Log.info("column value ===>>  "+tdVal);						
					}
				}				
					coordinate = trVal+"|"+" "+tdVal;
				Log.info("coordinates value ===>>   "+coordinate);
			return coordinate;
			}
			
			// end code change by Vivek for 57107


			// code changed by Princi for WI - 54875
			
			@Parameters("Date + 2M|dd/MM/yyyy|MyDate OR Time + 3h|HH:mm:ss|MyTime OR + ${MyIndex}+1||MyIndex")
			public StepReturn compute(String varName){		
				Date date = new Date();
				Log.info("system date and time--> "+varName);
				Calendar cal = Calendar.getInstance();		
				String[] splittedData = varName.split("\\|");
				String storedVar = splittedData[2].trim();
				Log.info("Stored Variable --> "+storedVar);
				String format = splittedData[1].trim();
				Log.info("format --> "+format);
				String compute = splittedData[0].trim();
				Boolean changeDateOrTime = false;
				if(compute.contains("+"))
					changeDateOrTime = true;
				String actionPerform = null;
				if(format.contains("y") || format.contains("Y"))
					actionPerform = "dateCompute";
				else if(format.contains("h") || format.contains("H"))
					actionPerform  = "timeCompute";
				else if(format.contains(""))
					actionPerform = "numberCompute";
				switch(actionPerform)
				{
				case "dateCompute":
				{
					//TODO delete if and catch  the exeception if format isn't valide :
					//java.lang.IllegalArgumentException: Illegal pattern character 'x'  
					if(format.equals("dd/MM/yyyy")||format.equals("dd-MM-yyyy")||format.equals("dd.MM.yyyy")||format.equals("yyyy/MM/dd")||format.equals("yyyy-MM-dd")||format.equals("yyyy.MM.dd")||format.equals("MM/dd/yyyy")||format.equals("MM-dd-yyyy")||format.equals("MM.dd.yyyy")||format.equals("dd MMM yyyy"))
					{
						SimpleDateFormat sdf = new SimpleDateFormat(format);
						if(changeDateOrTime == true) {
							String[] changeDate = compute.split("\\+");					
							cal.setTime(date);											
							String dateExtend = changeDate[1].trim();
							Log.info("Date extend --> "+dateExtend);
							String dateExtendBy[] = dateExtend.split("");					
							Log.info("1st argument after split "+dateExtendBy[0]);
							Log.info("2nd argument after split  "+dateExtendBy[1]);
							String number = dateExtendBy[0].trim();
							Log.info("Date Number --> "+number);
							if (dateExtend.contains("d")|| dateExtend.contains("D"))
								cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(number));
							if (dateExtend.contains("m")|| dateExtend.contains("M"))
								cal.add(Calendar.MONTH, Integer.parseInt(number));
							if (dateExtend.contains("y")|| dateExtend.contains("Y"))
								cal.add(Calendar.YEAR, Integer.parseInt(number));
							Date newDate = cal.getTime();					
							String currDate = sdf.format(newDate);
							Log.info("new date after format --> "+currDate);	
							this.liste.getStoredValues().put(storedVar, String.valueOf(currDate));
							Log.info("Date stored in the variable after computation --> "+this.liste.getStoredValues().get(storedVar));
							return new StepReturn(StepReturnEnum.PASS);
												
						}
						else
						{
							Log.info("Date to change the format --> "+date);
							String currDate = sdf.format(date);
							Log.info("Date after format --> "+currDate);
							this.liste.getStoredValues().put(storedVar, String.valueOf(currDate));
							Log.info("Date stored in the variable after computation --> "+this.liste.getStoredValues().get(storedVar));
							return new StepReturn(StepReturnEnum.PASS);
						}
					}
					else{
						Log.info("Please provide right format for Date, valid formats are - 'dd/MM/yyyy' or 'dd-MM-yyyy' or 'dd.MM.yyyy' or 'yyyy/MM/dd' or 'yyyy-MM-dd' or 'yyyy.MM.dd' or 'MM/dd/yyyy' or 'MM-dd-yyyy' or 'MM.dd.yyyy' or 'dd MMM yyyy' ");
						return new StepReturn(StepReturnEnum.FAIL);
					}
					//break;
				  } 
				
				case "timeCompute":
				{			
					if(format.equals("HH:mm:ss")||format.equals("hh:mm:ss a"))
					{
						SimpleDateFormat sdf = new SimpleDateFormat(format);
						if(changeDateOrTime == true) {
							String[] changeTime = compute.split("\\+");					
							cal.setTime(date);											
							String timeExtend = changeTime[1].trim();
							Log.info("Time extend --> "+timeExtend);
							String timeExtendBy[] = timeExtend.split("");
							Log.info("number to compute the time --> "+timeExtendBy[0]);
							String number = timeExtendBy[0].trim();
							if (timeExtend.contains("h")|| timeExtend.contains("H"))
								cal.add(Calendar.HOUR, Integer.parseInt(number));
							if (timeExtend.contains("m")|| timeExtend.contains("M"))
								cal.add(Calendar.MINUTE, Integer.parseInt(number));
							if (timeExtend.contains("s")|| timeExtend.contains("S"))
								cal.add(Calendar.SECOND, Integer.parseInt(number));
							Date newTime = cal.getTime();					
							String currTime = sdf.format(newTime);
							Log.info("new time after format --> "+currTime);					
							this.liste.getStoredValues().put(storedVar, String.valueOf(currTime));					
							Log.info("Time stored in the variable after computation --> "+this.liste.getStoredValues().get(storedVar));
							return new StepReturn(StepReturnEnum.PASS);				
						}
						else
						{
							Log.info("Time to change the format --> "+date);
							String currTime = sdf.format(date);
							Log.info("Time after format --> "+currTime);
							this.liste.getStoredValues().put(storedVar, String.valueOf(currTime));
							Log.info("Time stored in the variable after computation --> "+this.liste.getStoredValues().get(storedVar));
							return new StepReturn(StepReturnEnum.PASS);
						}
					}
					else{
						Log.info("Please provide right format for Time, valid formats are - 'HH:mm:ss' or 'hh:mm:ss a' ");
						return new StepReturn(StepReturnEnum.FAIL);
					}
					//break;
				  } 
				case "numberCompute":
				{
						if(compute.startsWith("-"))
						{
							Log.info("No index present ");
							return new StepReturn(StepReturnEnum.FAIL);
						}
						else{
					        String actionOnNumber = null;
					        if(compute.contains("+"))
					        	actionOnNumber = "add";
					        else if(compute.contains("-"))
					        	actionOnNumber = "subtract";
					        else if(compute.contains("*"))
					        	actionOnNumber = "multiply";
					        else if(compute.contains("/"))
					        	actionOnNumber = "divide";
					        	switch (actionOnNumber)
					        	{
					        	case "add":
					        	{
					        		String[] indexArray = compute.split("\\+");									
									String index = indexArray[0].trim();
									Log.info("My Index --> "+index);
									String number = indexArray[1].trim();
									Log.info("Number --> "+number);
									int computedNumber = Integer.parseInt(index)+Integer.parseInt(number);
									Log.info("ComputedNumber --> "+computedNumber);
									this.liste.getStoredValues().put(storedVar, String.valueOf(computedNumber));
									Log.info("Index stored in the variable after computation --> "+this.liste.getStoredValues().get(storedVar));
									return new StepReturn(StepReturnEnum.PASS);	
									//break;
					        	}
					        	case "subtract":
					        	{
					        		String[] indexArray = compute.split("\\-");									
									String index = indexArray[0].trim();
									Log.info("My Index --> "+index);
									String number = indexArray[1].trim();
									Log.info("Number --> "+number);
									int computedNumber = Integer.parseInt(index)-Integer.parseInt(number);
									Log.info("ComputedNumber --> "+computedNumber);
									this.liste.getStoredValues().put(storedVar, String.valueOf(computedNumber));
									Log.info("Index stored in the variable after computation --> "+this.liste.getStoredValues().get(storedVar));
									return new StepReturn(StepReturnEnum.PASS);	
									//break;
					        	}
					        	case "multiply":
					        	{
					        		String[] indexArray = compute.split("\\*");									
									String index = indexArray[0].trim();
									Log.info("My Index --> "+index);
									String number = indexArray[1].trim();
									Log.info("Number --> "+number);
									int computedNumber = Integer.parseInt(index)*Integer.parseInt(number);
									Log.info("ComputedNumber --> "+computedNumber);
									this.liste.getStoredValues().put(storedVar, String.valueOf(computedNumber));
									Log.info("Index stored in the variable after computation --> "+this.liste.getStoredValues().get(storedVar));
									return new StepReturn(StepReturnEnum.PASS);	
									//break;
					        	}
					        	case "divide":
					        	{
					        		String[] indexArray = compute.split("\\/");									
									String index = indexArray[0].trim();
									Log.info("My Index --> "+index);
									String number = indexArray[1].trim();
									Log.info("Number --> "+number);
									int computedNumber = Integer.parseInt(index)/Integer.parseInt(number);
									Log.info("ComputedNumber --> "+computedNumber);
									this.liste.getStoredValues().put(varName, String.valueOf(computedNumber));
									Log.info("Index stored in the variable after computation --> "+this.liste.getStoredValues().get(storedVar));
									return new StepReturn(StepReturnEnum.PASS);	
									//break;
					        	}
					        	
					        	}					
						break;	
						}
				  }
				default: 
				{
					Log.info("Wrong Format, Please provide correct format in Data Column");
					return new StepReturn(StepReturnEnum.FAIL);	
				}
				}
				if(this.liste.getStoredValues().get(storedVar)!=null ||this.liste.getStoredValues().get(storedVar)!="")			
				return new StepReturn(StepReturnEnum.PASS);
				
				else {
					Log.info("Please provide the variable to store the result ");
					return new StepReturn(StepReturnEnum.FAIL);
				}
			}
			
			// end of code changed by Princi for WI - 54875
			
			
			// start of code changed by Cyril for WI - 71265
			/**
			 * The scroll method (horizontal|vertical)
			 *
			 * @param Datas Horizontal Pixel|Vertical Pixel
			 * @return state of this step (Pass, fail, ...)
			 */
			@Parameters("Horizontal Pixel|Vertical Pixel")
			public StepReturn scroll(String Datas){
				final String[] dataSplited = Datas.split("\\|");
				if(dataSplited.length != 2)
					return new StepReturn(StepReturnEnum.ERROR, "Expected : 2 arguments separated by a pipe ('|').<br/>Actual : " + dataSplited.length + " arguments found.");

				final int iHorizontal = Integer.parseInt(dataSplited[0]);
				final int iVertical = Integer.parseInt(dataSplited[1]);
		        final JavascriptExecutor jse = (JavascriptExecutor)this.webDriverProvider.getWebDriver();
		        //jse.executeScript("scroll(0,1200)"); //OK
		        //jse.executeScript("window.scrollBy(0,1200)"); //OK
		        //jse.executeScript("window.scrollTo(0,1200)"); //OK
		        final String jseCommand = "scroll(" + iHorizontal + "," + iVertical + ")";
		        Log.info("Java Script Instruction executed is : " + jseCommand);
		        jse.executeScript(jseCommand);
		        return new StepReturn(StepReturnEnum.PASS);
			}			
			// end of code changed by Cyril for WI - 71265
	
			// start of code changed by Cyril for WI - xxxxxx storeTextBetweenDelimiters and wait actions
			/**
			 * Store value of a string between to string in variable to use later.
			 *
			 * @param TextBefore String before the string to store 
			 * @param TextAfter String before the string to store 
			 * @param VarName Variable's name where the Text between the two preceding will be store in
			 * @return state of this step (Pass, fail, ...)
			 */
			@Parameters("Text_Before|Text_After|variable_Name")
			// https://stackoverflow.com/questions/13796451/how-to-extract-a-string-between-two-delimiters
			public StepReturn storeTextBetweenDelimiters(String Datas){
				//replace //n by /n in Datas
				String replacedString = Datas.replace("\\n", "\n");
				final String[] dataSplited = replacedString.split("\\|");
				if(dataSplited.length != 3)
					return new StepReturn(StepReturnEnum.ERROR, "Expected : 3 arguments separated by a pipe ('|').<br/>Actual : " + dataSplited.length + " arguments found.");

				final String TextBefore = dataSplited[0];
				final String TextAfter = dataSplited[1];
				final String varName = dataSplited[2];
				final String bodyText = this.webDriverProvider.getWebDriver().findElement(By.tagName("BODY")).getText();
				String storedValue;
				try {
					storedValue = bodyText.substring(bodyText.indexOf(TextBefore) + TextBefore.length(), bodyText.indexOf(TextAfter));
					this.liste.getStoredValues().put(varName, storedValue);
					return new StepReturn(StepReturnEnum.PASS, "Store in variable '" + varName + "' the value : " + storedValue);
				} catch (Exception e) {
					e.printStackTrace();
					return new StepReturn(StepReturnEnum.FAIL, "Store in variable '" + varName + "' impossible, text to store not found");
				}

			}
			
			
			/**
			 * Wait a time in milliseconds.
			 *
			 * @param Time in milliseconds 
			 * @return state of this step (Pass, fail, ...)
			 * @throws InterruptedException 
			 */
			@Parameters("Time in milliseconds")
			// https://stackoverflow.com/questions/24104313/how-to-delay-in-java
			public StepReturn wait(String Datas) throws InterruptedException{
				final long iTime = Long.parseLong(Datas);
				TimeUnit.MILLISECONDS.sleep(iTime);
				return new StepReturn(StepReturnEnum.PASS);
			}
			// end of code changed by Cyril for WI - xxxxxx storeTextBetweenDelimiters and wait actions

			/**
			 * Get element by label with page object and click
			 * @param im WebElementModel
			 * @param label label 
			 * @return stepReturn
			 */
			@Parameters("Label")
			public StepReturn getElementByLabelAndClick(WebElementModel im, String label) {
								
				WebDriver driver = this.webDriverProvider.getWebDriver();
				
				WebElement webElement = null;

				String xpathToSearch = "//*[contains(text(),'" + label + "')]";

				// Get Element Title that user want
				List<WebElement> webElements = driver.findElements(By
						.xpath(xpathToSearch));
				
				// If there is no Element found, we return a StepReturn ERROR
				if (webElements.size() > 0) {
					webElement = webElements.get(0);
				} else {
					Log.error("There is no Element with text \"" + label + "\"");
					return new StepReturn(StepReturnEnum.FAIL);
				}	

				// Generate xPath of web element found by label
				String xPathElement = generateXPATH(webElement, "");

				String partSameXpathCorrespondant = "";

				WebElement webElementButtonCorrespondant = null;

				// Search Button Element of title element sought
				for (WebElement webElementButton : im.getWebElements()) {
					
					// Generate xpath of webElements
					String xPathElementButton = generateXPATH(webElementButton, "");
					
					// If there is not error, we search webElement button corresponding of webElementLabel
					if (xPathElementButton != null) {
						String partSameXpath = findLongestPrefixSuffix(xPathElement,
								xPathElementButton);
						if (partSameXpathCorrespondant.length() < partSameXpath
								.length()) {
							partSameXpathCorrespondant = partSameXpath;
							webElementButtonCorrespondant = webElementButton;
						}
					}
				}
				
				if(webElementButtonCorrespondant != null) {
					try {
						webElementButtonCorrespondant.click();
					} catch (Exception e) {
						Log.error("Error when clicking item");
						Log.error("Error : " + e.getMessage());
					}
				} else {
					return new StepReturn(StepReturnEnum.FAIL);
				}
				
				return new StepReturn(StepReturnEnum.PASS);
			}

			
			/**
			 * generate xpath of WebElement.
			 * 
			 * @param childElement
			 * @param current
			 * @return
			 */
			private String generateXPATH(WebElement childElement, String current) {
				try {
					String childTag = childElement.getTagName();
					if (childTag.equals("html")) {
						return "/html[1]" + current;
					}
					WebElement parentElement = childElement.findElement(By.xpath(".."));
					List<WebElement> childrenElements = parentElement.findElements(By
							.xpath("*"));
					int count = 0;
					for (int i = 0; i < childrenElements.size(); i++) {
						WebElement childrenElement = childrenElements.get(i);
						String childrenElementTag = childrenElement.getTagName();
						if (childTag.equals(childrenElementTag)) {
							count++;
						}
						if (childElement.equals(childrenElement)) {
							return generateXPATH(parentElement, "/" + childTag + "["
									+ count + "]" + current);
						}
					}
				} catch (Exception e) {
					Log.error("Fail to generate xpath of webElement");
					Log.error("Exception : " + e);
					return null;
				}
				return null;
			}

			/**
			 * Select part of String corresponding of Two String.
			 * 
			 * @param s1
			 *            first String
			 * @param s2
			 *            seconde String
			 * @return String corresponding
			 */
			private String findLongestPrefixSuffix(String s1, String s2) {
				for (int i = Math.min(s1.length(), s2.length());; i--) {
					if (s2.startsWith(s1.substring(0, i))) {
							return s1.substring(0, i);
					}
				}
			}
			
			//start of code changed by Princi for WI - 76628
			public StepReturn displayValue(WebElementModel im){	    	
				final String xpath = im.getXPath();
				String displayTextVal = this.webDriverProvider.getWebDriver().findElement(By.xpath(xpath)).getText();
				if(null!= displayTextVal){					
				Log.info("Object Value is ---> "+displayTextVal);	
				return new StepReturn(StepReturnEnum.PASS, "Display Value of Object is  '" + displayTextVal);
				}					
				else
					return new StepReturn(StepReturnEnum.FAIL, "Object not found.");		
			}
		  //end of code changed by Princi for WI - 76628
			
			
			
			//start of code changed by Princi for WI - 76628
			public StepReturn displayValue(String var){
				
				if(null!=var){
					this.liste.getStoredValues().put(var, String.valueOf(var));
					return new StepReturn(StepReturnEnum.PASS, "Display Value of variable is  '" + this.liste.getStoredValues().get(var));
				}
				else
					return new StepReturn(StepReturnEnum.FAIL, "Variable not exist.");		
			}
		  //end of code changed by Princi for WI - 76628
			
			
		    
		 // start code changed by Princi for WI - 76654
		    public StepReturn clickSikuli(String image){
		    	Log.info("Image name is --->   "+image);
		    	String imgPath = sikuliImagePath+"/"+image;
		    	Log.info("Image path is ---->  "+imgPath);
		    	File imgFile = new File(imgPath);
		    	if(!imgFile.exists()){
		    		String testCase[] = report.getTestName().split(" "); 
		    		String testImgDir = testCase[0].replace(".xlsm", "").trim();;
		    		Log.info("TestCase Name ---->  "+testImgDir);		    		
		    		Log.info("Test case image directory name --->  "+testImgDir);
		    		imgPath = testLibraryPath+"/"+testImgDir+"/"+image;
		    		Log.info("image path inside testcase folder --->   "+imgPath);
		    		imgFile = new File(imgPath);		    		
		    		if(!imgFile.exists()){
		    			Log.info("Image does not exist at any Path (Configuration/ORPicture  or  TestLibrary/test folder), please add the image first.");
		    			return new StepReturn(StepReturnEnum.FAIL);
		    		}
		    	}
		    	Screen screen = new Screen();
		    	Log.info("After Sikuli..1" );
		    	Log.info("Image File is --->  "+imgFile);		    	
		    	org.sikuli.script.Pattern imgPattern = new org.sikuli.script.Pattern(""+imgFile);		   
		    	Log.info("Screen Object is created ----> "+screen);
		    	try {
		    		screen.wait(imgPattern.similar((float)0.60),2).click();
				} catch (FindFailed e) {					
					Log.error("Exception in finding the image ---> "+e);
					return new StepReturn(StepReturnEnum.FAIL, "exception in finding the following image '" + image);
				}				
		    	return new StepReturn(StepReturnEnum.PASS);
		    }
		 // end code changed by Princi for WI - 76654
		     
		   // start code changed by Princi for WI -76630 
		    @Parameters("XObjectName|YObjectName")
		    public StepReturn getPixelCoordinate(WebElementModel im, String data){
		    	String[] coordinate= data.split("\\|");
		    	String xObject = coordinate[0].trim();
		    	String yObject = coordinate[1].trim();
		    	Log.info("xObject  --> "+xObject);
		    	Log.info("yObject  --> "+yObject);
		    	String xPath = im.getXPath();
		    WebElement obj = webDriverProvider.getWebDriver().findElement(By.xpath(xPath));
		    Point className = obj.getLocation();
		    int xCoordinate = className.getX();
		    Log.info("Element's position from left side ---> "+xCoordinate +"  pixel");
		    int yCoordinate = className.getY();
		    Log.info("Element's position from top ---> "+yCoordinate +"  pixel");
		    Log.info("XObjectName "+xCoordinate);
		    Log.info("YObjectName "+yCoordinate);
		   
		    this.liste.getStoredValues().put(xObject, String.valueOf(xCoordinate));
		    this.liste.getStoredValues().put(yObject, String.valueOf(yCoordinate));
		    Log.info("getPixelCoordinate ( XObjectName, YObjectName ) value is --->  "+"( "+this.liste.getStoredValues().get(xObject)+", "+this.liste.getStoredValues().get(yObject)+" )");		    
		    	return new StepReturn(StepReturnEnum.PASS);	
		    }
		    // end code changed by Princi for WI -76630		    
		        
		   
		   
		   // start code changed by Princi for WI - 76643
		   @Parameters("verifyVariableValue|value")
		    public StepReturn verifyValue(String data){
		    	Log.info("data coming in parameter --->  "+data);
		    	String[] dataArray = data.split("\\|");
		    	Log.info("data 1 =====>>  "+dataArray[0]);
		    	Log.info("data 2 =====>>  "+dataArray[1]);
		    	Boolean variable = isNumeric(dataArray[0]);
		    	Boolean value  = isNumeric(dataArray[1]);
		    	if(variable && value){
		    		Log.info("Variable and value both are integer ");
		    		int varInt = Integer.parseInt(dataArray[0]);
		    		int val = Integer.parseInt(dataArray[1]);
		    		if(varInt ==  val){
		    			Log.info("both the values are same ");
		    			return new StepReturn(StepReturnEnum.PASS);
		    		}
		    	}
		    	else 
		    	{
		    		if(dataArray[0].equals(dataArray[1])){
		    			Log.info("Variable and value both are string and equal ");
		    			return new StepReturn(StepReturnEnum.PASS);
		    		}
		    	}
		    	Log.info("Variable and value does not match.");
		    	return new StepReturn(StepReturnEnum.FAIL);
		    }
		
		 	
		   public static boolean isNumeric(String str)  
		   {  
		     try  
		     {  
		       double d = Double.parseDouble(str);  
		     }  
		     catch(NumberFormatException nfe)  
		     {  
		       return false;  
		     }  
		     return true;  
		   }   
		   
		   // end code changed by Princi for WI - 76643 	
		   
		// start of code changed by Princi for WI - 38836		   
		   @Parameters("${xCoordinate}|${yCoordinate}|value")
		    public StepReturn verifyValueInTable(WebElementModel im, String data){
			   Log.info("data coming in parameter for VerifyValueInTable  ===>>   "+data);
			   String dataArray[] = data.split("\\|");
			   String xCoordinate = dataArray[0];
			   String yCoordinate = dataArray[1];
			   String value =  dataArray[2];
			   Log.info("xCoordinate ===>>  "+xCoordinate);
			   Log.info("yCoordinate ===>>  "+yCoordinate);
			   Log.info("value in the parameter ===>>  "+value);
			   String xPath = im.getXPath();
			   Log.info("xPath --->>  "+xPath);
			   WebElement obj = webDriverProvider.getWebDriver().findElement(By.xpath(xPath));
			   Log.info("Object value captured in the table  ===>>>   "+obj.getText());
			   String newXPath = xPath.replaceAll("tr\\[(\\d?)\\]", "tr["+xCoordinate+"]");
			   String correctXPath = newXPath.replaceAll("td\\[(\\d?)\\]", "td["+yCoordinate+"]");
			   Log.info("New xPath ===>  "+correctXPath);
			   try{
			   obj = webDriverProvider.getWebDriver().findElement(By.xpath(correctXPath));
			   Log.info("Value to be matched with the parameter ===>>>   "+obj.getText());			   
			   } catch(Exception e){
				   Log.error("Exception raised row or column does not exist in the table "+e);
				   return new StepReturn(StepReturnEnum.FAIL);
			   }
			   //WebElement baseTable = webDriverProvider.getWebDriver().findElement(By.tagName("table"));
			   //WebElement tableRow = baseTable.findElement(By.xpath("//tr[1]")); //should be the third row
			   //WebElement cellIneed = tableRow.findElement(By.xpath("//td[1]"));
			   //String valueIneed = cellIneed.getText();
			   //Log.info("value is ===>>  "+valueIneed);
			   if(value.equals(obj.getText())){
				   Log.info("Both values are same ");
				   return new StepReturn(StepReturnEnum.PASS);
			   }
			   else
			   return new StepReturn(StepReturnEnum.FAIL);
		   }
		   
		   // end of code changed by Princi for WI - 38836
		   
		   
}
