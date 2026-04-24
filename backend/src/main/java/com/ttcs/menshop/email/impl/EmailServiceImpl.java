package com.ttcs.menshop.email.impl;

import com.ttcs.menshop.email.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.username}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác nhận OTP");
        message.setText("Mã OTP của bạn là: " + otp + ". Có hiệu lực trong 5 phút.");

        mailSender.send(message);
    }

    public void sendOrderStatusEmail(String to, String customerName, String orderCode, String status) {
        String subject = "Cập nhật trạng thái đơn hàng " + orderCode;
        String content = buildOrderStatusContent(customerName, orderCode, status);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        mailSender.send(message);
    }

    private String buildOrderStatusContent(String customerName, String orderCode, String status) {
        return switch (status) {
            case "CONFIRMED" -> """
                Xin chào %s,

                Đơn hàng %s của bạn đã được shop xác nhận.

                Cảm ơn bạn đã mua sắm.
                """.formatted(customerName, orderCode);

            case "DELIVERING" -> """
                Xin chào %s,

                Đơn hàng %s của bạn đang được giao.

                Vui lòng để ý điện thoại để nhận hàng.
                """.formatted(customerName, orderCode);

            case "DELIVERED" -> """
                Xin chào %s,

                Đơn hàng %s của bạn đã được giao thành công.

                Cảm ơn bạn đã tin tưởng mua sắm.
                """.formatted(customerName, orderCode);

            case "COMPLETED" -> """
                Xin chào %s,

                Đơn hàng %s của bạn đã hoàn thành.

                Cảm ơn bạn đã mua sắm.
                """.formatted(customerName, orderCode);


            case "CANCELLED" -> """
                Xin chào %s,

                Đơn hàng %s của bạn đã bị hủy.

                Nếu cần hỗ trợ, vui lòng liên hệ shop.
                """.formatted(customerName, orderCode);

            default -> """
                Xin chào %s,

                Đơn hàng %s của bạn vừa được cập nhật trạng thái: %s.
                """.formatted(customerName, orderCode, status);
        };
    }
}
