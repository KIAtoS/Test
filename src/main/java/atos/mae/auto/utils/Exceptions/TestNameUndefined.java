package atos.mae.auto.utils.Exceptions;

/**
 * Exception trigger when test's name in TestSet excel file is undefined.
 */
public class TestNameUndefined  extends RuntimeException
{
	/**
	 * serialVersionUID auto-generated.
	 */
	private static final long serialVersionUID = -2794642184966651787L;

	/**
	 * Exception constructor with error message.
	 */
	public TestNameUndefined() {
	    super("Test name not found");
	  }

}
