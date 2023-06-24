package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.services.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgetPassword {
	@Autowired
	 private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private EmailService emailService;
	
    @GetMapping("forget-password")
	public String forget() {
    	
		System.out.println("password forgeted");
		
		
		return"/forget";
	}
    @PostMapping("/change-password")
    public String changePassword(@RequestParam("userEmail") String userEmail,HttpSession session)
    {
    	System.out.println("Email:"+userEmail);
    	Random rand=new Random();
    	int otp=0;
    	User user=this.userRepository.getUserByUserName(userEmail);
    	if(user !=null)
    	{
    		 otp = rand.nextInt((9999 - 100) + 1) + 10;
      	   System.out.println(otp);
      	   String sentOtp=Integer.toString(otp);
      	   
      	   
      	   session.setAttribute("otp", sentOtp);
      	 session.setAttribute("emailotp", user.getEmail());
      	   
      	   
//      	   sending email
//      	   
//      	 boolean f=this.emailService.sendEmail("Verify OTP",sentOtp, userEmail);
//      	  if(f)
//      	  {
//      		session.setAttribute("message", new Message("OTP send successfully","success"));
//      	  
//        	return"/verify_otp"; 
//      	  }
//      	  else
//      	  {
//      		System.err.println("Enater Valid Email");
//			session.setAttribute("message", new Message("Enter Valid Email","danger"));
//			return"/forget";
//      	  }
//      	   
//      	   
//      	   
//      	   
      	   
      	   
      	
    	}
    	else {
    		
    		    System.err.println("Enater Valid Email");
    			session.setAttribute("message", new Message("Enter Valid Email","danger"));
    			return"/forget";
    		
		}
    	session.setAttribute("message", new Message("OTP send successfully","success"));
    	  
    	return"/verify_otp";
    }
     
    
      @PostMapping("/verify-otp")
      public String verify(@RequestParam("otp") String otp,HttpSession session)
      {
    	 System.err.println(otp);
    	 
    	 String sentOTP= (String) session.getAttribute("otp");
    	 System.out.println("Session Otp:"+sentOTP);
    	 
    	 if(otp.equals(sentOTP))
    	 {
    		 
    		 return"change_password"; 
    		 
    	 }
    	 else
    	 {
    		 session.setAttribute("message", new Message("Enater Valid OTP","danger"));
    		  return"verify_otp"; 
    	 }
    	  
    	  
    	
    	  
      }
      
      @PostMapping("/newPassword")
      public String newPassword(@RequestParam("newPassword") String newPassword,HttpSession session) {
    	  
    	 try {
    		 
    		 System.out.println(newPassword);
       	  String email=(String)session.getAttribute("emailotp");
       	  System.out.println(email);
       	  
       	    User user= this.userRepository.getUserByUserName(email);
       	 
       	 user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
       	  this.userRepository.save(user);
       	  session.setAttribute("message", new Message("Password Upadated..","success"));
			
		} 
    	 catch (Exception e)
    	 {
		       e.printStackTrace();
		       session.setAttribute("message", new Message("Password Not Upadated..","danger"));
		}
    	  
    	  return"/login";
      }
	
}
