package atos.mae.auto;

import com.github.markusbernhardt.proxy.ProxySearch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ProxySelector;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.UnhandledAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import atos.mae.auto.hpalm.HpAlm;
import atos.mae.auto.json.JsonLoader;
import atos.mae.auto.model.TestSetModel;
import atos.mae.auto.model.TestStepModel;
import atos.mae.auto.plugins.PluginsLoader;
import atos.mae.auto.plugins.requirement.WebDriverProvider;
import atos.mae.auto.utils.Liste;
import atos.mae.auto.utils.Tools;
import atos.mae.auto.utils.Report;
import atos.mae.auto.action.Action;
import atos.mae.auto.excel.TestSetLoader;
import atos.mae.auto.excel.TestSetService;
import atos.mae.auto.excel.TestStepLoader;
import atos.mae.auto.excel.TestStepService;

/**
 * Main class.
 */
@Component
public class Main {
	/**
	 * Logger.
	 */
	private static final Logger Log = LoggerFactory.getLogger(Main.class);

	@Value("${actionPath:Actions.txt}")
	private String actionPath;

	@Value("${pluginPath:Plugin}")
	private String pluginPath;

	@Value("${reportConfig:Configuration/Report-config.xml}")
	private String reportConfig;

	@Value("${logXmlPath:Configuration/log4j.xml}")
	private String logXmlPath;

	@Value("${reportPath:Report}")
	private String reportPath;

	@Value("${webDriverPath:Configuration/WebDriver}")
	private String webDriverPath;

	@Value("${webDriverPathUnix:Configuration/WebDriver/Unix}")
	private String webDriverPathUnix;
	
	@Value("${objectRepository:Configuration/ObjectRepository.json}")
	private String objectRepository;

	@Value("${resourcePath:.}")
	private String resourcePath;

	@Value("${testLibraryPath:TestLibrary}")
	private String testLibraryPath;

	@Value("${modulePath:Modules}")
	private String modulePath;
	
	// code change for 30616

	@Value("${objectRepositoryxlsm:Configuration/ObjectRepository.xlsm}")
	  private String objectRepositoryxlsm;
	
	@Value("${environmentsxlsm:Configuration/Environments.xlsm}")
	  private String environmentsxlsm; 

	//// end code change for 30616
	
	// start code changed by Princi for WI - 76654
	@Value("${sikuliImagePath:Configuration/ORPicture}")
	private String sikuliImagePath;
	// end code changed by Princi for WI - 76654

	/**
	 * Plugin loader for export method.
	 */
	@Autowired
	private PluginsLoader pluginLoader;

	/**
	 * Json loader.
	 */
	@Autowired
	private JsonLoader jsonLoader;

	/**
	 * Loading TestStep class.
	 */
	@Autowired
	private TestStepLoader testStepLoader;

	/**
	 * Execute TestStep class.
	 */
	@Autowired
	private TestStepService testStepService;

	/**
	 * Loading TestSet class.
	 */
	@Autowired
	private TestSetLoader testSetLoader;

	/**
	 * Execute TestSet class.
	 */
	@Autowired
	private TestSetService testSetService;

	@Autowired
	private Tools tools;

	@Autowired
	private Liste liste;

	@Autowired
	private Action action;

	@Autowired
	private WebDriverProvider webDriverProvider;

	@Autowired
	private Report report;

	@Autowired
	private HpAlm hpAlm;
	

	/**
	 * Main method.
	 *
	 * <p>Command line parameters can be :
	 * <ul>
	 * 	<li> --testset=file : use file as TestSet</li>
	 *  <li> --file=file : use file as TestStep</li>
	 *  <li> --config : generate config file at the root jar</li>
	 *  <li> --setup : Create base environment (config, directory, templates, ...)</li>
	 *  <li> --exportactions : export list method and parameter that excel can use in file</li>
	 * </ul>
	 *
	 * @param arg Command line parameter
	 */
	public static void main(final String[] args) {
		String path = "/Configuration/log4j.xml";
		
		File f = new File(path);
		if(f.exists())
			PropertyConfigurator.configure(path);

		f = new File(Paths.get("Configuration/Config.properties").toString());
		if(!f.exists()){
			try{
			config();
			}catch(IOException e){
				
			}
			
			return;
		}
		
		f = new File(Paths.get("Configuration/webdrivermanager.properties").toString());
		if(!f.exists()){
			webdrivermanager();
		}
		

		final ApplicationContext context = new ClassPathXmlApplicationContext("config.xml");
		final Main p = context.getBean(Main.class);
		try{
			p.start(args);
		}catch(RuntimeException e){
			Log.error("",e);
		}finally{
			((ConfigurableApplicationContext)context).close();
          // code changes  for WI 30618
	        p.getWebDriverProvider().closeWebDriver();
//	        try{
//	        	Log.info("Its in delete foder ");
//	    		File sourceDir = new File("./Configuration/WebDriver/chromedriver");
//	    		if(sourceDir.exists())
//	    		FileUtils.deleteDirectory(sourceDir);
//	    		
//	    		
//	    		}catch(Exception e){
//	    			
//	    			e.printStackTrace();
//	    		}
	      // end code changes  for WI 30618
		}


	}
	
