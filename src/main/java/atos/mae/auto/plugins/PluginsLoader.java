package atos.mae.auto.plugins;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import atos.mae.auto.plugins.requirement.IAction;

@Component
public class PluginsLoader {
	/**
	 * Logger.
	 */
	private final static Logger Log = Logger.getLogger(PluginsLoader.class);

	@Value("${pluginPath:Plugin}")
	private String pluginPath;

	/**
	 * Plugin jar list.
	 */
	private String[] files;

	/**
	 * IAction detected plugin waiting for instanciate.
	 */
	private ArrayList<Class<?>> IActionPluginsWaitingForInstanciate;

	/**
	 * IAction plugin instanciate list.
	 */
	private IAction[] IActionPlugins;


	public PluginsLoader(){
		this.IActionPluginsWaitingForInstanciate = new ArrayList<Class<?>>();
	}

	/**
	 * Singleton's private constructor.
	 *
	private PluginsLoader(){
		this.IActionPluginsWaitingForInstanciate = new ArrayList<Class<?>>();
	}*/

	/**
	 * Singleton's instance.
	 */
	private static PluginsLoader INSTANCE;

	/**
	 * Singleton's instance return.
	 * @return instance of PluginsLoader
	 */
	public static PluginsLoader getInstance()
	{
		if (INSTANCE == null) {
			INSTANCE = new PluginsLoader();
		}
		return INSTANCE;
	}

	/**
	 * IAction Plugin instanciate list getter.
	 * @return IAction Plugin instanciate list
	 */
	public IAction[] getActionPlugins() {
		return this.IActionPlugins;
	}

	/**
	 * Define the plugins's file list.
	 * @param path path of plugin
	 */
	public void setFilesPath(String path){
		final File file = new File(path);
		if (!file.exists() || !file.isDirectory()){
			Log.info("No available plugin directory found");
			return;
		}

		final FilenameFilter jarFilter = new FilenameFilter() {
			/**
			 * File name filter.
			 */
			public boolean accept(File dir, String name) {
				String lowercaseName = name.toLowerCase(Locale.getDefault());
				if (lowercaseName.endsWith(".jar")) {
					return true;
				} else {
					return false;
				}
			}
		};

		this.files = file.list(jarFilter);
	}

	/**
	 * Fonction de chargement de tout les plugins de type IActionPlugins.
	 * @return Une collection de IActionPlugins contenant les instances des plugins
	 */
	public void loadAllActionPlugins()  {

		this.initializeLoader();

		this.IActionPlugins = new IAction[this.IActionPluginsWaitingForInstanciate.size()];

		for(int index = 0 ; index < this.IActionPlugins.length; index ++ ){

			// Instanciation du plugin
			try {
				this.IActionPlugins[index] = (IAction)((Class<?>)this.IActionPluginsWaitingForInstanciate.get(index)).newInstance() ;
			} catch (InstantiationException | IllegalAccessException e) {
				Log.error("Error while instanciate '" + ((Class<?>)this.IActionPluginsWaitingForInstanciate.get(index)).getName() + "' Class from plugin",e);
			}

		}
		Log.info(this.IActionPlugins.length + " plugins has been loaded.");
	}

	private void initializeLoader() {
		//On verifie que la liste des plugins a charger a ete initialisee
		if(this.files == null || this.files.length == 0 ){
			Log.info("No plugin found");
			return;
		}

		//Pour eviter le double chargement des plugins
		if(this.IActionPluginsWaitingForInstanciate.size() != 0 ){
			System.out.println("Plugin already loaded");
			return ;
		}

		File[] files = new File[this.files.length];
		// Pour charger le .jar en memoire
		// Pour la comparaison de chaines
		String tmp = "";
		// Pour le contenu de l'archive jar
		Enumeration<JarEntry> enumeration;

		for(int index = 0 ; index < files.length ; index ++ ){
			files[index] = new File(Paths.get(this.pluginPath,this.files[index]).toString());
			if( !files[index].exists() ) {
				continue;
			}

			URL u;
			try {
				u = files[index].toURI().toURL();
			} catch (MalformedURLException e) {
				Log.error("Malformed Url", e);
				continue;
			}
			// URLClassLoader pour charger le jar (hors CLASSPATH)
			final URLClassLoader loader = new URLClassLoader(new URL[] {u});

			// On charge le jar en memoire
			try {
				enumeration = this.getJarEntry(files, index);
			} catch (IOException e) {
				Log.error("Error while loading plugin in memory", e);
				continue;
			}

			// Check if class implement IAction interface
			while(enumeration.hasMoreElements()){
				tmp = enumeration.nextElement().toString();

				try {
					this.searchIActionPlugin(tmp, loader);
				} catch (ClassNotFoundException e) {
					Log.error("Error while loading .class in plugin", e);
					break;
				}

			}
		}
	}

	private Enumeration<JarEntry> getJarEntry(File[] files, int index) throws IOException{
		final JarFile jar = new JarFile(files[index].getAbsolutePath());
		final Enumeration<JarEntry> enumeration = jar.entries();
		jar.close();
		return enumeration;
	}

	/**
	 * Check if class implement IAction interface.
	 * @param tmp File name
	 * @param loader Class loader
	 * @throws ClassNotFoundException Trigger if loader cannot load class found
	 */
	private void searchIActionPlugin(String tmp, ClassLoader loader) throws ClassNotFoundException{
		// Verifier que le fichier est un .class

		if(tmp.length() > 6 && tmp.substring(tmp.length()-6).compareTo(".class") == 0) {

			tmp = tmp.substring(0,tmp.length()-6);
			tmp = tmp.replaceAll("/",".");
			final Class<?> tmpClass = Class.forName(tmp ,true,loader);

			for(int i = 0 ; i < tmpClass.getInterfaces().length; i ++ ){
				// Verifier que la class utilise bien notre interface
				if(tmpClass.getInterfaces()[i].toString().compareTo("interface atos.mae.auto.plugins.requirement.IAction") == 0) {
					this.IActionPluginsWaitingForInstanciate.add(tmpClass);
				}
			}
		}
	}
}
