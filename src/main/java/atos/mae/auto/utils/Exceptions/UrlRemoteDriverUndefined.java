package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when remote webDriver url in excel file is undefined.
 */
public class UrlRemoteDriverUndefined  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = -5336194253175300368L;

	/**
	 * Exception constructor with error message.
	 */
	public UrlRemoteDriverUndefined() {
	    super("Url remote driver not defined");
	  }

}
