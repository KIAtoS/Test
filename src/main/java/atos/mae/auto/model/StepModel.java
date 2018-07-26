package atos.mae.auto.model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;

import atos.mae.auto.plugins.requirement.StepReturn;

public class StepModel {
	/**
	 * Step's name.
	 */
	private String name;

	/**
	 * Step description from excel file.
	 */
	private String stepDescription;
	 // code change for 30640
	
	/**
	 * Step action keyword from excel file.
	 */
	
	private String actionName;
	
	//end code change for 30640

	/**
	 * Path of excel directory (not excel file).
	 */
	private String path;



	/**
	 * Object found on webpage.
	 */
	//TODO : Study why this object is use with different type
	private Object webObject;

	/**
     * Default index if multiple object found
     */
    private int defaultIndex;

	/**
	 * Method to call.
	 */
	private Method methodCall;

	/**
	 * Module to call.
	 */
	private ModuleModel moduleCall;

	/**
	 * if test stop when step fail.
	 */
	private boolean isAbortOnFail;

	/**
	 * if step have to run or not.
	 */
	private boolean isOnError;

	/**
	 * Error found with validator.
	 */
	private String error;

	/**
	 * Data needed by method or module.
	 */
	private String data;

	/**
	 * State of this step.
	 */
	private StepReturn stepReturn = new StepReturn();

	/**
	 * Row where write result for reporting.
	 */
	private int row;

	/**
	 * Col where write result for reporting.
	 */
	private int col;

	/**
	 * Screen capture is needed.
	 */
	private boolean isScreenCaptureNeeded;
 // code change for 30640
	@NotNull(message = "Action Keyword is mandatory")
	public String getActionName() {
		return this.actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}
	
	// end  // code change for 30640
	
	@NotNull(message = "Step name is mandatory")
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NotNull(message = "WebObject not found in ObjectRepository")
	public Object getWebObject() {
		return this.webObject;
	}

	public void setWebObject(Object webObject) {
		this.webObject = webObject;
	}

	@NotNull(message = "Step action is mandatory")
	private Object getMethodOrModule(){
		if(this.getMethodCall() != null)
			return this.getMethodCall();
		else
			return this.getModuleCall();
	}

	public Method getMethodCall() {
		return this.methodCall;
	}

	public void setMethodCall(Method methodCall) {
		this.methodCall = methodCall;
	}

	public ModuleModel getModuleCall() {
		return this.moduleCall;
	}

	public void setModuleCall(ModuleModel moduleCall) {
		this.moduleCall = moduleCall;
	}

	@NotNull
	public String getStepDescription() {
		return this.stepDescription;
	}

	public void setStepDescription(String stepDescription) {
		this.stepDescription = stepDescription;
	}

	public boolean isAbortOnFail() {
		return this.isAbortOnFail;
	}

	/**
	 * Load boolean is abort on fail
	 * @param isAbortOnFail boolean is abort on fail
	 */
	public void setAbortOnFail(boolean isAbortOnFail) {
		this.isAbortOnFail = isAbortOnFail;
	}

	@AssertTrue
	private boolean methodOrModuleCanBeCalledCorrectly(){

		if(this.getModuleCall() != null){
			// if module need parameter
			if(this.getData() != null
			&& this.getModuleCall().getParameterCount() == 1)
				return true;

			// if module don't need parameter
			if(this.getData() == null
			&& this.getMethodCall().getParameterCount() == 0)
				return true;
		}else if(this.getMethodCall() != null){
			try {
				if (this.getData() == null || this.getData().isEmpty())
					this.getMethodCall().invoke(this.getWebObject());
				else
		    		this.getMethodCall().invoke(this.getWebObject(), "");
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
				return false;
			}catch ( RuntimeException e) {
				return true;
			}
			return true;
		}

		return false;
	}

	public String getData() {
		return this.data;
	}


	public void setData(String data) {
		this.data = data;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

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

	public boolean isOnError() {
		return isOnError;
	}

	public void setOnError(boolean isOnError) {
		this.isOnError = isOnError;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public StepReturn getStepReturn() {
		return stepReturn;
	}

	public void setStepReturn(StepReturn stepReturn) {
		this.stepReturn = stepReturn;
	}

	public boolean isScreenCaptureNeeded() {
		return isScreenCaptureNeeded;
	}

	public void setScreenCaptureNeeded(boolean isScreenCaptureNeeded) {
		this.isScreenCaptureNeeded = isScreenCaptureNeeded;
	}

    public int getDefaultIndex() {
        return defaultIndex;
    }

    public void setDefaultIndex(int defaultIndex) {
        this.defaultIndex = defaultIndex;
    }


}
