package atos.mae.auto.utils.Exceptions;

import java.util.ArrayList;

/**
 * Exception trigger when step have error while loading.
 */
public class StepException  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 7710400463425862790L;


	/**
	 * List of exception triggered.
	 */
	final private ArrayList<Exception> exceptionList;


	/**
	 * Exception constructor with error message.
	 */
	public StepException(ArrayList<Exception> exceptionList) {
	    super("");
	    this.exceptionList = exceptionList;
	  }


	public ArrayList<Exception> getExceptionList() {
		return this.exceptionList;
	}

}
