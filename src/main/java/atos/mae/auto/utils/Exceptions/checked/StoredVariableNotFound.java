package atos.mae.auto.utils.Exceptions.checked;

/**
 * Exception trigger when stored variable used but not found.
 */
public class StoredVariableNotFound extends Exception{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 8215461165983188213L;

	/**
	 * Exception constructor with error message.
	 */
	public StoredVariableNotFound(String var){
	    super("Stored variable '" + var + "' not found.");
	  }
}
