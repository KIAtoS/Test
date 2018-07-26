package atos.mae.auto.utils;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import atos.mae.auto.plugins.requirement.StepReturn;
import atos.mae.auto.plugins.requirement.StepReturnEnum;
import atos.mae.auto.plugins.requirement.WebDriverProvider;
import atos.mae.auto.utils.Exceptions.StepReturnUndefined;
import atos.mae.auto.utils.Exceptions.TestNameUndefined;
import atos.mae.auto.utils.enums.TagReport;

@ContextConfiguration(locations = { "file:src/main/resources/config.xml" })
public class ReportTest extends AbstractJUnit4SpringContextTests {

	@Value("${reportPath:Report}")
	private static String reportPath;

	@Autowired
	private Report report;

	@Autowired
	private WebDriverProvider webDriverProvider;

	private static int PNGFileListLength;

	@Test
	public void completeReportTest() {
		this.report.startTest("TUName", "TUDesc");
		this.report.addTag(TagReport.MOZILLA);
		this.report.setStep(new StepReturn(StepReturnEnum.PASS), "TUStepName",
				"TUAction", true, null);
		this.report.endtest();
	}

	@Test
	public void LogStatusTest_DONOTLOGAndAbortOnFail() {
		this.report.startTest("TUName", "TUDesc");

		this.report.setStep(new StepReturn(StepReturnEnum.DONOTLOG),
				"TUStepName", "TUAction", true, null);
		Assert.assertEquals(com.relevantcodes.extentreports.LogStatus.UNKNOWN,
				this.report.getStatus());

		this.report.endtest();
	}

	@Test
	public void LogStatusTest_PASSAndAbortOnFail() {
		this.report.startTest("TUName", "TUDesc");

		this.report.setStep(new StepReturn(StepReturnEnum.PASS), "TUStepName",
				"TUAction", true, null);
		Assert.assertEquals(com.relevantcodes.extentreports.LogStatus.PASS,
				this.report.getStatus());

		this.report.endtest();
	}

	@Test
	public void LogStatusTest_PASSAfterDONOTLOGAndAbortOnFail() {
		this.report.startTest("TUName", "TUDesc");
		this.report.setStep(new StepReturn(StepReturnEnum.DONOTLOG),
				"TUStepName", "TUAction", true, null);
		this.report.setStep(new StepReturn(StepReturnEnum.PASS), "TUStepName",
				"TUAction", true, null);
		Assert.assertEquals(com.relevantcodes.extentreports.LogStatus.PASS,
				this.report.getStatus());

		this.report.endtest();
	}

	@Test
	public void LogStatusTest_WARNAndAbortOnFail() {
		this.report.startTest("TUName", "TUDesc");
		this.report.setStep(new StepReturn(StepReturnEnum.WARN), "TUStepName",
				"TUAction", true, null);
		Assert.assertEquals(com.relevantcodes.extentreports.LogStatus.WARNING,
				this.report.getStatus());

		this.report.endtest();
	}

	@Test
	public void LogStatusTest_PASSAfterWARNAndAbortOnFail() {
		this.report.startTest("TUName", "TUDesc");
		this.report.setStep(new StepReturn(StepReturnEnum.WARN), "TUStepName",
				"TUAction", true, null);
		this.report.setStep(new StepReturn(StepReturnEnum.PASS), "TUStepName",
				"TUAction", true, null);
		Assert.assertEquals(com.relevantcodes.extentreports.LogStatus.WARNING,
				this.report.getStatus());

		this.report.endtest();
	}

	@Test
	public void LogStatusTest_FAILAndNotAbortOnFail() {
		this.report.startTest("TUName", "TUDesc");
		this.report.setStep(new StepReturn(StepReturnEnum.FAIL), "TUStepName",
				"TUAction", false, null);
		Assert.assertEquals(com.relevantcodes.extentreports.LogStatus.WARNING,
				this.report.getStatus());

		this.report.endtest();
	}

	@Test
	public void LogStatusTest_PASSAfterFailAndNotAbortOnFail() {
		this.report.startTest("TUName", "TUDesc");

		this.report.setStep(new StepReturn(StepReturnEnum.FAIL), "TUStepName",
				"TUAction", false, null);
		this.report.setStep(new StepReturn(StepReturnEnum.PASS), "TUStepName",
				"TUAction", false, null);
		Assert.assertEquals(com.relevantcodes.extentreports.LogStatus.WARNING,
				this.report.getStatus());

		this.report.endtest();
	}

	@Test
	public void LogStatusTest_FAILAndAbortOnFail() {
		this.report.startTest("TUName", "TUDesc");
		this.report.setStep(new StepReturn(StepReturnEnum.FAIL), "TUStepName",
				"TUAction", true, null);
		Assert.assertEquals(com.relevantcodes.extentreports.LogStatus.FAIL,
				this.report.getStatus());

		this.report.endtest();
	}

	@Test
	public void LogStatusTest_PASSAfterFAILAndAbortOnFail() {
		this.report.startTest("TUName", "TUDesc");
		this.report.setStep(new StepReturn(StepReturnEnum.FAIL), "TUStepName",
				"TUAction", true, null);
		this.report.setStep(new StepReturn(StepReturnEnum.PASS), "TUStepName",
				"TUAction", true, null);
		Assert.assertEquals(com.relevantcodes.extentreports.LogStatus.FAIL,
				this.report.getStatus());

		this.report.endtest();
	}

	@Test
	public void tagTest() {
		this.report.startTest("TUName", "TUDesc");
		int BaseTagSize = this.report.getTagListSize();
		Assert.assertEquals(0, BaseTagSize);
		this.report.addTag(TagReport.MOZILLA);
		Assert.assertEquals(BaseTagSize + 1, this.report.getTagListSize());
		this.report.addTag(TagReport.CHROME);
		Assert.assertEquals(BaseTagSize + 2, this.report.getTagListSize());
		this.report.addTag(TagReport.MOZILLA);
		Assert.assertEquals(BaseTagSize + 2, this.report.getTagListSize());
		// code change for 30843
		this.report.addTag(TagReport.HTMLUNIT);
		Assert.assertEquals(BaseTagSize + 2, this.report.getTagListSize());
		// end code change for 30843
		this.report.endtest();
		

		this.report.startTest("TUName", "TUDesc");
		BaseTagSize = this.report.getTagListSize();
		Assert.assertEquals(0, BaseTagSize);
		this.report.endtest();
	}

	/*
	 *
	 * KO
	 */

	@Test(expected = TestNameUndefined.class)
	public void skipWithoutName() {
		this.report.skipTest(null, null);
	}

	@Test(expected = TestNameUndefined.class)
	public void startTestWithoutName() {
		this.report.startTest(null, null);
	}

	@Test(expected = StepReturnUndefined.class)
	public void setStepWithoutStepReturn() {
		this.report.startTest("TUName", "");
		this.report.setStep(null, "", "", true, null);
	}

}