	// code changes  for WI -30618
	public WebDriverProvider getWebDriverProvider() {
		return webDriverProvider;
	}
	// end of code changes  for WI -30618



	final public void start(final String[] args){
		if(args == null || args.length == 0 || args[0].trim().isEmpty()){
			this.setUp();
			return;
		}

		//Searching if a proxy is used to internet connection
		ProxySearch proxySearch = ProxySearch.getDefaultProxySearch();
		proxySearch.addStrategy(ProxySearch.Strategy.OS_DEFAULT);
		proxySearch.addStrategy(ProxySearch.Strategy.JAVA);
		proxySearch.addStrategy(ProxySearch.Strategy.BROWSER);
		ProxySelector proxySelector = proxySearch.getProxySelector();
		
		//If proxy find set useSystemProxies to True else set to false
		if (proxySelector != null) {
			ProxySelector.setDefault(proxySelector);
			System.setProperty("java.net.useSystemProxies", "true");
			Log.info("System use proxy to internet connection");
		}else{
			System.setProperty("java.net.useSystemProxies", "false");
			Log.info("System do not use proxy to internet connection");
		}
		
		
		
		/*URI home = URI.create("http://www.google.com");
		System.out.println("ProxySelector: " + proxySelector);
		System.out.println("URI: " + home);
		List<Proxy> proxyList = proxySelector.select(home);
		if (proxyList != null && !proxyList.isEmpty()) {
		 for (Proxy proxy : proxyList) {
		   System.out.println(proxy);
		   SocketAddress address = proxy.address();
		   if (address instanceof InetSocketAddress) {
		     String host = ((InetSocketAddress) address).getHostName();
		     String port = Integer.toString(((InetSocketAddress) address).getPort());
		     System.setProperty("http.proxyHost", host);
		     System.setProperty("http.proxyPort", port);
		   }
		 }
		}*/

		for (final String params : args) {
			final String[] param = params.split("=");
			switch(param[0]){
				case "--testset":
				case "-t" :
					this.launchTest(param,true);
					break;
				case "--file":
				case "-f":
					this.launchTest(param,false);
					break;
				case "--config":
				case "-c":
					try{
					this.config();
					}catch(IOException e){
						
					}
					break;
				case "--webdrivermanager":
				case "-w":
					this.webdrivermanager();
					break	;
				case "--setup":
				case "-s":
					this.setUp();
					break;
				case "--exportactions":
				case "-e":
					 this.exportAction();
					break;
				case "--test":
					try {
						this.jsonLoader.LoadJson();
					} catch (UnsupportedEncodingException | FileNotFoundException e) {
						Log.error("Error while reading json file",e);
						return;
					}

					this.liste.setModulesAvailable();

					this.pluginLoader.setFilesPath(this.pluginPath);
					this.pluginLoader.loadAllActionPlugins();


					//this.action.openBrowser("Chrome");

					//this.liste.setModulesAvailable();
					//final TestStepModel tsm = (TestStepModel)this.testStepLoader.load("Test_Dev");


					break;
				default:
					break;
			}
		}
	}

	/**
	 * Export action available with parameters in 'Actions.txt' file
	 */
	public void exportAction(){
		try {
			final PrintWriter writer = new PrintWriter(new FileOutputStream(this.actionPath, false));
			this.tools.ExportAction(writer);
			this.tools.ExportPluginAction(writer);
			this.tools.ExportModule(writer);
			writer.close();
		} catch (FileNotFoundException e) {
			Log.error("Error while creating '" + this.actionPath + "' file.",e);
		}
	}

	/**
	 * Create config.properties if not exist with default configuration
	 */
	public static void config() throws IOException{
		File dir = new File("Configuration");
		dir.mkdirs();
		File config = new File("Configuration/Config.properties");
		
		File srcDir = new File("Configuration/Sikulix");
		File destDir = new File("Configuration/Sikulix");
		FileUtils.copyDirectory(srcDir, destDir);
		
		if(!config.exists())
			ResourceCopy("Config.properties", "Configuration/Config.properties");
	}
	
	public static void webdrivermanager(){
		File dir = new File("Configuration");
		dir.mkdirs();
		File webdrivermanager = new File("Configuration/webdrivermanager.properties");
		if(!webdrivermanager.exists())
			ResourceCopy("webdrivermanager.properties", "Configuration/webdrivermanager.properties");
	}

