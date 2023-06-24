package com.smart.services;


import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;

import javax.mail.internet.MimeMessage;


import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	
	 public boolean sendEmail(String subject, String message, String to) {

	        boolean f=false;
	        String from="ashok.marketyard@gmail.com";
	        //Veriable for gmail

	        String host="smtp.gmail.com";

	        //Get the System properties

	        Properties properties=System.getProperties();

	        System.out.println("PROPERTIES:"+properties);


	        //setting importent information to properties object

	        //host set

	        properties.put("mail.smtp.host", host);
	        properties.put("mail.smtp.port", "465");

	        properties.put("mail.smtp.ssl.enable","true");

	        properties.put("mail.smtp.auth","true");

	        //step:1 Get session object

	        Session session=Session.getInstance(properties, new Authenticator() {

	            @Override
	            protected PasswordAuthentication getPasswordAuthentication() {
	                // TODO Auto-generated method stub
	                return new PasswordAuthentication("ashok.marketyard@gmail.com","1995");


	            }


	        });

	        session.setDebug(true);
	        //Step:2 compose message[text,multimedia]

	        MimeMessage m=new MimeMessage(session);

	        try {
	            //from email
	            m.setFrom(from);

	            //recipient
	            m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

	            //add Subject

	            m.setSubject(subject);

	            //add text to message

	            m.setText(message);

	            //send
	            //send 3:send the message using Transport class

	            Transport.send(m);

	            System.out.println("message Send Successfuly!");
	            f=true;

	        } catch (MessagingException e) {

	            e.printStackTrace();
	        }


	     return f;
	    }

}
