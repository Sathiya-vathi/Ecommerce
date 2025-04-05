package com.website.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        	
            .csrf(csrf -> csrf.disable())  // Disable CSRF for login to work
            .authorizeHttpRequests(auth -> auth
            	//User
                .requestMatchers("/api/users/register", "/api/users/login").permitAll()
                .requestMatchers("/api/users/all").hasRole("ADMIN")
                //Product
                .requestMatchers("/api/products/add","/api/products/update/{id}","/api/products/delete/{id}").hasRole("ADMIN")
                .requestMatchers("/api/products/**").permitAll() // Anyone can view products
                //Category
                .requestMatchers("/api/categories/add","/api/categories/delete/{id}","/api/categories/update/{id}").hasRole("ADMIN")
                .requestMatchers("/api/categories/**").permitAll()
                //Cart
                .requestMatchers("/api/cart/add", "/api/cart/remove", "/api/cart/clear","/api/cart/items").authenticated()
                //Order
                .requestMatchers("/orders/all","/orders/update-status/{orderId}","/orders/filter-by-price").hasRole("ADMIN") // Only admin can update order status
                .requestMatchers("/orders/create-payment","/orders/place","/orders/{orderId}","/orders/my-orders","/orders/invoice/{orderId}","/orders/cancel/{orderId}").hasRole("USER") // Only logged-in users can place orders
                //Address
                .requestMatchers("/api/addresses/save").authenticated()
                //Review
                .requestMatchers("/api/reviews/add").authenticated() // Only logged-in users can add reviews
                .requestMatchers("/api/reviews/{productId}").permitAll() // Anyone can view reviews
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}


