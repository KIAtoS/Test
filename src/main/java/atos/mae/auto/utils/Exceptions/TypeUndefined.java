package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when identifier's type is undefined in ObjectRepository.
 */
public class TypeUndefined  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 8550167843024806060L;

	/**
	 * Exception constructor with error message.
	 */
	public TypeUndefined() {
	    super("Type of Identifier undefined");
	  }

}
