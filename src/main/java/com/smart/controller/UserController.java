package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import com.razorpay.*;
@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	 private BCryptPasswordEncoder bCryptPasswordEncoder;
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private MyOrderRepository myOrderRepository;
	
	@ModelAttribute
	public void addCommonData(Model m,Principal principal,HttpSession session)
	{
		
		session.removeAttribute("message");
		String userName=principal.getName();
        
        System.out.println("UserName:"+userName);
        
       User user= this.userRepository.getUserByUserName(userName);
        System.out.println(user);
        m.addAttribute(user);
		
	}
	
	@RequestMapping("/index")
	public String dashboard(Model m,Principal principal) {

		m.addAttribute("title","User DashBoard"); 
		return"normal/user_dashboard";
	}
	
	//open add form handler
	
	
	@GetMapping("/add-contact")
	public String openContactForm(Model m,HttpSession session)
	{ 
		
		m.addAttribute("title","Add Contact");
		m.addAttribute("contact",new Contact());
		
		return"normal/add_contact_form";
	}
	
	
	//handle save contact
	
	@PostMapping("/process-form")
	public String processContact(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file, 
			Principal principal,Model m,HttpSession session)
	{
		
		 
		
		try {
			

			String name=principal.getName();
			User user=this.userRepository.getUserByUserName(name);
			
			//Uploading file
		
			
			if(file.isEmpty())
			{
				 contact.setImage("default.png");
			}
			else
			{
				//find the file to folder and update the name to contact
                contact.setImage(file.getOriginalFilename());	
                
               File saveFile= new ClassPathResource("static/img").getFile();
               
             Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
               
               Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING);
				
			}
			
			
			
			
			contact.setUser(user);
			
			user.getContacts().add(contact);
			
			this.userRepository.save(user);
			System.out.println("Data:"+contact);
			System.out.println("added to data base");
			
			System.out.println("Image uploaded");
			//message success
			session.setAttribute("message", new Message("Your contact added Successfully!","success"));
			return"normal/user_dashboard";
			
			
		} catch (Exception e) {
		      e.printStackTrace();
		      //error message
		      session.setAttribute("message", new Message("Something went wrong!"+e.getMessage(),"danger"));
		}
		return"normal/user_dashboard";
	}
	
	//show contact handler
	//per page=5
	//current page=[page]
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page,Model m,Principal principal)
	{
		m.addAttribute("title","View Contact");
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		int id=user.getId();
		//contact list
		//currentPage-page
		//Contact Per-Page
		Pageable pageable=PageRequest.of(page, 5);
		
		Page<Contact>contact=this.contactRepository.findContactByUser(id,pageable);
		
		System.out.println(contact);
		m.addAttribute("contact",contact);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contact.getTotalPages());
		
		return"normal/show_contact";
	}
	
	
	//Showing perticular user
	
	@RequestMapping("/{cId}/contact")
	public String showContactDeatail(@PathVariable("cId") Integer cId,Model m,Principal principal)
	{
		
	
	 System.out.println("ID:"+cId);
		
	Optional<Contact> contact=	this.contactRepository.findById(cId);
		Contact contact1=contact.get();
		
		String email=principal.getName();
		   
		User user=this.userRepository.getUserByUserName(email);
		
		if(user.getId()==contact1.getUser().getId())
		{
			m.addAttribute("contact",contact1);
		    m.addAttribute("title",user.getName());
		}
		
			  
		
		
		
		
	return"normal/contact_detail";	
	}
	
	//delete contact
	@RequestMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,HttpSession session,Principal principal)
	{
		
	  Optional<Contact>c=	this.contactRepository.findById(cId);
	  Contact contact=c.get();
	  
//	      contact1.setUser(null);
	    User user=this.userRepository.getUserByUserName(principal.getName());
	     user.getContacts().remove(contact);
	  
	      this.userRepository.save(user);
		System.out.println("deleted Contact"+cId);
		session.setAttribute("message", new Message("Your contact Deleted Successfully!","success"));
		return"redirect:/user/show-contacts/0";
	}

	
	
	//update form
	@RequestMapping("/update-form/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId,Model m)
	{   
	Optional<Contact>c=this.contactRepository.findById(cId);
		Contact contact=c.get();
		System.out.println(contact);
		m.addAttribute("contact",contact);
		return"normal/update_contact_form";
	}
	
	@PostMapping("/update-contact")
	public String updateContact(@ModelAttribute("contact") Contact contact,
			@RequestParam("profileImage") MultipartFile file,Principal principal,
			HttpSession session,@RequestParam("cId") Integer cId)
	{   
		try {
			
			Optional<Contact> c=this.contactRepository.findById(cId);
			 Contact updateContact=c.get();
			  String image=updateContact.getImage();
			  Random r=new Random();
			   r.setSeed(123456789);
			  double d=r.nextDouble();
			   System.out.println(d);
			   String random=Double.toString(d);
			   String i=random.concat(image);
			  
			if(file.isEmpty())
			{ 
				
			  contact.setImage(image);
			}
			else
			{   
				 File deleteFile= new ClassPathResource("static/img").getFile();
				  File file2=new File(deleteFile,updateContact.getImage());
				  file2.delete();
				
				//find the file to folder and update the name to contact
				  String s=random+file.getOriginalFilename();
                  contact.setImage(s);	
                
               File saveFile= new ClassPathResource("static/img").getFile();
               
               Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+random+file.getOriginalFilename());
               
               Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING);
				
			}
			
			
			String email=principal.getName();
			
			User user=this.userRepository.getUserByUserName(email);
			
			 contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact Updated Successfully!","success"));
			return"normal/user_dashboard";
			
		}  
		catch (Exception e)
		{
			 session.setAttribute("message", new Message("Something went wrong!"+e.getMessage(),"danger"));
		}
		
		
		return"normal/user_dashboard";
	}
	
	//Your Profile
	
	@GetMapping("/profile")
	public String yourProfile(Model m,Principal principal)
	{    
		String email=principal.getName();
	User user=this.userRepository.getUserByUserName(email);
		m.addAttribute("user",user);
		m.addAttribute("title","profile Page");
		return"normal/profile";
	}
	
	@GetMapping("/updateProfile")
	public String updateProfile(Principal principal,Model m)
  	{
	String email=principal.getName();
	
	 User user=this.userRepository.getUserByUserName(email);
		m.addAttribute("user",user);
		
		return"normal/update_profile";
	}
	
	
	//UPdate profile
	
	@PostMapping("/update-profile")
	public String update(@ModelAttribute("user") User user,Principal principal,HttpSession session
			,@RequestParam("password1") String password,@RequestParam("profileImage")MultipartFile file)
	{
             
		try
		{
			
			
			
			   String email=principal.getName();
			 
			   String oldPassword=user.getPassword();
			   System.err.println(oldPassword);
			   
			
			   
			 
			 if(password.isBlank())
			 {
				 
				 user.setPassword(oldPassword);
				 System.err.println(user.getPassword());
				 
			 }
			 else 
			 {
				 user.setPassword(passwordEncoder.encode(password));
				 System.err.println(user.getPassword());
			 }
			 
			 
			  Random r=new Random();
			   r.setSeed(123456789);
			  double d=r.nextDouble();
			   System.out.println(d);
			   String random=Double.toString(d);
			   String i=random.concat(user.getImageurl());
			 
			 
			    if(file.isEmpty())
			    {
			    	 
			    	String s=user.getImageurl();
			    	user.setImageurl(s);
			    	System.err.println(user.getImageurl());
			    	
			    }
			    else
			    {
			    	
                      //find the file to folder and update the name to contact
					  String s=random+file.getOriginalFilename();
					  System.err.println(s);
	                  user.setImageurl(s);	
	                
	               File saveFile= new ClassPathResource("static/img").getFile();
	               
	               Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+random+file.getOriginalFilename());
	               
	               Files.copy(file.getInputStream(),path ,StandardCopyOption.REPLACE_EXISTING);
			    	
			    	
			    }
			 
			
			 this.userRepository.save(user);
				
				session.setAttribute("message", new Message("Your Profile Updated Successfully!","success"));		
				return"normal/user_dashboard";
				
		
			
			
		
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		return"/home";
	}
	
	@GetMapping("/deleteProfile")
	public String deleteProfile(Principal principal)
	{  
		String name=principal.getName();
	User user=this.userRepository.getUserByUserName(name);
	    this.userRepository.delete(user);
		
		return"/home";
	}
	
	@RequestMapping("/setting")
	public String oprnSetting()
	{
		
		
		return "normal/setting";
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session)
	{  
		String email=principal.getName();
		
		User currentUser=this.userRepository.getUserByUserName(email);
		 System.out.println(currentUser.getPassword());
		 String encodePassword=passwordEncoder.encode(oldPassword);
		System.err.println("OLDPASSWORD:"+encodePassword);
		System.err.println("NEWPASSWORD:"+newPassword);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your Password Updated Successfully!","success"));
		}
		else
		{
			session.setAttribute("message", new Message("Please enter correct old password !","danger"));
			return "normal/setting";
		}
		
		return"normal/user_dashboard";
	}
	
	//Payment Gateway
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data,Principal principal ) throws RazorpayException
	{
		System.err.println(data);
		System.err.println("HEY OREDR IS RECIEVED");
		
	  int amt=Integer.parseInt(data.get("amount").toString());	
	  
	  RazorpayClient client= new RazorpayClient("rzp_test_9KZkqRrN8EycDe","fw66vmqrC97mDK0GnFYMP3he");
	  
	  JSONObject ob=new JSONObject();
	  ob.put("amount",amt*100);
	  ob.put("currency","INR");
	  ob.put("receipt","txn_12345");
	  
	  Order order=client.orders.create(ob);
	  System.err.println(order);
	  
	  //save order in database
	  
	  MyOrder myOrder=new MyOrder();
	  
	  myOrder.setAmount(order.get("amount")+"");
	  myOrder.setOrderId(order.get("id"));
	  myOrder.setPaymentId(null);
	  myOrder.setStatus("created");
	  myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
	  myOrder.setReceipt(order.get("receipt"));
	  
	 this.myOrderRepository.save(myOrder); 
	  
	  
	  
	  
		
		return order.toString();
	}
	
	@PostMapping("/update_order")

	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object>data )
	{
		
	MyOrder myorder=this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		  
	 myorder.setPaymentId(data.get("payment_id").toString());
	 myorder.setStatus(data.get("status").toString());

	   this.myOrderRepository.save(myorder);
		
		
		
		System.err.println(data);
		
		
        return ResponseEntity.ok(Map.of("msg","Updated"));
	}
	
	
	
	
	
	


}
