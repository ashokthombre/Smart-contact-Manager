package com.smart.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;


@Controller
public class HomeController {
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;
	
	@ModelAttribute
	public void removesession(HttpSession session)
	{
		session.removeAttribute("message");
		System.out.println("session removed");
	}
	
	@RequestMapping("/")
	public String home(Model m)
	
	{
		
		m.addAttribute("title","Home-Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about/")
	public String about(Model m)
	{
		
		m.addAttribute("title","About-Smart Contact Manager");
		return "about";
	}
	@RequestMapping("/signup/")
	public String signup(Model m)
	{
		User user=new User();
		
		m.addAttribute("user",user);
		
 		m.addAttribute("title","Signup-Smart Contact Manager");
		return "signup";
	}
	
	//This handler for registerering form
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,
			@RequestParam(value="agreement",
			defaultValue = "false")boolean agreement,
			Model m,BindingResult result1,HttpSession session)
	{
		
		
		try {
//			if(result1.hasErrors())
//			{
//				
//				m.addAttribute("user",user);
//				System.out.println("ERROR"+result1.toString());
//				
//				 return"signup";
//			
//				
//			}
			
			if(!agreement )
			{
				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions.");
			}
			 
				user.setRole("ROLE_USER");
				user.setEnabled(true);
				user.setImageurl("default.png");
				user.setPassword(passwordEncoder.encode(user.getPassword()));
				
		            		
				System.out.println(agreement);
				System.out.println("USER" +user);
				
				User result=this.userRepository.save(user);
				m.addAttribute("user",new User());
				
				session.setAttribute("message", new Message("Successfully Registered !","alert-success"));
				
			
			
			
			
		} catch (Exception e) {
	     e.printStackTrace();
	     m.addAttribute("user",user);
	     session.setAttribute("message", new Message("Something went wrong !"+e.getMessage(),"alert-danger"));
	     
		}
		return"signup";
	}
	
	@GetMapping("/signin")
	public String customLogin(Model m)
	{
		
		m.addAttribute("title","Signin-Smart Contact Manager");
		
		return"login";
	}
	
	
}
