package atos.mae.auto.utils;

import java.util.Iterator;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Component;

import atos.mae.auto.utils.Exceptions.RangeNameNotFoundException;

/**
 * Class used to work with excel range.
 */
@Component
public class ExcelUtils {

	/**
	 * return column id from range name.
	 * @param wb workbook
	 * @param RangeName Range's name
	 * @return Column id
	 * @throws NullPointerException Trigger when range not found or have no cells
	 * @throws RangeNameNotFoundException
	 */
	public int getColByName(Workbook wb, String RangeName) throws NullPointerException, RangeNameNotFoundException{
		final CellReference CellRef = this.getCellRef(wb, RangeName);
	    return CellRef.getCol();
	}

	/**
	 * return row id from range name.
	 * @param wb workbook
	 * @param RangeName Range's name
	 * @return Row id
	 * @throws NullPointerException Trigger when range not found or have no cells
	 * @throws RangeNameNotFoundException
	 */
	public int getRowByName(Workbook wb, String RangeName) throws NullPointerException, RangeNameNotFoundException{
		final CellReference CellRef = this.getCellRef(wb, RangeName);
	    return CellRef.getRow();
	}

	/**
	 * return the first cell from range name.
	 * @param wb workbook
	 * @param Sheet sheet to work
	 * @param RangeName Range's name
	 * @return cell
	 * @throws NullPointerException Trigger when range not found or have no cells
	 * @throws RangeNameNotFoundException
	 */
	public Cell getCellByName(Workbook wb,String RangeName) throws NullPointerException, RangeNameNotFoundException{
		final CellReference CellRef = this.getCellRef(wb, RangeName);
		Sheet s = wb.getSheet(CellRef.getSheetName());
		//final Row row = Sheet.getRow(CellRef.getRow());
		final Row row = s.getRow(CellRef.getRow());
	    return row.getCell(CellRef.getCol());
	}

	/**
	 * return the last cell from range name.
	 * @param wb workbook
	 * @param RangeName Range's name
	 * @return Cellreference
	 * @throws NullPointerException Trigger when range not found or have no cells
	 * @throws RangeNameNotFoundException
	 */
	public CellReference getLastCellFromArea(Workbook wb, String RangeName) throws NullPointerException, RangeNameNotFoundException{
		final AreaReference aref = this.getArea(wb, RangeName);
		final CellReference CellRef = aref.getLastCell();
	    return CellRef;
	}

	/**
	 * return column id from range name.
	 * @param wb workbook
	 * @param RangeName Range's name
	 * @return Column id
	 * @throws NullPointerException Trigger when range not found or have no cells
	 * @throws RangeNameNotFoundException
	 */
	private CellReference getCellRef(Workbook wb, String RangeName) throws NullPointerException, RangeNameNotFoundException{
		final AreaReference aref = this.getArea(wb, RangeName);
		final CellReference CellRef = aref.getFirstCell();
	    return CellRef;
	}

	/**
	 * return column id from range name.
	 * @param wb workbook
	 * @param RangeName Range's name
	 * @return Column id
	 * @throws RangeNameNotFoundException
	 * @throws NullPointerException Trigger when range not found or have no cells
	 */
	private AreaReference getArea(Workbook wb, String RangeName) throws RangeNameNotFoundException {
	    final Name aNamedCell = wb.getName(RangeName);
	    if(aNamedCell == null)
	    	throw new RangeNameNotFoundException("Range " + RangeName + " not found");
	    final AreaReference aref = new AreaReference(aNamedCell.getRefersToFormula(), SpreadsheetVersion.EXCEL2007);
	    return aref;
	}

	/**
	 * return column id from range name.
	 * @param wb workbook
	 * @param RangeName Range's name
	 * @return Column id
	 * @throws NullPointerException Trigger when range not found or have no cells
	 */
	public void clearRange(XSSFSheet Sheet, int ColStart) throws NullPointerException{
		final Iterator<Row> rows = Sheet.rowIterator();
		Row row = rows.next();
    	while (rows.hasNext()){
    		row = rows.next();
    		final Cell cell = row.getCell(ColStart);
    		final double[] ExpectedValues = {1,2,3};
    		if (cell != null && cell.getCellType() == Cell.CELL_TYPE_NUMERIC && ArrayUtils.contains(ExpectedValues,cell.getNumericCellValue())){
		    	cell.setCellType(Cell.CELL_TYPE_STRING);
		    	cell.setCellValue("");
    		}
    	}
	}


	public Cell getCellByRowCol(XSSFSheet sheet, int rowNum, int colNum){
		final Row row = sheet.getRow(rowNum);
		return row.getCell(colNum);
	}

}
