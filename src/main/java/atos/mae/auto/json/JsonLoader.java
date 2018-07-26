package atos.mae.auto.json;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import atos.mae.auto.model.*;
import atos.mae.auto.utils.Liste;
import atos.mae.auto.utils.Exceptions.PlatformNotFound;

/**
 * Class used to convert json into object instance.
 */
@Component
public class JsonLoader {

	/**
	 * Logger.
	 */
	private static Logger Log = Logger.getLogger(JsonLoader.class);

	@Value("${objectRepository:Configuration/ObjectRepository.json}")
	private String objectRepository;

	@Value("${dataServerPath:Configuration/DataServer.json}")
	private String dataServerPath;

	@Autowired
	private Liste liste;

	private boolean testSetEnvironment = false;


	/**
	 * Load ObjectRepository.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public void LoadJson() throws UnsupportedEncodingException, FileNotFoundException{
		Log.info("Start : Loading ObjectRepository ");
		this.liste.setIdentifiers((WebElementModelList) LoadFile(this.objectRepository, WebElementModelList.class));
		Log.info("End : Loading ObjectRepository ");
	}


	/**
	 * Load environment configuration.
	 * @param Env Environment name to search
	 * @throws PlatformNotFound Trigger when environment name was not found
	 */
	public void LoadEnv(String Env,boolean testSetEnvironment) throws PlatformNotFound{
		if(this.testSetEnvironment)
			return;

		this.testSetEnvironment = testSetEnvironment;

    	String jsonLine = null;
		try {
			final byte[] encoded = Files.readAllBytes(Paths.get(this.dataServerPath));
			jsonLine = new String(encoded, Charset.forName("UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

    	JsonElement jelement = new JsonParser().parse(jsonLine);
    	JsonObject  jobject = jelement.getAsJsonObject();
    	final JsonArray jArray = jobject.getAsJsonArray("platforms");
    	for (final JsonElement jsonElement : jArray) {
    		jobject = jsonElement.getAsJsonObject();
    		jelement = jobject.get("Environment");
    		final String name = jelement.getAsString();
    		if(name.compareTo(Env) == 0){
    			for (final Entry<String,JsonElement> entry : jobject.entrySet()) {
    				this.liste.getStoredValues().put(entry.getKey(), entry.getValue().getAsString());
    			}
    			return;
    		}
		}
    	throw new PlatformNotFound();

	}

	private static <T> Object LoadFile(String path, Class<T> classOfElement) throws UnsupportedEncodingException, FileNotFoundException
	{
		BufferedReader br = null;
		br = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
		return new Gson().fromJson(br, classOfElement);
	}

	/**
	 * generate default ObjectRepository when --setup command line called.
	 * @return default ObjectRepository as json
	 */
	public static String defaultObjectRepository(){
		final WebElementModelList list = new WebElementModelList();
		list.defaultObjectRepository(true);
		final Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(list).replace("\n","\r\n");
	}
}
