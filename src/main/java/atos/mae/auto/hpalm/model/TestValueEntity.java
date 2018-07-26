package atos.mae.auto.hpalm.model;

public class TestValueEntity {

	public TestValueEntity(){}

	public TestValueEntity(String value){
		this.setValue(value);
	}

	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
