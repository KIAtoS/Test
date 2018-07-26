package atos.mae.auto.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import atos.mae.auto.plugins.requirement.StepReturnEnum;
import atos.mae.auto.utils.enums.EnvironnementExecutionEnum;

public class TestStepModel {

	/**
	 * Excel file name.
	 */
	private String testName;

	/**
	 * TestStep description from excel file.
	 */
	private String testDescription;

	/**
	 * Excel File.
	 */
	private File file;


	private boolean run;

	/**
	 * global StepReturn for Excel reporting.
	 */
	private StepReturnEnum globalSepReturn;

	/**
	 * List of step.
	 */
	private List<DataColumnModel> dataColumnList = new ArrayList<DataColumnModel>();

	/**
	 * Environment execution (local, remote or browserStack).
	 */
	private EnvironnementExecutionEnum EnvExec = EnvironnementExecutionEnum.LOCAL;



	public boolean isModule() {
		return this instanceof ModuleModel;
	}

	public List<DataColumnModel> getDataColumnList(){
		return this.dataColumnList;
	}

	public void addDataColumn(DataColumnModel dcm) {
		this.dataColumnList.add(dcm);
	}


	public String getTestName() {
		return this.testName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	public String getTestDescription() {
		return this.testDescription;
	}

	public void setTestDescription(String stepDescription) {
		this.testDescription = stepDescription;
	}

	public StepReturnEnum getGlobalSepReturn() {
		return this.globalSepReturn;
	}

	public void setGlobalSepReturn(StepReturnEnum globalSepReturn) {
		this.globalSepReturn = globalSepReturn;
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public EnvironnementExecutionEnum getEnvExec() {
		return this.EnvExec;
	}

	public void setEnvExec(EnvironnementExecutionEnum envExec) {
		this.EnvExec = envExec;
	}

	public boolean haveToRun() {
		return this.run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}



}
