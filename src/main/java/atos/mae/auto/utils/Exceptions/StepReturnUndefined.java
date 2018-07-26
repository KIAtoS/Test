package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when a stepReturn is missing.
 */
public class StepReturnUndefined  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = -7354669668725665090L;

	/**
	 * Exception constructor with error message.
	 */
	public StepReturnUndefined() {
	    super("Step state not found. Please contact support");
	  }

}
