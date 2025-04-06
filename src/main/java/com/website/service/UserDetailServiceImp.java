package com.website.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.website.entities.User;
import com.website.security.CustomUserDetails;


@Service
public class UserDetailServiceImp implements UserDetailsService {

	@Autowired
	private UserService userService;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		// Fetch user details from the database using the provided username
		User user=userService.findUser(username);
		if(user==null)
		{
			throw new UsernameNotFoundException("User not found for email"+username);
		}
		
		
return new CustomUserDetails(user);
	}
	

}
