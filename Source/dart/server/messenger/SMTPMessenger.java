package dart.server.messenger;

import java.security.Security;
import java.util.*;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.*;

import dart.DartServer;
import dart.server.Container;
import dart.server.Project;
import dart.server.event.*;
import dart.server.messenger.*;

public class SMTPMessenger extends Messenger {
  static Logger logger = Logger.getLogger ( SMTPMessenger.class );   

  /**
   * Authenticator class used when SMTP authenticaton is needed.
   */
  class SMTPMessengerAuthenticator extends Authenticator {
    Properties properties = null;

    public SMTPMessengerAuthenticator(Properties p) { properties = p; }
    
    public void setProperties(Properties p) { properties = p; }
    
    protected PasswordAuthentication getPasswordAuthentication() {
      String user = null;
      String password = null;

      if (properties != null) {
        user = properties.getProperty("mail.user");
        if (properties.getProperty("mail.smtp.user") != null) {
          user = properties.getProperty("mail.smtp.user");
        }
        if (properties.getProperty("mail.smtps.user") != null) {
          user = properties.getProperty("mail.smtps.user");
        }
        
        password = properties.getProperty("mail.password");
        if (properties.getProperty("mail.smtp.password") != null) {
          password = properties.getProperty("mail.smtp.password");
        }
        if (properties.getProperty("mail.smtps.password") != null) {
          password = properties.getProperty("mail.smtps.password");
        }
        
        if (user != null && password != null) {
          return new PasswordAuthentication(user, password);
        }
      }

      return null;
    }
  }

  
  /**
   * Send a message over an SMTP connection
   */
  public void send(Collection recipients, String subject, String message) throws Exception {

    // Get a mail session and construct the message.  The
    // properties attached to the Messenger (specified in the
    // Project.xml file) are passed directly to the session,
    // configuring the mail host, port, encryption, etc. The
    // minimal set of JavaMail properties to send a message are:
    // 
    //     mail.host, mail.port, mail.from, and
    //     mail.transport.protocol
    //
    // If authentication is used, the properties
    //
    //     mail.smtp.auth, mail.user, and mail.password
    //
    // can be specified in the Listener properties.
    //
    // If encryption is needed, the property
    //
    //     mail.smtp.starttls.enable
    //
    // can be specified.
    //
    // For specific values or these properties, consult the
    // JavaMail API documentation.
    //

    Session mailSession = null;
    Authenticator authenticator = null;

    // if we need to authenticate, create a session with an
    // authenticator that pulls the user and password from the
    // Messenger's properties
    if ((properties.getProperty("mail.smtp.auth") != null
         && properties.getProperty("mail.smtp.auth").equals("true"))
        || (properties.getProperty("mail.smtps.auth") != null
            && properties.getProperty("mail.smtps.auth").equals("true"))) {
      authenticator = new SMTPMessengerAuthenticator( properties );
      mailSession = Session.getDefaultInstance(properties, authenticator);
    } else {
      mailSession = Session.getDefaultInstance(properties, null);
    }

    // if we need to use transport layer security, add a security
    // provider
    if (properties.getProperty("mail.smtp.starttls.enable") != null
        && properties.getProperty("mail.smtp.startls.enable").equals("true")) {
      Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());      
    }
    
    MimeMessage msg = new MimeMessage(mailSession);
    
    // set the "From: field
    msg.setFrom(new InternetAddress(properties.getProperty("mail.from")));
    
    // set the "To:" field.  The recipients are already email
    // addresses, so we do not need to map to a user for the transport
    // mechanism.
    Iterator rit = recipients.iterator();
    while (rit.hasNext()) {
      msg.addRecipient(Message.RecipientType.TO,
                       new InternetAddress((String)rit.next()));
    }
    
    // set the "Subject:" field
    msg.setSubject(subject);
    
    // set the body of the message
    msg.setText( message );
    
    // Send the message
    try {
      Transport.send(msg);
    } catch (MessagingException me) {
      logger.error("Messaging exception. Unable to send notification. " + me);
    } catch (Exception e) {
      logger.error("Unable to send notification. " + e);
    }
  }
  
}
