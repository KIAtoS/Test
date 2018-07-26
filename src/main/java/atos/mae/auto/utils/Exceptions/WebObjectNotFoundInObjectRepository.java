package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when action is undefined in excel file.
 */
public class WebObjectNotFoundInObjectRepository  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */


	/**
	 * Exception constructor with error message.
	 */
	public WebObjectNotFoundInObjectRepository(String webObjectName) {
	    super("Identifier '" + webObjectName + "' not found in ObjectRepository. Please verify consistency between Test and ObjectRepository");
	  }

}
