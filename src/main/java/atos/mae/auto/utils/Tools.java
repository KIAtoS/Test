package atos.mae.auto.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import atos.mae.auto.action.Action;
import atos.mae.auto.plugins.PluginsLoader;
import atos.mae.auto.plugins.requirement.Parameters;
import atos.mae.auto.plugins.requirement.StepReturn;

/**
 * Tools class.
 */
@Component
public class Tools {

	/**
	 * Logger.
	 */
	final static Logger Log = Logger.getLogger(Tools.class);

	@Value("${pluginPath:Plugin}")
	private String pluginPath;

	@Value("${modulePath:Modules}")
	private String modulePath;

	@Value("${testStepSheetName:TestSteps}")
	private String testStepSheetName;

	@Value("${moduleRangeParameters:ModParameters}")
	private String moduleRangeParameters;

	@Autowired
	private Liste liste;

	@Autowired
	private Action action;

	@Autowired
	private ExcelUtils excelUtils;

	/**
	 * Method called by --exportactions command line.
	 * Write all public Action class method with parameters in file.
	 * @param writer Enable to write in the same file than other method exported
	 */
	public void ExportAction(PrintWriter writer){
		final Method[] methods =  this.action.getClass().getMethods();
		for (final Method method : methods) {
			// compilation de la regex
			final Pattern p = Pattern.compile("atos\\.mae\\.auto\\.action\\.Action\\.([\\w_]+)\\(");
			// cr�ation d'un moteur de recherche
			final Matcher m = p.matcher(method.toString());
			// lancement de la recherche de toutes les occurrences
			while (m.find()){
		        final String methodToWrite = m.group(1).trim();
		        String parameters = "";
		        if(method.isAnnotationPresent(Parameters.class))
		        	parameters = ((Parameters)method.getAnnotation(Parameters.class)).value();
		        writer.println(methodToWrite + ":" + parameters);
			}
		}
	}

	/**
	 * Method called by --exportactions command line.
	 * Write all public plugin method with parameters in file.
	 * @param writer Enable to write in the same file than other method exported
	 */
	public void ExportPluginAction(PrintWriter writer){

		PluginsLoader.getInstance().setFilesPath(this.pluginPath);
		PluginsLoader.getInstance().loadAllActionPlugins();


		for (final Object ap : PluginsLoader.getInstance().getActionPlugins()) {
			final Method[] methods = ap.getClass().getMethods();

			for (final Method method : methods) {
				if (method.getReturnType().equals(StepReturn.class)) {
					String parameters = "";
			        if(method.isAnnotationPresent(Parameters.class))
			        	parameters = ((Parameters)method.getAnnotation(Parameters.class)).value();
			        writer.println(method.getName() + ":" + parameters);
				}
			}
		}
	}

	/**
	 * Method called by --exportactions command line.
	 * Write all module with parameters in file.
	 * @param writer Enable to write in the same file than other method exported
	 */
	public void ExportModule(PrintWriter writer){
		this.liste.setModulesAvailable();
		for (final String ModuleName : this.liste.getModulesAvailable()) {
			if(ModuleName.compareTo("Mod_Template") == 0)
				continue;

			FileInputStream TestStepsFIS = null;
			Workbook wb = null;
			try {
				TestStepsFIS = new FileInputStream(Paths.get(this.modulePath,ModuleName,ModuleName + ".xlsm").toString());
				wb = new XSSFWorkbook(TestStepsFIS);
			} catch (FileNotFoundException e) {
				Log.error("Module '" + ModuleName + "' not found");
				return;
			} catch (IOException e) {
				Log.error(e.getMessage());
				return;
			}

			//final XSSFSheet Sheet = (XSSFSheet) wb.getSheet(this.testStepSheetName);
			final Cell cell = this.excelUtils.getCellByName(wb,this.moduleRangeParameters);
			if(cell != null){
				final String params = cell.getStringCellValue();
				if(params.trim().isEmpty())
					continue;
				writer.println(ModuleName + ":" + params);
			}else{
				continue;
			}
		}

	}

	/**
	 * Enable to write xml with a pretty format.
	 * @param input Xml as string
	 * @return xml as pretty string or null if error was triggered
	 */
	public String XmlprettyFormat(String input) {
		try {
	    	final Source xmlInput = new StreamSource(new StringReader(input));
	    	final StringWriter stringWriter = new StringWriter();
	    	final StreamResult xmlOutput = new StreamResult(stringWriter);
	    	final TransformerFactory transformerFactory = TransformerFactory.newInstance();
	    	Transformer transformer;
			transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
	        transformer.transform(xmlInput, xmlOutput);
	        return xmlOutput.getWriter().toString();
		} catch (TransformerException e) {
			Log.error("Error while transforming xml to pretty xml", e);
			return null;
		}

	}


}
