package atos.mae.auto.model;

import java.util.ArrayList;
import java.util.List;

import atos.mae.auto.plugins.requirement.StepReturnEnum;

public class DataColumnModel {

	/**
	 * Name of column data.
	 */
	private String dataColName;

	/**
	 * Index of column data.
	 */
	private int dataColIndex;

	/**
	 * List of step.
	 */
	private List<StepModel> stepList;

	private StepReturnEnum status;

	private ArrayList<String> checkErrors;

	/**
	 * if step have to run or not
	 */
	private boolean isOnError;

	public DataColumnModel(){
		this.stepList = new ArrayList<StepModel>();
		this.setCheckErrors(new ArrayList<String>());
		this.setStatus(StepReturnEnum.PASS);
	}

	public String getDataColName() {
		return this.dataColName;
	}

	public void setDataColName(String dataColName) {
		this.dataColName = dataColName;
	}

	public List<StepModel> getStepList() {
		return this.stepList;
	}

	public void addStep(StepModel sm){
		this.stepList.add(sm);
	}

	public StepReturnEnum getStatus() {
		return this.status;
	}

	public void setStatus(StepReturnEnum checkStatus) {
		this.status = checkStatus;
	}

	public ArrayList<String> getCheckErrors() {
		return this.checkErrors;
	}

	public void setCheckErrors(ArrayList<String> checkErrors) {
		this.checkErrors = checkErrors;
	}

	public int getDataColIndex() {
		return dataColIndex;
	}

	public void setDataColIndex(int dataColIndex) {
		this.dataColIndex = dataColIndex;
	}


}
