package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when an action is call before a webDriver was define.
 */
public class NoDriverDefineException  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 5821409181834940568L;

	/**
	 * Exception constructor with error message.
	 */
	public NoDriverDefineException() {
	    super("No defined driver");
	  }

}
