package atos.mae.auto.plugins.requirement;


public class StepReturn {

	/**
	 * State of step.
	 */
	private StepReturnEnum stepReturnEnum;

	/**
	 * Information about step.
	 */
	private String information = "";

	/**
	 * Expected information.
	 */
	private String expected = "";

	/**
	 * Actual information.
	 */
	private String actual = "";

	/**
	 * Exception triggered.
	 */
	private Exception exception;

	/**
	 * Constructor.
	 */
	public StepReturn(){
	}

	/**
	 * Constructor.
	 * @param stepReturnEnum state of this step
	 */
	public StepReturn(StepReturnEnum stepReturnEnum){
		this.stepReturnEnum = stepReturnEnum;
	}

	/**
	 * Constructor.
	 * @param stepReturnEnum state about this step
	 * @param information information about this step
	 */
	public StepReturn(StepReturnEnum stepReturnEnum, String information){
		this.stepReturnEnum = stepReturnEnum;
		this.information = information;
	}

	/**
	 * Constructor.
	 * @param stepReturnEnum state about this step
	 * @param information information about this step
	 */
	public StepReturn(StepReturnEnum stepReturnEnum, String expected, String actual, String information){
		this.stepReturnEnum = stepReturnEnum;
		this.expected = expected;
		this.actual = actual;
		this.information = information;
	}

	/**
	 * Constructor.
	 * @param stepReturnEnum state about this step
	 * @param information information about this step
	 */
	public StepReturn(StepReturnEnum stepReturnEnum, String expected, String actual, String information,  Exception exception){
		this.stepReturnEnum = stepReturnEnum;
		this.expected = expected;
		this.actual = actual;
		this.information = information;
		this.exception = exception;
	}

	/**
	 * Constructor.
	 * @param stepReturnEnum state about this step
	 * @param information information about this step
	 * @param exception exception trigger in this step
	 */
	public StepReturn(StepReturnEnum stepReturnEnum, String information, Exception exception){
		this.stepReturnEnum = stepReturnEnum;
		this.information = information;
		this.exception = exception;
	}

	/**
	 * Constructor.
	 * @param stepReturnEnum state about this step
	 * @param exception exception trigger in this step
	 */
	public StepReturn(StepReturnEnum stepReturnEnum, Exception exception){
		this.stepReturnEnum = stepReturnEnum;
		this.exception = exception;
	}

	/**
	 * StepReturnEnum getter.
	 * @return state about this step
	 */
	final public StepReturnEnum getStepReturn() {
		return this.stepReturnEnum;
	}

	/**
	 * StepReturnEnum setter.
	 * @param stepReturnEnum state about this step
	 */
	final public void setStepReturn(StepReturnEnum stepReturnEnum) {
		this.stepReturnEnum = stepReturnEnum;
	}

	/**
	 * Information getter.
	 * @return Information
	 */
	final public String getInformation() {
		return this.information;
	}

	/**
	 * Information setter.
	 * @param information Information about this step
	 */
	final public void setInformation(String information) {
		this.information = information;
	}

	/**
	 * Exception getter.
	 * @return Exception trigger in this step
	 */
	final public Exception getException() {
		return this.exception;
	}

	/**
	 * Exception setter.
	 * @param exception Exception trigger in this step
	 */
	final public void setException(Exception exception) {
		this.exception = exception;
	}

	final public boolean isFail(){

		switch(this.getStepReturn()){
			case DONOTLOG:
			case PASS:
			case WARN:
				return false;
			case ERROR:
			case FAIL:
			default:
				return true;
		}

	}

	public String getExpected() {
		return expected;
	}

	public void setExpected(String expected) {
		this.expected = expected;
	}

	public String getActual() {
		return actual;
	}

	public void setActual(String actual) {
		this.actual = actual;
	}
}
