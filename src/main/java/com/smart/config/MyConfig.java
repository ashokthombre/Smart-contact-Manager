package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class MyConfig
{
	  @Bean
	    public UserDetailsService getUserDetailsService() {
	        return new UserDetailsServiceImpl();
	    }

	    @Bean
	    public BCryptPasswordEncoder passwordEncoder() {
	        return new BCryptPasswordEncoder();
	    }
	    

	    
	    @Bean
	    public DaoAuthenticationProvider authenticationProvider() {
	        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
	         
	        authProvider.setUserDetailsService(this.getUserDetailsService());
	        authProvider.setPasswordEncoder(this.passwordEncoder());
	     
	        return authProvider;
	    }
	    @Bean
	    public AuthenticationManager authenticationManager(
	            AuthenticationConfiguration authConfig) throws Exception {
	        return authConfig.getAuthenticationManager();
	    }
	    
	    @Bean
	    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
	     
	    	http.authorizeRequests().requestMatchers("/admin/**").hasRole("ADMIN").
	    	requestMatchers("/user/**").hasRole("USER").requestMatchers("/**").
	    	permitAll().and().formLogin().
	    	loginPage("/signin")
	        .loginProcessingUrl("/signin")
	        .defaultSuccessUrl("/user/index", true)

	    	.and().csrf().disable();
	 
	        return http.build();
	    }
	
	
}