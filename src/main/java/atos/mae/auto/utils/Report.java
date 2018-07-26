package atos.mae.auto.utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;
import com.relevantcodes.extentreports.NetworkMode;

import atos.mae.auto.plugins.requirement.StepReturn;
import atos.mae.auto.plugins.requirement.StepReturnEnum;
import atos.mae.auto.plugins.requirement.WebDriverProvider;
import atos.mae.auto.utils.Exceptions.StepReturnUndefined;
import atos.mae.auto.utils.Exceptions.TestNameUndefined;
import atos.mae.auto.utils.enums.TagReport;

/**
 * Report manager.
 */
@Component
public class Report {

	@Value("${reportPath:Report}")
	private String reportPath;

	@Value("${reportName:Reporting}")
	private String reportName;

	@Value("${reportConfig:Configuration/Report-config.xml}")
	private String reportConfig;

	@Autowired
	private WebDriverProvider webDriverProvider;

	/**
	 * Global reporter.
	 */
	private ExtentReports extent;

	/**
	 * Current test reporter.
	 */
	private ExtentTest test;

	/**
	 * Tag list to put in report.
	 */
	private ArrayList<String> TagList;

	/**
	 * Test name.
	 */
	private String TestName;

	/**
	 * Logger.
	 */
	private static Logger Log = Logger.getLogger(Report.class);

	/**
	 * Report's directory and html file name.
	 */
	private String completeReportName;

	/**
	 * Information list to put in report.
	 */
	private ArrayList<String> informations;


	private void init(){
		this.completeReportName = this.reportName + "_" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_kk_mm_ss"));
		this.extent = new ExtentReports(Paths.get(this.reportPath,this.completeReportName,this.completeReportName + ".html").toString(),NetworkMode.OFFLINE);
		try{
			this.extent.loadConfig(new File(this.reportConfig));
		}catch(NullPointerException e){
			Log.error(this.reportConfig + " not found, please use --setup command line on jar");
		}
		this.informations = new ArrayList<String>();
		//Pour supprimer les informations sur le poste, d�commenter les lignes ci-dessous
		this.extent.addSystemInfo("User Name", "");
		this.extent.addSystemInfo("Host Name", "");
		//this.extent.addSystemInfo("Java Version", "");
		//this.extent.addSystemInfo("OS", "");
	}

	/**
	 * Skip a test.
	 * @param TestName Test's name
	 * @param TestDescription Test's description
	 */
	public void skipTest(String TestName, String TestDescription){
		if(this.extent == null)
			this.init();

		if(TestName == null)
			throw new TestNameUndefined();

		if(TestDescription == null)
			TestDescription = "";

		this.test = this.extent.startTest(TestName, TestDescription);
		this.test.log(LogStatus.SKIP, "");
		this.TagList = new ArrayList<>();
		this.extent.endTest(this.test);
		this.extent.flush();
	}

