package com.ttcs.menshop.email;

public interface EmailService {
    void sendOtpEmail(String to, String otp);
    void sendOrderStatusEmail(String to, String customerName, String orderCode, String status);
    void sendSignupOtpEmail(String to, String otp);
}
