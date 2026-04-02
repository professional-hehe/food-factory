package com.example.foodfactory.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendRegistrationEmail(String toEmail, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to FoodApp – Registration Successful!");
            message.setText("Hi " + name + ",\n\n"
                    + "Welcome to FoodApp! Your account has been created successfully.\n\n"
                    + "Email: " + toEmail + "\n\n"
                    + "Start exploring restaurants and place your first order!\n\n"
                    + "Regards,\nFoodApp Team");
            mailSender.send(message);
            logger.info("Registration email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send registration email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendOrderConfirmationEmail(String toEmail, String name, Long orderId, String restaurantName, String totalPrice) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Confirmed – Order #" + orderId);
            message.setText("Hi " + name + ",\n\n"
                    + "Your order has been placed successfully!\n\n"
                    + "Order ID     : #" + orderId + "\n"
                    + "Restaurant   : " + restaurantName + "\n"
                    + "Total Amount : ₹" + totalPrice + "\n"
                    + "Status       : PLACED\n\n"
                    + "We will notify you once your order is confirmed.\n\n"
                    + "Thank you for ordering with FoodApp!\n\n"
                    + "Regards,\nFoodApp Team");
            mailSender.send(message);
            logger.info("Order confirmation email sent to: {} for order #{}", toEmail, orderId);
        } catch (Exception e) {
            logger.error("Failed to send order confirmation email to {}: {}", toEmail, e.getMessage());
        }
    }

    @Async
    public void sendOrderCancellationEmail(String toEmail, String name, Long orderId, String restaurantName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Order Cancelled – Order #" + orderId);
            message.setText("Hi " + name + ",\n\n"
                    + "Your order has been cancelled as requested.\n\n"
                    + "Order ID   : #" + orderId + "\n"
                    + "Restaurant : " + restaurantName + "\n"
                    + "Status     : CANCELLED\n\n"
                    + "If this was a mistake, please place a new order.\n\n"
                    + "Regards,\nFoodApp Team");
            mailSender.send(message);
            logger.info("Order cancellation email sent to: {} for order #{}", toEmail, orderId);
        } catch (Exception e) {
            logger.error("Failed to send order cancellation email to {}: {}", toEmail, e.getMessage());
        }
    }
}
