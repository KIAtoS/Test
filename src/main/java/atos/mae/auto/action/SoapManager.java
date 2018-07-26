package atos.mae.auto.action;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import javax.xml.soap.*;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import atos.mae.auto.plugins.requirement.StepReturn;
import atos.mae.auto.plugins.requirement.StepReturnEnum;
import atos.mae.auto.utils.Report;
import atos.mae.auto.utils.Tools;

/**
 * Class used to manage soap request.
 */
public class SoapManager {

	/**
	 * Logger.
	 */
	private static Logger Log = Logger.getLogger(SoapManager.class);

	/**
	 * Soap API Url.
	 */
	private String url;

	@Value("${reportPath:Report}")
	private String reportPath;

	@Autowired
	private Tools tools;

	@Autowired
	private Report report;


	/**
	 * Constructor.
	 * @param url Soap API Url
	 */
	public SoapManager(String url){
		this.url = url;
	}

	/**
	 * Soap request from string and store result in file.
	 * @param XmlContent Soap request as string
	 * @param FileName File's name where to store result
	 * @return Error if XmlContent is empty, error during soap request or store result in file, else Pass
	 */
    public StepReturn requestFromString(String XmlContent, String FileName) {
        OutputStream output = null;
		try {
			// Create SOAP Connection
			final SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			final SOAPConnection soapConnection = soapConnectionFactory.createConnection();


	    	if(XmlContent == null || XmlContent.trim().isEmpty())
	    		return new StepReturn(StepReturnEnum.ERROR,"Soap message is empty");

	    	// Create SOAP Message
	    	final MessageFactory factory = MessageFactory.newInstance();
	    	final SOAPMessage soapMessage = factory.createMessage(new MimeHeaders(), new ByteArrayInputStream(XmlContent.getBytes(Charset.forName("UTF-8"))));

	    	// Send SOAP Message to SOAP Server
	    	final SOAPMessage soapResponse = soapConnection.call(soapMessage, this.url);

	        output = new OutputStream()
	        {
	            private StringBuilder string = new StringBuilder();
	            @Override
	            public void write(int b) throws IOException {
	                this.string.append((char) b );
	            }

	            public String toString(){
	                return this.string.toString();
	            }
	        };
	        soapResponse.writeTo(output);
	        soapConnection.close();

		} catch (UnsupportedOperationException | IOException |SOAPException e) {
			return new StepReturn(StepReturnEnum.ERROR,e.getMessage());
		}

		FileOutputStream out = null;
		try {
			out = new FileOutputStream(Paths.get(this.reportPath,this.report.getReportName(),FileName).toString());
			final String dataString = this.tools.XmlprettyFormat(output.toString());
			if(dataString == null)
				return new StepReturn(StepReturnEnum.ERROR, "Error while formatting : " + output.toString());
			final byte data[] = dataString.getBytes();
			out.write(data);
		} catch (FileNotFoundException e) {
			Log.error(e.getMessage());
		} catch (IOException e) {
			Log.error(e.getMessage());
		} finally{
			try {
				if(out != null)
					out.close();
			} catch (IOException e) {
				Log.error(e.getMessage());
			}
		}

		return new StepReturn(StepReturnEnum.PASS, "SOAP response : <a href=\"" + FileName + "\">" + FileName + "</a>");
    }


}
