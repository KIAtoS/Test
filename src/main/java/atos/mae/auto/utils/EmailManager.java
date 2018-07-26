package atos.mae.auto.utils;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;

public class EmailManager {

	@Value("${mail_Smtp_Host:}")
	private String mail_Smtp_Host;

	public void sendMail(String From, String To){

	      // Get system properties
	      final Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", this.mail_Smtp_Host);

	      // Get the default Session object.
	      final Session session = Session.getDefaultInstance(properties);

	      try{
	         // Create a default MimeMessage object.
	         final MimeMessage message = new MimeMessage(session);

	         // Set From: header field of the header.
	         message.setFrom(new InternetAddress(From));

	         // Set To: header field of the header.
	         message.addRecipient(Message.RecipientType.TO, new InternetAddress(To));

	         // Set Subject: header field
	         message.setSubject("This is the Subject Line!");

	         // Send the actual HTML message, as big as you like
	         message.setContent("<h1>This is actual message</h1>", "text/html" );

	         // Send message
	         Transport.send(message);
	      }catch (MessagingException mex) {
	         mex.printStackTrace();
	      }
	}
}
