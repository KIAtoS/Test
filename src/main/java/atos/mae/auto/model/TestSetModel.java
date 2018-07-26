package atos.mae.auto.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.remote.DesiredCapabilities;

public class TestSetModel {

	private String name;
	
	
	//changed by Princi for 57182	
	/**
	 * Row where write result for reporting for test set.
	 */
	private int row;

	/**
	 * Col where write result for reporting for test set.
	 */
	private int col;
	
	private File testStepfile;
	
	private int runTest;
	
	private int StartCol;	
	//end of changed by Princi for 57182
	
	/**
	 * List of step.
	 */
	private List<TestStepModel> testStepModelList = new ArrayList<TestStepModel>();

	private List<DesiredCapabilities> desiredCapabilitiesList = new ArrayList<DesiredCapabilities>();

	public List<TestStepModel> getTestStepModelList() {
		return this.testStepModelList;
	}

	public void addTestStepModel(TestStepModel testStepModelList) {
		this.testStepModelList.add(testStepModelList);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<DesiredCapabilities> getDesiredCapabilitiesList() {
		return this.desiredCapabilitiesList;
	}

	public void addCapability(DesiredCapabilities desiredCapabilities) {
		this.desiredCapabilitiesList.add(desiredCapabilities);
	}
	
	
	// changed by Princi for 57182
	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}	
	
	public File getTestStepfile() {
		return testStepfile;
	}

	public void setTestStepfile(File testStepfile) {
		this.testStepfile = testStepfile;
	}

	public int getRunTest() {
		return runTest;
	}

	public void setRunTest(int runTest) {
		this.runTest = runTest;
	}

	public int getStartCol() {
		return StartCol;
	}

	public void setStartCol(int startCol) {
		StartCol = startCol;
	}	
	//end of changed by Princi for 57182
	
}