	/**
	 * Launch the test
	 * @param param parrameters
	 * @param isTestSet is testSet
	 */
	private void launchTest(String[] param, boolean isTestSet){
		if(param[1] == null){
			Log.error("File name not found");
		}

		Log.info("Application started" + ZonedDateTime.now());

		try {
			this.jsonLoader.LoadJson();
		} catch (UnsupportedEncodingException | FileNotFoundException e) {
			Log.error("Error while reading json file",e);
			return;
		}
		this.liste.setModulesAvailable();

		this.pluginLoader.setFilesPath(this.pluginPath);
		this.pluginLoader.loadAllActionPlugins();

		String TestName = param[1];
		if(!TestName.endsWith(".xlsm")){
			final StringBuilder sb = new StringBuilder(TestName);
			sb.append(".xlsm");
			TestName = sb.toString();
		}
		
		// 3rd parameter environment if it is  specified
		String envParam = "";
		if(param.length > 2) {
			 envParam = param[2];
		}
		
		try{
			if(isTestSet){
				final TestSetModel tsm = this.testSetLoader.load(TestName, envParam);
				this.testSetService.execute(tsm);
			}else{
				final TestStepModel tsm = (TestStepModel)this.testStepLoader.load(TestName, envParam);
				try{
					this.testStepService.execute(tsm);
				}catch(UnhandledAlertException uae){
					// ignore
				}
			}
		}catch(RuntimeException e){
			Log.error("",e);
			this.webDriverProvider.closeDriver();
			this.report.endtest();
		}finally{
			this.hpAlm.disconnect();
		}


	}

	/**
	 * Extract file from jar.
	 * @param in The file path in jar you need to extract
	 * @param out The destination file path
	 */
	public static void ResourceCopy(String in, String out){
		try {
			final InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(in);
			final FileOutputStream output = new FileOutputStream(out);

			final byte [] buffer = new byte[4096];
			int bytesRead = input.read(buffer);
			while (bytesRead != -1) {
			    output.write(buffer, 0, bytesRead);
			    bytesRead = input.read(buffer);
			}
			output.close();
			input.close();
		} catch (IOException e) {
			Log.error("Error while extracting file from jar",e);
		}
	}

	/**
	 * Create base environment.
	 */
	public void setUp(){
		try{
		this.config();
		}catch(IOException e){
			
		}
		this.webdrivermanager();
		//report config
		ResourceCopy("extent-config.xml", this.reportConfig);
		ResourceCopy("log4j.xml", this.logXmlPath);

		// Make directory
		File dir = new File(this.reportPath);
		dir.mkdirs();
//		dir = new File(this.config.LogPath);
//		dir.mkdirs();


		// Copy webdriver
		dir = new File(this.webDriverPath);
		dir.mkdirs();		
		
		// Make default ObjectRepository
		final String path = this.objectRepository;
		final File file = new File(path);
		if(!file.exists()){
			final String OR = JsonLoader.defaultObjectRepository();
			try {
				final FileOutputStream out = new FileOutputStream(path);
				out.write(OR.getBytes());
				out.close();
			} catch (IOException e) {
				Log.error(e.getMessage());
				return;
			}
		}

		// code change for 30616
		
		final String pathORX = this.objectRepositoryxlsm;
		final File fileORX = new File(pathORX);
		if(!fileORX.exists()){
			 ResourceCopy("ObjectRepository.xlsm", Paths.get("Configuration","ObjectRepository.xlsm").toString());			
		}
		
		final String pathEX = this.environmentsxlsm;
		final File fileEX = new File(pathEX);
		if(!fileEX.exists()){
			ResourceCopy("Environments.xlsm", Paths.get("Configuration","Environments.xlsm").toString());		
		}
		
		// end code change for 30616
		
		
		// Copy template
		ResourceCopy("TestSet_Template.xlsm", Paths.get(this.resourcePath,"TestSet_Template.xlsm").toString());
		// commented for code change for 30616
		//ResourceCopy("ObjectRepository.xlsm", Paths.get("Configuration","ObjectRepository.xlsm").toString());
		//ResourceCopy("Environments.xlsm", Paths.get("Configuration","Environments.xlsm").toString());
		// end commented for code change for 30616
		ResourceCopy("DataServer.json", Paths.get("Configuration","DataServer.json").toString());
		dir = new File(Paths.get(this.testLibraryPath,"Test_Template").toString());
		dir.mkdirs();
		ResourceCopy("Test_Template.xlsm", Paths.get(this.testLibraryPath,"Test_Template","Test_Template.xlsm").toString());
		dir = new File(Paths.get(this.modulePath,"Mod_Template").toString());
		dir.mkdirs();
		ResourceCopy("Mod_Template.xlsm", Paths.get(this.modulePath,"Mod_Template","Mod_Template.xlsm").toString());
		
		// start code changed by Princi for WI - 76654
		dir  =  new File(this.sikuliImagePath);
		dir.mkdirs();
		// end code changed by Princi for WI - 76654
		
	}
}
