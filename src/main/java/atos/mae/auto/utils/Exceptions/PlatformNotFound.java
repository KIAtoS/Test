package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when platform was not found in environment execution json file.
 */
public class PlatformNotFound extends RuntimeException{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = -2205487627994740497L;

	/**
	 * Exception constructor with error message.
	 */
	public PlatformNotFound(){
	    super("Platform's name not found");
	  }
}
