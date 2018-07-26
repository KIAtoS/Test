package atos.mae.auto.plugins.requirement;

import io.github.bonigarcia.wdm.ChromeDriverManager;
import io.github.bonigarcia.wdm.EdgeDriverManager;
import io.github.bonigarcia.wdm.FirefoxDriverManager;
import io.github.bonigarcia.wdm.InternetExplorerDriverManager;
import io.github.bonigarcia.wdm.PhantomJsDriverManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.UnhandledAlertException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
//import org.openqa.selenium.firefox.FirefoxProfile;
//import org.openqa.selenium.firefox.MarionetteDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
//import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import atos.mae.auto.utils.Report;
import atos.mae.auto.utils.Exceptions.DriverNotExistException;
import atos.mae.auto.utils.Exceptions.NoDriverDefineException;
import atos.mae.auto.utils.Exceptions.UrlRemoteDriverUndefined;
import atos.mae.auto.utils.enums.EnvironnementExecutionEnum;
import atos.mae.auto.utils.enums.TagReport;



/**
 * WebDriver provider class.
 */
@Component
public class WebDriverProvider {

	@Value("${webDriverPath:Configuration/WebDriver}")
	private String webDriverPath;

	@Value("${webDriverPathUnix:Configuration/WebDriver/Unix}")
	private String webDriverPathUnix;

	@Value("${webDriverTimeOut:30}")
	private long webDriverTimeOut;

	@Value("${webDriverPollingEvery:5}")
	private long webDriverPollingEvery;


	@Value("${reportPath:Report}")
	private String reportPath;

	@Value("${BrowserStackUrl:}")
	private String BrowserStackUrl;
	
	@Autowired
	private Report report;

	/**
	 * WebDriver used to interact with selenium.
	 */
	private WebDriver Driver;

	/**
	 * Environment execution (local, remote or browserStack).
	 */
	private EnvironnementExecutionEnum EnvExec = EnvironnementExecutionEnum.LOCAL;

	/**
	 * Url to access remote webdriver.
	 */
	private String urlRemoteDriver;

	/**
	 * BrowserStack capabilities.
	 */
	private DesiredCapabilities desiredCapabilities;

	/**
	 * WebDriver wait.
	 */
	private FluentWait<WebDriver> Wait;

	/**
	 * WebDriver javascript executor.
	 */
	private JavascriptExecutor Jse;

	/**
	 * WebDriver TakesScreenshot.
	 */
	private TakesScreenshot ts;

	/**
	 * Logger.
	 */
	private static Logger Log = Logger.getLogger(WebDriverProvider.class);

	/**
	 * Set the webDriver, the path of webdriver and open browser.
	 * @param webDriver Browser name
	 */

