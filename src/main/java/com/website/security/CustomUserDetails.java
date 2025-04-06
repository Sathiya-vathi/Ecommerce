package com.website.security;


import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.website.entities.Role;
import com.website.entities.User;

public class CustomUserDetails implements UserDetails {

    private User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRole()
                .stream()
                .map(Role::getName) // e.g., "ROLE_USER"
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // or add logic based on user fields
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // or add logic
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // or add logic
    }

    @Override
    public boolean isEnabled() {
        return true; // or use a user.isEnabled() field
    }

    public User getUser() {
        return user;
    }
}

