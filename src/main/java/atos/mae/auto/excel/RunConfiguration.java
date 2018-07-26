package atos.mae.auto.excel;

public class RunConfiguration {

	private int rowStart;
	private int runRow;
	private int nameCol;
	private int descriptionCol;
	private int abortOnFailCol;
	private int identifierCol;
	private int identifierIndexCol;
	private int action_ModuleCol;
	private int dataStartCol;

	public int getRowStart() {
		return rowStart;
	}
	public void setRowStart(int rowStart) {
		this.rowStart = rowStart;
	}
	public int getRunRow() {
		return runRow;
	}
	public void setRunRow(int runRow) {
		this.runRow = runRow;
	}
	public int getNameCol() {
		return nameCol;
	}
	public void setNameCol(int nameCol) {
		this.nameCol = nameCol;
	}
	public int getDescriptionCol() {
		return descriptionCol;
	}
	public void setDescriptionCol(int descriptionCol) {
		this.descriptionCol = descriptionCol;
	}
	public int getAbortOnFailCol() {
		return abortOnFailCol;
	}
	public void setAbortOnFailCol(int abortOnFailCol) {
		this.abortOnFailCol = abortOnFailCol;
	}
	public int getIdentifierCol() {
		return identifierCol;
	}
	public void setIdentifierCol(int identifierCol) {
		this.identifierCol = identifierCol;
	}
	public int getIdentifierIndexCol() {
		return identifierIndexCol;
	}
	public void setIdentifierIndexCol(int identifierIndexCol) {
		this.identifierIndexCol = identifierIndexCol;
	}
	public int getAction_ModuleCol() {
		return action_ModuleCol;
	}
	public void setAction_ModuleCol(int action_ModuleCol) {
		this.action_ModuleCol = action_ModuleCol;
	}
	public int getDataStartCol() {
		return dataStartCol;
	}
	public void setDataStartCol(int dataStartCol) {
		this.dataStartCol = dataStartCol;
	}
	
	public Object clone(){
		RunConfiguration result = new RunConfiguration();
		result.setRowStart(this.rowStart);
		result.setRunRow(this.runRow);
		result.setNameCol(this.nameCol);
		result.setDescriptionCol(this.descriptionCol);
		result.setAbortOnFailCol(this.abortOnFailCol);
		result.setIdentifierCol(this.identifierCol);
		result.setIdentifierIndexCol(this.identifierIndexCol);
		result.setAction_ModuleCol(this.action_ModuleCol);
		result.setDataStartCol(this.dataStartCol);
		return result;
	}
	
}
