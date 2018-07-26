package atos.mae.auto.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import atos.mae.auto.factory.WebElementFactory;
import atos.mae.auto.model.WebElementModel;
import atos.mae.auto.model.WebElementModelList;
import atos.mae.auto.utils.Exceptions.TypeUndefined;
import atos.mae.auto.utils.Exceptions.checked.StoredVariableNotFound;

/**
 * Class use to store differents list.
 */
@Component
public class Liste {

	/**
	 * Logger.
	 */
	final static Logger Log = Logger.getLogger(Liste.class);

	@Value("${resourcePath:.}")
	private String resourcePath;

	@Value("${modulePath:Modules}")
	private String modulePath;

	@Autowired
	private Report report;

	@Autowired
	private WebElementFactory webElementFactory;

	/**
	 * Identifiers found in ObjectRepository.
	 */
	private HashMap<String, Object> Identifiers = new HashMap<>();

	/**
	 * Modules available found in module's directory.
	 */
	private ArrayList<String> ModulesAvailable = new ArrayList<String>();

	/**
	 * Values from environment execution or stored during test.
	 */
	private LinkedHashMap<String,String> StoredValues = new LinkedHashMap<String, String>();


	/**
	 * Identifier list getter.
	 * @return List of identifier
	 */
	public HashMap<String, Object> getIdentifiers()
	{
		return this.Identifiers;

	}

	/**
	 * Build the list of identifier by convert model to identifier by type (Button, Link, ...)
	 *
	 * @param Base the list of model
	 */
	public void setIdentifiers(WebElementModelList Base)
	{
		for (final WebElementModelList listSection : Base.Base){
			for (final WebElementModel identifierModel : listSection.List) {
				Object Identifier = null;
				try{
					Identifier = this.webElementFactory.MakeIdentifier(identifierModel);
				} catch(NullPointerException e){
					throw new TypeUndefined();
				}
				this.Identifiers.put(identifierModel.getObjectName().trim(), Identifier);
			}
		}
	}

	/**
	 * Module list getter.
	 * @return Module list
	 */
	public ArrayList<String> getModulesAvailable() {
		return this.ModulesAvailable;
	}

	/**
	 * Build the list of module available by searching directory in module's path.
	 */
	public void setModulesAvailable() {
		final File folder = new File(this.modulePath);

		final File[] listOfFiles = folder.listFiles();

		if(listOfFiles != null){
		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isDirectory()) {
		    	  this.ModulesAvailable.add(listOfFiles[i].getName());
		      }
		    }
		}
	}

	/**
	 * Stored variable list getter.
	 * @return Stored variable list
	 */
	public LinkedHashMap<String,String> getStoredValues() {
		return this.StoredValues;
	}

	/**
	 * Check and replace stored variable with pattern.
	 * @param Data String that need to replace part corresponding to pattern
	 * @return String with part replace if pattern was found
	 * @throws StoredVariableNotFound if stored variable found by pattern was not found in stored variable list
	 */
	public String CheckStoredVariable(String Data) throws StoredVariableNotFound{
		// Check and use stored variable
	    Log.info("Check if stored variable is needed");
	    if(Data == null)
	    	return null;

		// compilation de la regex
		final Pattern p = Pattern.compile("(\\$\\{[\\w_]+\\})");
		// creation d un moteur de recherche
		final Matcher m = p.matcher(Data);
		// lancement de la recherche de toutes les occurrences
		while (m.find()){
	        // affichage de la sous-chaine captur�e
	        final String var = m.group().replaceAll("(\\$\\{|\\})", "");
	        if(this.getStoredValues().containsKey(var)){
	        	final String value = this.getStoredValues().get(var);
		        Data = Data.replace(m.group(), value);
		        Log.info("Stored variable used : '" + var + "' and the value is '" + value + "'");

		        if(value.trim().isEmpty()){
		        	this.report.addInformation("variable '" + var + "' is empty");
		        }
	        }else{
	        	throw new StoredVariableNotFound(var);
	        }
		}
		return Data;
	}

}
