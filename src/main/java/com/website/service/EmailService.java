package com.website.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOrderStatusEmail(String toEmail, Long orderId, String status) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Order Status Updated - Order ID: " + orderId);
        message.setText("Hi there,\n\nYour order with ID " + orderId + " has been updated to: " + status + ".\n\nThank you for shopping with us!");

        mailSender.send(message);
    }
}


