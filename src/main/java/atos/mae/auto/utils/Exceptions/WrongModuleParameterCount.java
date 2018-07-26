package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when the called module has wrong number of parameter.
 */
public class WrongModuleParameterCount extends RuntimeException{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 6955979040855412399L;

	/**
	 * Exception constructor with error message.
	 */
	public WrongModuleParameterCount(){
	    super("Wrong number of module parameter. please refer to module.");
	  }
}
