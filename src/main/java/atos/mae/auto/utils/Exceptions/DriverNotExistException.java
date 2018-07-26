package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when browser needed is not available.
 * See WebDriverProvider class for available browser.
 */
public class DriverNotExistException  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 8748334067745062290L;

	/**
	 * Exception constructor with error message.
	 */
	public DriverNotExistException() {
	    super("Driver wanted is not available");
	  }

}