	/**
	 * Set the webDriver, the path of webdriver and open browser.
	 * @param webDriver Browser name


	 */
	public void setWebDriver(String webDriver){
		
		
		//code change for WI 49344
		InetSocketAddress addr = null;
		String host = null;
		int port = 0 ;
		 try {
	            System.setProperty("java.net.useSystemProxies","true");
	            List<Proxy> l = ProxySelector.getDefault().select(
	                        new URI("http://www.yahoo.com/"));

	            for (Iterator<Proxy> iter = l.iterator(); iter.hasNext(); ) {

	                Proxy proxy = iter.next();

	               // System.out.println("proxy hostname : " + proxy.type());

	                addr = (InetSocketAddress)proxy.address();

	                if(addr == null) {

	                  //  System.out.println("No Proxy");

	                } else {
	                	
	                    host = addr.getHostName();
	                    port = addr.getPort();
	                   // System.out.println("proxy hostname : " +host );
	                   // System.out.println("proxy port : " + port);
	                    
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		 
		   String Systemproxy =host+":"+port;
		 
		 //end //code change for WI 49344
		
		if(this.EnvExec == EnvironnementExecutionEnum.BROWSERSTACK){
			URL url;
			try {

				url = new URL(this.BrowserStackUrl);
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
			if(this.Driver != null)
				this.Driver.quit();

			this.Driver = new RemoteWebDriver(url, this.desiredCapabilities);
		} else if(this.EnvExec == EnvironnementExecutionEnum.REMOTE){
			this.closeDriver();
			DesiredCapabilities dc = null;
			switch (webDriver.toUpperCase()){
			    case "FIREFOX":
					dc = DesiredCapabilities.firefox();
					break;
				case "CHROME":
					dc = DesiredCapabilities.chrome();
					break;
				case "IE":
					dc = DesiredCapabilities.internetExplorer();
					break;
				case "EDGE":
					dc = DesiredCapabilities.edge();
					break;
				case "PHANTOMJS":
					dc = DesiredCapabilities.phantomjs();
					break;					
					// code change for 30843
				case "HTMLUNIT":
					dc = DesiredCapabilities.htmlUnit();
					break;
					// end code change for 30843
					
				default:
					return;
			}


			try{
				// Augmenter allow to take Screenshot
				this.Driver = new Augmenter().augment(new RemoteWebDriver(new URL(this.getUrlRemoteDriver()) , dc));
			}catch(MalformedURLException e){
				Log.error("Error while taking screenshot",e);
			}
		} else {
			switch (webDriver.toUpperCase()){
				case "FIREFOX":
					this.report.addTag(TagReport.MOZILLA);
					//final FirefoxProfile profile = new FirefoxProfile();
					//explicitly enable native events(this is mandatory on Linux system, since they
					//are not enabled by default
					//profile.setEnableNativeEvents(true);
					//this.Driver = new Augmenter().augment(new FirefoxDriver(profile));
					
					//code change for WI 49344
					FirefoxDriverManager.getInstance().proxy(Systemproxy).setup();	
//					try{
//						File source = new File("./src/main/resources/geckodriver/win32/0.16.1/geckodriver.exe");
//						//File sourceDir = new File("./src/main/resources/geckodriver");
//						File dest = new File("./src/main/resources/geckodriver.exe");
//						FileUtils.copyFile(source, dest);
//						//FileUtils.deleteDirectory(sourceDir);
//						}catch(Exception e){e.printStackTrace();}
					// end //code change for WI 49344
					//System.setProperty("webdriver.gecko.driver", Paths.get(this.webDriverPath,"geckodriver.exe").toString());
					//FirefoxDriverManager.getInstance().setup(); 
					final DesiredCapabilities capabilities = DesiredCapabilities.firefox();
					capabilities.setCapability("marionette", true);
					this.Driver = new FirefoxDriver(capabilities);
					//this.Driver = new MarionetteDriver(capabilities);
					break;
				case "CHROME":
					this.report.addTag(TagReport.CHROME);											
					//code change for WI 49344										
					ChromeDriverManager.getInstance().proxy(Systemproxy).setup();			
					//end//code change for WI 49344
					this.Driver = new Augmenter().augment(new ChromeDriver());
					//TODO : delete "Chrome is being controlled by automated test software" notification --> solution http://support.applitools.com/customer/en/portal/articles/2783976-%22chrome-is-being-controlled-by-automated-test-software%22-notification?t=628019
					//System.setProperty("webdriver.chrome.driver",Paths.get(this.webDriverPath,"chromedriver.exe").toString());
					//this.Driver = new Augmenter().augment(new ChromeDriver());
					break;
				case "IE":
					this.report.addTag(TagReport.INTERNETEXPLORER);
					//System.setProperty("webdriver.ie.driver", Paths.get(this.webDriverPath,"IEDriverServer.exe").toString());
					//code change for WI 49344
					InternetExplorerDriverManager.getInstance().proxy(Systemproxy).setup();	
					
					// end //code change for WI 49344
					final DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
					caps.setCapability(InternetExplorerDriver.IGNORE_ZOOM_SETTING, true);
					caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
					this.Driver = new Augmenter().augment(new InternetExplorerDriver(caps));
					break;
				case "EDGE":
					this.report.addTag(TagReport.EDGE);
					//code change for WI 49344
					EdgeDriverManager.getInstance().proxy(Systemproxy).setup();	
					//end //code change for WI 49344
					//System.setProperty("webdriver.edge.driver", Paths.get(this.webDriverPath,"MicrosoftWebDriver.exe").toString());
					this.Driver = new Augmenter().augment(new EdgeDriver());
					break;
					// code change for 30843
				case "PHANTOMJS":
					this.report.addTag(TagReport.PHANTOMJS);
					final DesiredCapabilities phantomCapabilities =new  DesiredCapabilities();									
					phantomCapabilities.setJavascriptEnabled(true);
					phantomCapabilities.setCapability("takesScreenshot", true); 
					//code change for WI 49344
					PhantomJsDriverManager.getInstance().proxy(Systemproxy).setup();	
					// end //code change for WI 49344
					
					this.Driver = new Augmenter().augment(new PhantomJSDriver(phantomCapabilities));
					//phantomCapabilities.setCapability("phantomjs.binary.path", this.webDriverPath +"/phantomjs.exe");
					//this.Driver= new PhantomJSDriver(phantomCapabilities);
					break;
					// end code change for 30843
				case "HTMLUNIT":
					this.report.addTag(TagReport.HTMLUNIT);
					final DesiredCapabilities htmlcapabilities = DesiredCapabilities.htmlUnit();
					htmlcapabilities.setJavascriptEnabled(true);
					//htmlcapabilities.setCapability("takesScreenshot", true); 
					this.Driver= new ScreenCaptureHtmlUnitDriver(htmlcapabilities);
					break;					
				default:
					throw new DriverNotExistException();
			}

		}
		
		this.Driver.manage().window().maximize();
		//this.Driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		this.Wait = new FluentWait<WebDriver>(this.Driver)
	    .withTimeout(this.webDriverTimeOut, TimeUnit.SECONDS)
	    .pollingEvery(this.webDriverPollingEvery, TimeUnit.SECONDS)
	    .ignoring(NoSuchElementException.class);
		//this.Wait = new WebDriverWait(this.Driver, this.webDriverTimeOut);
		this.Jse = ((JavascriptExecutor)this.getWebDriver());
		this.ts = (TakesScreenshot)(this.Driver);
		
		//
		
	}

	

	/**
	 * Get webDriver.
	 * @return webDriver
	 * @throw NoDriverDefineException if webDriver is null
	 */
	public WebDriver getWebDriver() {
		if(this.Driver == null)
			throw new NoDriverDefineException();
		return this.Driver;
	}

	/**
	 * Get JavascriptExecutor.
	 * @return JavascriptExecutor
	 * @throw NoDriverDefineException if webDriver is null
	 */
	public JavascriptExecutor getJavascriptExecutor(){
		if(this.Driver == null)
			throw new NoDriverDefineException();
		return this.Jse;
	}

	/**
	 * Get Wait.
	 * @return Wait
	 * @throw NoDriverDefineException if webDriver is null
	 */
	public FluentWait<WebDriver> getWait(){
		if(this.Driver == null)
			throw new NoDriverDefineException();
		return this.Wait;
	}

	/**
	 * Environment execution getter.
	 */
	public EnvironnementExecutionEnum getEnvExec() {
		return this.EnvExec;
	}

	/**
	 * Environment execution setter.
	 * @param env environment execution from excel
	 */
	public void setEnvExec(int env) {
		switch(env){
			case 1:
				//local
				this.EnvExec = EnvironnementExecutionEnum.LOCAL;
				Log.info("execution environnement : Local");
			break;
			case 2:
				// remote
				this.EnvExec = EnvironnementExecutionEnum.REMOTE;
				Log.info("execution environnement : Remote");
				break;
			case 3:
				// browserStack
				this.EnvExec = EnvironnementExecutionEnum.BROWSERSTACK;
				Log.info("execution environnement : BrowserStack");
				break;
			default:
				//local
				this.EnvExec = EnvironnementExecutionEnum.LOCAL;
				Log.info("execution environnement Unknown, using default : Local");
				break;
		}
	}



	/**
	 * Remote driver url getter.
	 * @return url
	 */
	public String getUrlRemoteDriver() {
		if(this.urlRemoteDriver == null)
			throw new UrlRemoteDriverUndefined();
		return this.urlRemoteDriver;
	}

	/**
	 * Remote driver url setter.
	 * @param urlRemoteDriver url from excel
	 */
	public void setUrlRemoteDriver(String urlRemoteDriver) {
		// CPD : modification for WI 57207
		if(urlRemoteDriver == null)
			throw new UrlRemoteDriverUndefined();
		this.urlRemoteDriver = urlRemoteDriver;
	}

	/**
	 * Close browser and tear down driver.
	 */
	public void closeDriver(){
		if(this.Driver != null)
			this.Driver.quit();
		this.leaveDriver();
	}

	/**
	 * Tear down driver.
	 */
	public void leaveDriver(){
		this.Driver = null;
		this.Wait = null;
		this.Jse = null;
		this.ts = null;
	}
	
	/**
	 * Close web driver.
	 */
	
	
	// code changes for WI -  30618
	public void closeWebDriver(){
		try
		{
		if(this.Driver != null){
			this.Driver.quit();			
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	// code changes end for WI -  30618
			

	/**
	 * Take screenshot of browser from webDrivers, copy it in report's directory and return path.
	 * @param ReportName Report's directory's name of this run
	 * @param TestName TestStep file name
	 * @param StepName Step name in excel file
	 * @return Path to insert in report
	 */
	public String takeScreenshot(String ReportName ,String TestName, String StepName){
		if(this.ts == null)
			return null;
		try {
			Log.info("WebDriver used : " + this.Driver.getClass().getSimpleName());
			if ("ScreenCaptureHtmlUnitDriver".equals(this.Driver.getClass().getSimpleName())){
				final String PathToReturn = Paths.get(TestName + "_" + StepName + "_" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_kk_mm_ss")) + ".zip").toString();
				final String Path = Paths.get(this.reportPath,ReportName,PathToReturn).toString();
				byte[] zipFileBytes = this.ts.getScreenshotAs(OutputType.BYTES);
	            FileUtils.writeByteArrayToFile(new File(Path), zipFileBytes);
	            return PathToReturn;
				}
			else {
				final String PathToReturn = Paths.get(TestName + "_" + StepName + "_" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_kk_mm_ss")) + ".png").toString();
				final String Path = Paths.get(this.reportPath,ReportName,PathToReturn).toString();
				
				
				try {
					File scrFile = this.ts.getScreenshotAs(OutputType.FILE);
					FileUtils.copyFile(scrFile, new File(Path));
				} catch (UnhandledAlertException e) {
					// ignore exception
					//TODO Put Focus on alert with switchTo.Alert function before taking screenshot
					//https://stackoverflow.com/questions/21670361/capture-screenshot-of-alert
				}
				return PathToReturn;
			    }
		
		} catch (IOException e) {
			Log.error("Error during screenshot copy",e);
		}
		return null;
	}

	public DesiredCapabilities getDesiredCapabilities() {
		return desiredCapabilities;
	}

	public void setDesiredCapabilities(DesiredCapabilities desiredCapabilities) {
		this.desiredCapabilities = desiredCapabilities;
	}


}




