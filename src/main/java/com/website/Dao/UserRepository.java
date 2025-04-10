package com.website.Dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.website.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User,Integer>{
	
	User findByEmail(String email);

}
