package atos.mae.auto.action;

import com.jcraft.jsch.*;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/**
 * Class used to manage SSH request.
 */
public class SSHManager
{
	/**
	 * Logger.
	 */
	private static final Logger Log = Logger.getLogger(SSHManager.class);

	/**
	 * SSH Channel.
	 */
	private JSch jschSSHChannel;

	/**
	 * Login.
	 */
	private String strUserName;

	/**
	 * Password.
	 */
	private String strPassword;

	/**
	 * IP or DNS.
	 */
	private String strConnectionIP;

	/**
	 * Port for IP or DNS.
	 */
	private int intConnectionPort;

	/**
	 * Session while connected.
	 */
	private Session sesConnection;

	/**
	 * TimeOut.
	 */
	private int intTimeOut;

	private void doCommonConstructorActions(String userName,
	     String password, String connectionIP, String knownHostsFileName)
	{
	   this.jschSSHChannel = new JSch();
	   JSch.setConfig("StrictHostKeyChecking", "no");

	   this.strUserName = userName;
	   this.strPassword = password;
	   this.strConnectionIP = connectionIP;
	}

	public SSHManager(String userName, String password,
	   String connectionIP, String knownHostsFileName)
	{
		this.doCommonConstructorActions(userName, password,
	              connectionIP, knownHostsFileName);
	   this.intConnectionPort = 22;
	   this.intTimeOut = 60000;
	}

	public SSHManager(String userName, String password, String connectionIP,
	   String knownHostsFileName, int connectionPort)
	{
		this.doCommonConstructorActions(userName, password, connectionIP,
	      knownHostsFileName);
		this.intConnectionPort = connectionPort;
		this.intTimeOut = 60000;
	}

	public SSHManager(String userName, String password, String connectionIP,
	    String knownHostsFileName, int connectionPort, int timeOutMilliseconds)
	{
		this.doCommonConstructorActions(userName, password, connectionIP,
	       knownHostsFileName);
		this.intConnectionPort = connectionPort;
		this.intTimeOut = timeOutMilliseconds;
	}

	/**
	 * Connect to a server.
	 * @return
	 * @throws JSchException
	 */
	public void connect() throws JSchException
	{
	   this.sesConnection = this.jschSSHChannel.getSession(this.strUserName,
			   this.strConnectionIP, this.intConnectionPort);
	   this.sesConnection.setPassword(this.strPassword);
	   // UNCOMMENT THIS FOR TESTING PURPOSES, BUT DO NOT USE IN PRODUCTION
	   // sesConnection.setConfig("StrictHostKeyChecking", "no");
	   this.sesConnection.connect(this.intTimeOut);
	}

	/**
	 * Connect and send ssh command to a server.
	 * @param command SSH command
	 * @return Result of SSH command as string
	 */
	final public String sendCommand(String command)
	{
	   StringBuilder outputBuffer = null;
	   boolean bExec = false;
	   Channel channel = null;
	   int iTry = 0;
	   do{
		   try
		   {
			  iTry++;
			  Log.info("try number " + iTry + " to execute SSH command : " + command );
			  outputBuffer = new StringBuilder();
		      channel = this.sesConnection.openChannel("exec");
		      Thread.sleep(2000);
		      ((ChannelExec)channel).setCommand(command);
		      final InputStream commandOutput = channel.getInputStream();
		      channel.connect();
		      int readByte = commandOutput.read();

		      while(readByte != 0xffffffff)
		      {
		         outputBuffer.append((char)readByte);
		         readByte = commandOutput.read();
		      }

		      bExec = true;
		   }
		   catch(IOException e){
			   Log.error("Error while reading ssh response", e);
		   }catch(JSchException | InterruptedException e){
			   Log.error("Error during ssh request execution", e);
		   } finally{
			   if(channel != null)
				   channel.disconnect();
		   }
	   }while(!bExec && iTry < 5);

	   if(!bExec){
		   Log.info("Cannot execute SSH after 5 try");
		   return null;
	   }
	   return outputBuffer.toString();
	}

	public void close()
	{
		this.sesConnection.disconnect();
	}

}