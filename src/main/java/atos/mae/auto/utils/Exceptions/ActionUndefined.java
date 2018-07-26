package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when action is undefined in excel file.
 */
public class ActionUndefined  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 7450173422655535987L;

	/**
	 * Exception constructor with error message.
	 */
	public ActionUndefined() {
	    super("Action not found");
	  }

}
