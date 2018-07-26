package atos.mae.auto.model;

import java.util.List;

public class ModuleModel extends TestStepModel{


	/**
	 * Parameters send to module.
	 */
	private List<String> ParamModuleValues;

	/**
	 * Current index.
	 */
	private int index = 0;


	public int getParameterCount() {
		return this.ParamModuleValues.size();
	}

	public String getNextParam() {
		final String param = this.ParamModuleValues.get(this.index);
		this.index++;
		return param;
	}

	public void setParamModuleValues(List<String> paramModuleValues) {
		this.ParamModuleValues = paramModuleValues;
	}




}
