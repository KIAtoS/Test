package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when step name not found.
 */
public class StepNameNotFound extends RuntimeException{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 6028320976803796965L;


	/**
	 * Exception constructor with error message.
	 */
	public StepNameNotFound(int rowCount){
	    super("Step name not found at line " + rowCount);
	  }
}
