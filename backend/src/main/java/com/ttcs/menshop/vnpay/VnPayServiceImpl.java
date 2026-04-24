package com.ttcs.menshop.vnpay;

import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.order.repository.OrderRepository;
import com.ttcs.menshop.modules.order_item.entity.OrderItemEntity;
import com.ttcs.menshop.modules.order_item.repository.OrderItemRepository;
import com.ttcs.menshop.modules.order_status.entity.OrderStatusEntity;
import com.ttcs.menshop.modules.order_status.repository.OrderStatusRepository;
import com.ttcs.menshop.modules.payment_group.entity.PaymentGroupEntity;
import com.ttcs.menshop.modules.payment_group.repository.PaymentGroupRepository;
import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import com.ttcs.menshop.modules.product_variant.repository.ProductVariantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VnPayServiceImpl implements VnPayService {

    private final VnPayConfig vnPayConfig;
    private final OrderRepository orderRepository;
    private final PaymentGroupRepository paymentGroupRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;

    public VnPayServiceImpl(VnPayConfig vnPayConfig, OrderRepository orderRepository, PaymentGroupRepository paymentGroupRepository, OrderStatusRepository orderStatusRepository, OrderItemRepository orderItemRepository, ProductVariantRepository productVariantRepository) {
        this.vnPayConfig = vnPayConfig;
        this.orderRepository = orderRepository;
        this.paymentGroupRepository = paymentGroupRepository;
        this.orderStatusRepository = orderStatusRepository;
        this.orderItemRepository = orderItemRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Override
    public String createPaymentUrl(PaymentGroupEntity payment, HttpServletRequest request) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        String createDate = formatter.format(calendar.getTime());
        calendar.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(calendar.getTime());

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        params.put("vnp_Amount", payment.getTotalAmount().multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getCode());
        params.put("vnp_OrderInfo", "Thanh toan hoa don " + payment.getCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        params.put("vnp_IpAddr", VnPayUtil.getClientIp(request));
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        String hashData = VnPayUtil.buildHashData(params);
        String secureHash = VnPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), hashData);
        String query = VnPayUtil.buildQuery(params);

        return vnPayConfig.getPayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public VnPayResponse handleReturn(Map<String, String> params) {
        String secureHash = params.get("vnp_SecureHash");
        if (secureHash == null || secureHash.isBlank()) {
            return VnPayResponse.builder()
                    .success(false)
                    .verified(false)
                    .message("Thiếu chữ ký VNPay")
                    .build();
        }

        Map<String, String> fields = new HashMap<>(params);
        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String signData = VnPayUtil.buildHashData(fields);
        String calculatedHash = VnPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), signData);
        boolean verified = calculatedHash.equalsIgnoreCase(secureHash);

        String paymentCode = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String transactionNo = params.get("vnp_TransactionNo");

        PaymentGroupEntity paymentGroup = paymentGroupRepository.findByCode(paymentCode).orElse(null);

        if (!verified) {
            return VnPayResponse.builder()
                    .success(false)
                    .verified(false)
                    .message("Sai chữ ký VNPay")
                    .orderCode(paymentCode)
                    .responseCode(responseCode)
                    .transactionStatus(transactionStatus)
                    .transactionNo(params.get("vnp_TransactionNo"))
                    .bankCode(params.get("vnp_BankCode"))
                    .payDate(params.get("vnp_PayDate"))
                    .build();
        }

        boolean success = "00".equals(responseCode) && "00".equals(transactionStatus);

        if(paymentGroup != null && !"PAID".equalsIgnoreCase(paymentGroup.getStatus())) {
            List<OrderEntity> orders = orderRepository.findByPaymentGroupId(paymentGroup.getId());

            if (success) {
                paymentGroup.setStatus("PAID");
                paymentGroup.setTransactionCode(transactionNo);
                paymentGroup.setPaidAt(LocalDateTime.now());

                for (OrderEntity order : orders) {
                    order.setPaymentStatus("PAID");
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                }
            } else {
                paymentGroup.setStatus("FAILED");
                paymentGroup.setTransactionCode(transactionNo);

                for (OrderEntity order : orders) {
                    order.setStatus("CANCELLED");
                    order.setPaymentStatus("FAILED");
                    order.setUpdatedAt(LocalDateTime.now());

                    List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(order.getId());
                    for (OrderItemEntity item : orderItems) {
                        ProductVariantEntity variant = item.getVariant();
                        variant.setStock(variant.getStock() + item.getQuantity());
                        productVariantRepository.save(variant);
                    }

                    OrderStatusEntity history = new OrderStatusEntity();
                    history.setOrder(order);
                    history.setStatus("CANCELLED");
                    history.setNote("Khách hàng hủy hoặc thanh toán thất bại");
                    orderStatusRepository.save(history);

                    orderRepository.save(order);
                }
            }

            paymentGroupRepository.save(paymentGroup);
        }

        return VnPayResponse.builder()
                .success(success)
                .verified(true)
                .message(success ? "Thanh toán thành công" : "Thanh toán thất bại")
                .orderCode(paymentCode)
                .finalTotal(paymentGroup.getTotalAmount())
                .responseCode(responseCode)
                .transactionStatus(transactionStatus)
                .transactionNo(transactionNo)
                .bankCode(params.get("vnp_BankCode"))
                .payDate(params.get("vnp_PayDate"))
                .build();
    }

    @Override
    @Transactional
    public Map<String, String> handleIpn(Map<String, String> params) {
        Map<String, String> response = new HashMap<>();

        try {
            String secureHash = params.get("vnp_SecureHash");
            if (secureHash == null || secureHash.isBlank()) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
                return response;
            }

            Map<String, String> fields = new HashMap<>(params);
            fields.remove("vnp_SecureHash");
            fields.remove("vnp_SecureHashType");

            String signData = VnPayUtil.buildHashData(fields);
            String calculatedHash = VnPayUtil.hmacSHA512(vnPayConfig.getHashSecret(), signData);

            if (!calculatedHash.equalsIgnoreCase(secureHash)) {
                response.put("RspCode", "97");
                response.put("Message", "Invalid Checksum");
                return response;
            }

            String paymentGroupCode = params.get("vnp_TxnRef");
            String responseCode = params.get("vnp_ResponseCode");
            String transactionStatus = params.get("vnp_TransactionStatus");
            String transactionNo = params.get("vnp_TransactionNo");
            String amountRaw = params.get("vnp_Amount");

            PaymentGroupEntity paymentGroup = paymentGroupRepository.findByCode(paymentGroupCode)
                    .orElse(null);

            if (paymentGroup == null) {
                response.put("RspCode", "01");
                response.put("Message", "Payment group not found");
                return response;
            }

            List<OrderEntity> orders = orderRepository.findByPaymentGroupId(paymentGroup.getId());

            long amountFromVnPay = Long.parseLong(amountRaw) / 100L;
            long expectedAmount = paymentGroup.getTotalAmount().longValue();

            if (amountFromVnPay != expectedAmount) {
                response.put("RspCode", "04");
                response.put("Message", "Invalid Amount");
                return response;
            }

            if ("PAID".equalsIgnoreCase(paymentGroup.getStatus())) {
                response.put("RspCode", "02");
                response.put("Message", "Payment group already confirmed");
                return response;
            }

            boolean success = "00".equals(responseCode) && "00".equals(transactionStatus);

            if (success) {
                paymentGroup.setStatus("PAID");
                paymentGroup.setTransactionCode(transactionNo);
                paymentGroup.setPaidAt(LocalDateTime.now());

                for (OrderEntity order : orders) {
                    order.setPaymentStatus("PAID");
                    order.setUpdatedAt(LocalDateTime.now());
                    orderRepository.save(order);
                }
            } else {
                paymentGroup.setStatus("FAILED");
                paymentGroup.setTransactionCode(transactionNo);

                for (OrderEntity order : orders) {
                    order.setStatus("CANCELLED");
                    order.setPaymentStatus("FAILED");
                    order.setUpdatedAt(LocalDateTime.now());

                    List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(order.getId());
                    for (OrderItemEntity item : orderItems) {
                        ProductVariantEntity variant = item.getVariant();
                        variant.setStock(variant.getStock() + item.getQuantity());
                        productVariantRepository.save(variant);
                    }

                    OrderStatusEntity history = new OrderStatusEntity();
                    history.setOrder(order);
                    history.setStatus("CANCELLED");
                    history.setNote("Thanh toán thất bại");
                    orderStatusRepository.save(history);

                    orderRepository.save(order);
                }
            }

            paymentGroupRepository.save(paymentGroup);

            response.put("RspCode", "00");
            response.put("Message", "Confirm Success");
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            response.put("RspCode", "99");
            response.put("Message", "Unknown error");
            return response;
        }
    }
}
