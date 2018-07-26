package atos.mae.auto.plugins.requirement;

public enum StepReturnEnum {
	PASS, // OK
	FAIL, // KO
	WARN, // OK, but warning
	ERROR, // KO before test (example : unable to locate element, method not found, browser not defined, ...)
	DONOTLOG, // no need to log (is module for example, already log)
	METHODNOTFOUND, // method not found for this kind of identifier (example : click on textBox)
}