	/**
	 * Begin a test.
	 * @param TestName Test's name
	 * @param TestDescription Test's description
	 */
	public void startTest(String TestName ,String TestDescription){
		if(this.extent == null)
			this.init();

		if(TestName == null)
			throw new TestNameUndefined();

		if(TestDescription == null)
			TestDescription = "";

		this.TestName = TestName;
		setTestName(TestName);
		this.TagList = new ArrayList<>();


		try {
			this.test = this.extent.startTest(new String(TestName.getBytes("UTF-8")), new String(TestDescription.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			this.test = this.extent.startTest(TestName, TestDescription);
		}
		Log.info("Start Test '" + this.TestName + "'");
	}

	/**
	 * Report a step after his end.
	 * @param sr StepReturn, with state and error
	 * @param StepName Name of step from Excel
	 * @param StepDescription Description of step from Excel
	 * @param Identifier WebElement name from Excel
	 * @param Action Action done by step from Excel
	 * @param Data Data used by step from Excel
	 * @param AbortOnFail if test stop when StepReturn is fail
	 */
	public void setStep(StepReturn sr, String StepName, String StepDescription, boolean AbortOnFail, String screenCapturePath){
		if(sr == null)
			throw new StepReturnUndefined();

		if(sr.getStepReturn() == StepReturnEnum.DONOTLOG)
			return;

		LogStatus ls;

		switch(sr.getStepReturn()){
		case PASS:
			ls = LogStatus.PASS;
			break;
		case WARN:
			ls = LogStatus.WARNING;
			break;
		case FAIL:
			if(AbortOnFail)
				ls = LogStatus.FAIL;
			else
				ls = LogStatus.WARNING;
			break;
		case ERROR:
			ls = LogStatus.ERROR;
			break;
		default:
			ls = LogStatus.UNKNOWN;
			break;
		}

		final StringBuilder detailsReport = new StringBuilder("");
		final StringBuilder detailsLog = new StringBuilder("");
		detailsReport.append(StepDescription);

		this.setDetails(sr, StepName,ls, detailsReport, detailsLog, screenCapturePath);


		// step log
		Log.info("Step '" + StepName + "' : " + ls.toString() + " : " + detailsLog);


		try {
			this.test.log(ls, new String(StepName.getBytes("UTF-8")),new String(detailsReport.toString().getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			this.test.log(ls, StepName,detailsReport.toString());
		}
	}

	/*private void setDetails(StepReturn sr, String StepName, LogStatus ls,
			final StringBuilder detailsReport, final StringBuilder detailsLog) {
		this.setDetails(sr, StepName, ls, detailsReport, detailsLog, null);

	}*/

	private void setDetails(StepReturn sr, String StepName, LogStatus ls,
			final StringBuilder detailsReport, final StringBuilder detailsLog, String screenshotPath) {
		if(ls != LogStatus.PASS){
			if(sr.getInformation() == null || sr.getInformation().trim().isEmpty()){
				detailsReport.append("<br/>Expected : Chrome Version :-Chrome 57+ ; IE version :- IE 9+ ; Firefox version :- Firefox 52+;");
				detailsReport.append("<br/>Unknown Error, please refer to logs.");
			}else{
				detailsReport.append("<br/>").append(sr.getInformation().replace("Expected :", "<FONT COLOR=#DF7401><b><u>Expected :</u></b></FONT>").replace("Actual :", "<FONT COLOR=#DF0101><b><u>Actual :</u></b></FONT>"));
				detailsLog.append(sr.getInformation().replace("<br/>", " "));
			}

			if(sr.getException() != null){
				final StringWriter sw = new StringWriter();
				sr.getException().printStackTrace(new PrintWriter(sw));
				detailsLog.append(sw.toString());

			}

			if(screenshotPath == null)
				screenshotPath = this.webDriverProvider.takeScreenshot(this.completeReportName,this.TestName, StepName);

			if(screenshotPath != null)
				detailsReport.append("<br/>").append(this.test.addScreenCapture(screenshotPath));
		} else {
			if(sr.getInformation() != null){
				detailsReport.append("<br/>").append(sr.getInformation());
				detailsLog.append(sr.getInformation());
			}

			if(this.informations.size() > 0){
				detailsReport.append("<br/>");
				for (final String string : this.informations) {
					detailsReport.append("<br/><font color=\"#FFBF00\"><i class=\"fa fa-fa fa-exclamation-triangle\"></i></font> ").append(string);
					detailsLog.append(' ').append(string.replace("<br/>"," "));
				}
				this.informations = new ArrayList<String>();
			}

			if(screenshotPath != null)
				detailsReport.append("<br/>").append(this.test.addScreenCapture(screenshotPath));

		}
	}

	/**
	 * End test (not campaign) and write it in report.
	 */
	public void endtest(){
		if (this.test != null && this.extent != null){
			for (final String tag : this.TagList) {
				try {
					this.test.assignCategory(new String(tag.getBytes("UTF-8")));
				} catch (UnsupportedEncodingException e) {
					this.test.assignCategory(tag);
				}
			}
			this.TagList = new ArrayList<String>();
			this.test.assignCategory(this.webDriverProvider.getEnvExec().toString());
			// ending test
			this.extent.endTest(this.test);
			Log.info("End Test '" + this.TestName + "'");
			// writing everything to document
			this.extent.flush();
			this.test = null;
		}
	}

	protected void finalize() throws Throwable {
	     try {
	    	 this.extent.close();        // close open files
	     } finally {
	         super.finalize();
	     }
	 }

	/**
	 * add tag to report. It will be add in file with endTest method
	 * @param tag Tag to add
	 */
	public void addTag(TagReport tag){
		final String tagToAdd = tag.toString().trim();
		if(!this.TagList.contains(tagToAdd))
			this.TagList.add(tagToAdd);
	}

	/**
	 * Get Tag list size.
	 * @return Tag list size
	 */
	public int getTagListSize() {
		return this.TagList.size();
	}

	/**
	 * Status getter.
	 * @return Status of the current test
	 */
	public LogStatus getStatus(){
		return this.test.getRunStatus();
	}

	/**
	 * Report's name and report's directory name getter.
	 * @return Report's name and report's directory name
	 */
	public String getReportName(){
		return this.completeReportName;
	}

	/**
	 * Add information about the current step to report.
	 * @param information information about the current step
	 */
	public void addInformation(String information){
		this.informations.add(information);
	}

	public String takeScreenShot(boolean screenCapture, String stepName){
		if(screenCapture){
			final String screenshotPath = this.webDriverProvider.takeScreenshot(this.completeReportName,this.TestName, stepName);
			return screenshotPath;
		}
		return null;
	}

	// start code changed by Princi for WI - 76654
	public String getTestName() {
		return TestName;
	}

	public void setTestName(String testName) {
		TestName = testName;
	}
	// end code changed by Princi for WI - 76654
	

}
