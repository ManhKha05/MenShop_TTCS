package com.ttcs.menshop.modules.otp.service;

public interface OtpService {
    void sendOtp(String email);
    void verifyOtp(String email, String otp);
}
