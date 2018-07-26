package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when identifier's type was not found in IdentifierType enum.
 */
public class TypeNotFoundException  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 7512410591850887479L;

	/**
	 * Exception constructor with error message.
	 */
	public TypeNotFoundException() {
	    super("Type of identifier not found in IdentifierType list. Please contact support");
	  }

}
