package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when step's name in excel file is undefined.
 */
public class StepNameUndefined  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = 1042286199603433156L;

	/**
	 * Exception constructor with error message.
	 */
	public StepNameUndefined() {
	    super("Step name not found");
	  }

}
