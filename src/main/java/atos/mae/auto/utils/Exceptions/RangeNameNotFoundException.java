package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when step name not found.
 */
public class RangeNameNotFoundException extends RuntimeException{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = -1344208936855111863L;


	/**
	 * Exception constructor with error message.
	 */
	public RangeNameNotFoundException(String message){
	    super(message);
	  }
}
