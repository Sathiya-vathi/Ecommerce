package com.website.Dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.website.entities.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {
	Optional<Role> findByName(String name); 

}
