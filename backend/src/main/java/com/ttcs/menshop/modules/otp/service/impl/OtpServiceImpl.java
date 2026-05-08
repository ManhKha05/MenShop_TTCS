package com.ttcs.menshop.modules.otp.service.impl;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.email.EmailService;
import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.modules.otp.entity.OtpEntity;
import com.ttcs.menshop.modules.otp.repository.OtpRepository;
import com.ttcs.menshop.modules.otp.service.OtpService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public OtpServiceImpl(OtpRepository otpRepository, UserRepository userRepository, EmailService emailService) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }


    @Transactional
    @Override
    public void sendOtp(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email này không được liên kết với tài khoản nào!"));

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpired_at(LocalDateTime.now().plusMinutes(5));
        otpEntity.setUsed(false);
        otpRepository.save(otpEntity);

        emailService.sendOtpEmail(email, otp);
    }

    @Override
    public void sendSignupOtp(String email) {
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        OtpEntity otpEntity = new OtpEntity();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpired_at(LocalDateTime.now().plusMinutes(5));
        otpEntity.setUsed(false);
        otpRepository.save(otpEntity);

        emailService.sendSignupOtpEmail(email, otp);
    }

    @Transactional
    @Override
    public void verifyOtp(String email, String otp) {
        OtpEntity otpEntity = otpRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new BadRequestException("OTP không hợp lệ"));

        if (otpEntity.isUsed()) {
            throw new BadRequestException("OTP đã được sử dụng");
        }

        if (otpEntity.getExpired_at().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP đã hết hạn");
        }

        if (!otpEntity.getOtp().equals(otp)) {
            throw new BadRequestException("OTP không đúng");
        }

        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);
    }
}
