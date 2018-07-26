package atos.mae.auto.hpalm.model;

import java.util.LinkedList;

public class TestFieldEntity {

	public TestFieldEntity(){}

	public TestFieldEntity(String Name){
		this.setName(Name);
	}

	private String Name;
	private LinkedList<TestValueEntity> values = new LinkedList<TestValueEntity>();
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public LinkedList<TestValueEntity> getValues() {
		return values;
	}
	public void setValues(LinkedList<TestValueEntity> values) {
		this.values = values;
	}
}
