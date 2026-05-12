package com.ttcs.menshop.modules.order.service.impl;

import com.ttcs.menshop.auth.entity.UserEntity;
import com.ttcs.menshop.auth.repository.UserRepository;
import com.ttcs.menshop.auth.service.AuthService;
import com.ttcs.menshop.email.EmailService;
import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.address.entity.AddressEntity;
import com.ttcs.menshop.modules.address.repository.AddressRepository;
import com.ttcs.menshop.modules.cart_item.repository.CartItemRepository;
import com.ttcs.menshop.modules.ghn.dto.response.GhnCreateOrderResponse;
import com.ttcs.menshop.modules.ghn.service.GhnService;
import com.ttcs.menshop.modules.notification.service.NotificationService;
import com.ttcs.menshop.modules.order.dto.PreparedOrderItem;
import com.ttcs.menshop.modules.order.dto.request.OrderCreateRequest;
import com.ttcs.menshop.modules.order.dto.request.OrderItemRequest;
import com.ttcs.menshop.modules.order.dto.response.*;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.order.repository.OrderRepository;
import com.ttcs.menshop.modules.order.service.OrderService;
import com.ttcs.menshop.modules.order_item.dto.response.OrderItemDetailResponse;
import com.ttcs.menshop.modules.order_item.entity.OrderItemEntity;
import com.ttcs.menshop.modules.order_item.repository.OrderItemRepository;
import com.ttcs.menshop.modules.order_status.dto.response.OrderStatusHistoryResponse;
import com.ttcs.menshop.modules.order_status.entity.OrderStatusEntity;
import com.ttcs.menshop.modules.order_status.repository.OrderStatusRepository;
import com.ttcs.menshop.modules.payment_group.entity.PaymentGroupEntity;
import com.ttcs.menshop.modules.payment_group.repository.PaymentGroupRepository;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.product.entity.UserInteractionEntity;
import com.ttcs.menshop.modules.product.enums.InteractionType;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.product.repository.UserInteractionRepository;
import com.ttcs.menshop.modules.product_image.entity.ProductImageEntity;
import com.ttcs.menshop.modules.product_variant.entity.ProductVariantEntity;
import com.ttcs.menshop.modules.product_variant.repository.ProductVariantRepository;
import com.ttcs.menshop.modules.sale_product.repository.SaleProductRepository;
import com.ttcs.menshop.modules.shipping.dto.request.ShopShippingRequest;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import com.ttcs.menshop.notification.shop.service.ShopNotificationService;
import com.ttcs.menshop.vnpay.VnPayService;
import com.ttcs.menshop.websocket.dto.OrderStatusEvent;
import com.ttcs.menshop.websocket.dto.ShopOrderEvent;
import com.ttcs.menshop.websocket.service.WebSocketService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final PaymentGroupRepository paymentGroupRepository;
    private final VnPayService vnPayService;
    private final SaleProductRepository saleProductRepository;
    private final ShopRepository shopRepository;
    private final AuthService authService;
    private final WebSocketService webSocketService;
    private final ProductRepository productRepository;
    private final ShopNotificationService shopNotificationService;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final UserInteractionRepository userInteractionRepository;
    private final GhnService ghnService;


    @Transactional
    @Override
    public OrderCreateResponse createOrder(OrderCreateRequest request, HttpServletRequest httpRequest) {
        UserEntity user = authService.getCurrentUser();

        validateRequest(request);

        AddressEntity address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new BadRequestException("Địa chỉ không tồn tại"));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("Bạn không có quyền sử dụng địa chỉ này");
        }

        Map<Integer, List<PreparedOrderItem>> itemsByShop = new LinkedHashMap<>();

        for (OrderItemRequest item : request.getItems()) {
            ProductVariantEntity variant = productVariantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new BadRequestException("Variant không tồn tại"));;

            if(variant.getProduct().getShop().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("Bạn không thể mua sản phẩm của chính shop mình");
            }

            if (variant.getStock() == null || variant.getStock() <= 0) {
                throw new BadRequestException("Sản phẩm " + variant.getProduct().getName() + " đã hết hàng");
            }

            if (item.getQuantity() > variant.getStock()) {
                throw new BadRequestException(
                        "Sản phẩm " + variant.getProduct().getName() + " chỉ còn " + variant.getStock() + " trong kho"
                );
            }

            ProductEntity product = variant.getProduct();
            product.setSoldCount(product.getSoldCount() + item.getQuantity());
            productRepository.save(product);

            BigDecimal flashSalePrice = saleProductRepository.findActiveByProductId(product.getId())
                    .map(s -> s.getSalePrice())
                    .orElse(null);

            BigDecimal unitPrice = flashSalePrice != null ? flashSalePrice :
                    (product.getSalePrice() != null ? product.getSalePrice() : product.getPrice())  ;

            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

            Integer shopId = product.getShop().getId();
            PreparedOrderItem prepared = new PreparedOrderItem();
            prepared.setCartItemId(item.getCartItemId());
            prepared.setVariant(variant);
            prepared.setUnitPrice(unitPrice);
            prepared.setQuantity(item.getQuantity());
            prepared.setSubtotal(subtotal);

            itemsByShop.computeIfAbsent(shopId, k -> new ArrayList<>()).add(prepared);
        }

        Map<Integer, ShopShippingRequest> shippingByShop =
                request.getShippingFees()
                        .stream()
                        .collect(Collectors.toMap(
                                ShopShippingRequest::getShopId,
                                item -> item
                        ));


        BigDecimal grandTotal = BigDecimal.ZERO;

        for (List<PreparedOrderItem> shopItems : itemsByShop.values()) {
            BigDecimal totalPrice = BigDecimal.ZERO;
            for (PreparedOrderItem item : shopItems) {
                totalPrice = totalPrice.add(item.getSubtotal());
            }

            ShopShippingRequest shipping = shippingByShop.get(shopItems.get(0)
                    .getVariant()
                    .getProduct()
                    .getShop()
                    .getId());

            BigDecimal shippingFee = shipping.getFee();
            BigDecimal finalTotal = totalPrice.add(shippingFee);
            grandTotal = grandTotal.add(finalTotal);
        }

        PaymentGroupEntity paymentGroup = new PaymentGroupEntity();
        paymentGroup.setCode(generateCode("HD"));
        paymentGroup.setUser(user);
        paymentGroup.setMethod(request.getPaymentMethod().toUpperCase());
        paymentGroup.setTotalAmount(grandTotal);
        paymentGroup.setStatus("PENDING");
        paymentGroupRepository.save(paymentGroup);

        List<OrderEntity> createdOrders = new ArrayList<>();
        Map<Integer, OrderEntity> ordersByShop = new LinkedHashMap<>();

        for (Map.Entry<Integer, List<PreparedOrderItem>> entry : itemsByShop.entrySet()) {
            int shopId = entry.getKey();
            List<PreparedOrderItem> shopItems = entry.getValue();

            ShopEntity shop = shopRepository.findById(shopId).orElse(null);

            BigDecimal totalPrice = BigDecimal.ZERO;
            for (PreparedOrderItem item : shopItems) {
                totalPrice = totalPrice.add(item.getSubtotal());
            }

            ShopShippingRequest shipping = shippingByShop.get(shopId);

            BigDecimal finalTotal = totalPrice.add(shipping.getFee());

            OrderEntity order = new OrderEntity();
            order.setCode(generateCode("DH"));
            order.setUser(user);
            order.setShop(shop);
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getPhone());
            order.setAddress(address.getAddress());
            order.setReceiverDistrictId(address.getDistrictId());
            order.setReceiverWardCode(address.getWardId());
            order.setPaymentMethod(request.getPaymentMethod().toUpperCase());
            order.setPaymentStatus("UNPAID");
            order.setNote(request.getNote());
            order.setPaymentGroup(paymentGroup);
            order.setStatus("PENDING");
            order.setShippingStatus(null);
            order.setUpdatedAt(LocalDateTime.now());
            order.setTotalPrice(totalPrice);
            order.setFinalTotal(finalTotal);

            order.setShippingServiceId(shipping.getShippingServiceId());
            order.setShippingServiceName(shipping.getShippingServiceName());
            order.setShippingFee(shipping.getFee());

            orderRepository.save(order);

            List<OrderItemEntity> orderItems = new ArrayList<>();
            List<UserInteractionEntity> interactions = new ArrayList<>();
            for (PreparedOrderItem item : shopItems) {
                OrderItemEntity orderItem = new OrderItemEntity();
                orderItem.setOrder(order);
                orderItem.setShop(item.getVariant().getProduct().getShop());
                orderItem.setVariant(item.getVariant());
                orderItem.setUnitPrice(item.getUnitPrice());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setSubtotal(item.getSubtotal());

                orderItems.add(orderItem);

                UserInteractionEntity userInteractionEntity = UserInteractionEntity.builder()
                        .user(user)
                        .product(item.getVariant().getProduct())
                        .interactionType(InteractionType.PURCHASE)
                        .weightScore(BigDecimal.valueOf(5.0).multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build();

                interactions.add(userInteractionEntity);

                ProductVariantEntity variant = item.getVariant();
                variant.setStock(variant.getStock() - item.getQuantity());
                productVariantRepository.save(variant);

                if (item.getCartItemId() != null) {
                    cartItemRepository.deleteById(item.getCartItemId());
                }
            }

            orderItemRepository.saveAll(orderItems);
            userInteractionRepository.saveAll(interactions);

            saveHistory(
                    order,
                    "PENDING",
                    "Đơn hàng đã được tạo"
            );

            createdOrders.add(order);
            webSocketService.sendToTopic(
                    "/topic/shop-orders/" + shopId,
                    new ShopOrderEvent(
                            "ORDER_CREATED",
                            shopId
                            )
            );

            notificationService.createAndSend(
                    order.getShop().getUser(),
                    order,
                    "NEW_ORDER",
                    "Đơn hàng mới",
                    "Bạn có đơn hàng mới " + order.getCode()
            );
        }

        if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            return OrderCreateResponse.builder()
                    .orderIds(createdOrders.stream().map(OrderEntity::getId).toList())
                    .orderCodes(createdOrders.stream().map(OrderEntity::getCode).toList())
                    .paymentMethod(request.getPaymentMethod().toUpperCase())
                    .paymentUrl(null)
                    .finalTotal(grandTotal)
                    .message("Đặt hàng COD thành công")
                    .build();
        }

        String paymentUrl = vnPayService.createPaymentUrl(paymentGroup, httpRequest);

        return OrderCreateResponse.builder()
                .orderIds(createdOrders.stream().map(OrderEntity::getId).toList())
                .orderCodes(createdOrders.stream().map(OrderEntity::getCode).toList())
                .paymentMethod(request.getPaymentMethod().toUpperCase())
                .paymentUrl(paymentUrl)
                .finalTotal(grandTotal)
                .message("Tạo link thanh toán VNPay thành công")
                .build();

    }

    @Override
    public OrderDetailResponse getOrderDetail(Integer id) {
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy đơn hàng id:" + id));

        UserEntity currentUser = authService.getCurrentUser();

        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> "ADMIN".equals(role.getName()));

        boolean isCustomerOwner = order.getUser() != null
                && order.getUser().getId().equals(currentUser.getId());

        boolean isShopOwner = order.getOrderItems().stream()
                .anyMatch(item ->
                        item.getShop() != null
                                && item.getShop().getUser() != null
                                && item.getShop().getUser().getId().equals(currentUser.getId())
                );

        if (!isAdmin && !isCustomerOwner && !isShopOwner) {
            throw new BadRequestException("Bạn không được truy cập đơn hàng này");
        }

        OrderDetailResponse res = new OrderDetailResponse();
        res.setId(order.getId());
        res.setOrderCode(order.getCode());
        res.setReceiverName(order.getReceiverName());
        res.setReceiverPhone(order.getReceiverPhone());
        res.setAddress(order.getAddress());
        res.setNote(order.getNote());
        res.setTotalPrice(order.getTotalPrice());
        res.setShippingFee(order.getShippingFee());
        res.setFinalPrice(order.getFinalTotal());
        res.setStatus(order.getStatus());
        res.setCreatedAt(order.getCreatedAt());
        res.setPaymentMethod(order.getPaymentMethod());
        res.setPaymentStatus(order.getPaymentStatus());
        res.setPaidAt(order.getPaymentGroup().getPaidAt());
        res.setShippingServiceName(order.getShippingServiceName());
        res.setShippingStatus(order.getShippingStatus());
        res.setGhnOrderCode(order.getGhnOrderCode());
        res.setExpectedDeliveryTime(order.getExpectedDeliveryTime());

        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(id);
        List<OrderItemDetailResponse> orderItemDetails = orderItems.stream()
                .map(it -> {
                    OrderItemDetailResponse item = new OrderItemDetailResponse();
                    item.setId(it.getId());
                    item.setProductName(it.getVariant().getProduct().getName());

                    List<ProductImageEntity> images = it.getVariant().getProduct().getImages();
                    String image = images.size() > 0 ? images.get(0).getImageUrl() : null;

                    item.setImage(image);
                    item.setSize(it.getVariant().getSize());
                    item.setColor(it.getVariant().getColor());
                    item.setPrice(it.getUnitPrice());
                    item.setQuantity(it.getQuantity());
                    item.setSubtotal(it.getSubtotal());
                    return item;
                })
                .toList();

        List<OrderStatusEntity> orderStatusEntities = orderStatusRepository.findAllByOrderId(id);
        List<OrderStatusHistoryResponse> statusHistories = orderStatusEntities.stream()
                .map(s -> {
                    OrderStatusHistoryResponse status = new OrderStatusHistoryResponse();
                    status.setStatus(s.getStatus());

                    status.setTitle(mapStatusTitle(s.getStatus()));
                    status.setDescription(s.getNote());

                    status.setCreatedAt(s.getCreatedAt());
                    return status;
                })
                .toList();

        res.setOrderDetails(orderItemDetails);
        res.setOrderStatusHistories(statusHistories);
        return res;
    }

    @Transactional
    @Override
    public void cancelOrder(Integer id) {

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Không tìm thấy đơn hàng id:" + id));

        if(order.getStatus().equals("COMPLETED")) {
            throw new BadRequestException("Không thể hủy đơn đã hoàn tất");
        }

        if(order.getStatus().equals("CANCELLED")) {
            throw new BadRequestException("Đơn hàng đã bị hủy");
        }

        order.setStatus("CANCELLED");

        order.setUpdatedAt(LocalDateTime.now());

        if(order.getPaymentStatus().equals("PAID")) {
            order.setPaymentStatus("REFUNDED");
        }

        restoreStock(order);

        orderRepository.save(order);

        saveHistory(
                order,
                "CANCELLED",
                "Đơn hàng đã được hủy"
        );

        webSocketService.sendToTopic(
                "/topic/shop-orders/" + order.getShop().getId(),
                new ShopOrderEvent(
                        "ORDER_CANCELLED",
                        order.getShop().getId()
                )
        );

        webSocketService.sendToTopic(
                "/topic/orders/" + order.getId(),
                new OrderStatusEvent(
                        "ORDER_UPDATED_STATUS",
                        order.getId()
                )
        );
    }

    @Override
    public List<OrderHistoryResponse> getMyOrders(String status, String keyword) {
        UserEntity user = authService.getCurrentUser();

        List<OrderEntity> orderEntities = orderRepository.findMyOrders(user.getId(), status, keyword);

        return orderEntities.stream()
                .map(o -> {
                    OrderHistoryResponse res = new OrderHistoryResponse();
                    res.setId(o.getId());
                    res.setOrderCode(o.getCode());
                    res.setStatus(o.getStatus());
                    res.setCreatedAt(o.getCreatedAt());
                    res.setTotalPrice(o.getFinalTotal());
                    res.setShopId(o.getShop().getId());
                    res.setShopName(o.getShop().getName());

                    List<OrderHistoryItemResponse> items = o.getOrderItems().stream().map(detail -> {
                        OrderHistoryItemResponse item = new OrderHistoryItemResponse();
                        item.setId(detail.getId());
                        item.setProductId(detail.getVariant().getProduct().getId());
                        item.setProductName(detail.getVariant().getProduct().getName());
                        String image = detail.getVariant().getProduct().getImages().get(0).getImageUrl();
                        item.setImage(image);
                        item.setColor(detail.getVariant().getColor());
                        item.setSize(detail.getVariant().getSize());
                        item.setQuantity(detail.getQuantity());
                        item.setPrice(detail.getSubtotal());
                        return item;
                    }).toList();

                    res.setItems(items);
                    return res;
                })
                .toList();
    }

    @Override
    public OrderStatsResponse getOrderStats(Integer shopId) {
        return orderRepository.getOrderStats(shopId);
    }

    @Override
    public Page<ShopOrderResponse> getShopOrders(int page, int size, String code, String status, String shippingStatus, String paymentMethod, LocalDateTime fromDate, LocalDateTime toDate) {
        UserEntity user = authService.getCurrentUser();

        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findShopOrders(user.getShop().getId(), code, status, shippingStatus, paymentMethod, fromDate, toDate, pageable);
    }

    @Transactional
    @Override
    public void updateOrderStatus(Integer orderId, String status) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new NotFoundException("Không tìm thấy đơn hàng id:" + orderId));

        String currentStatus = order.getStatus();

        if (!List.of("CONFIRMED", "DELIVERING", "DELIVERED", "CANCELLED").contains(status)) {
            throw new BadRequestException("Trạng thái không hợp lệ");
        }

        if (status.equals("CONFIRMED")) {
            if (!currentStatus.equals("PENDING")) {
                throw new BadRequestException("Chỉ đơn hàng chờ xác nhận mới được xác nhận");
            }

            GhnCreateOrderResponse ghnResponse = ghnService.createOrder(order);

            if (ghnResponse == null || ghnResponse.getData() == null) {
                throw new BadRequestException("Không thể tạo vận đơn GHN");
            }

            order.setGhnOrderCode(ghnResponse.getData().getOrderCode());

            if (ghnResponse.getData().getExpectedDeliveryTime() != null) {
                OffsetDateTime odt = OffsetDateTime.parse(
                        ghnResponse.getData().getExpectedDeliveryTime()
                );
                order.setExpectedDeliveryTime(odt.toLocalDateTime());
            }

            order.setStatus("CONFIRMED");
            order.setShippingStatus("READY_TO_PICK");
            order.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(order);

            saveHistory(order, "CONFIRMED", "Shop đã xác nhận đơn hàng");
            saveHistory(order, "READY_TO_PICK", "Đơn hàng đang chờ đơn vị vận chuyển lấy hàng");

            sendOrderStatusEmail(order, "CONFIRMED");
            sendOrderSocket(orderId);

            return;
        }

        if (status.equals("DELIVERING")) {
            if (!currentStatus.equals("CONFIRMED")) {
                throw new BadRequestException("Chỉ đơn hàng đã xác nhận mới được chuyển sang đang giao");
            }

            order.setStatus("DELIVERING");
            order.setUpdatedAt(LocalDateTime.now());

            orderRepository.save(order);

            saveHistory(order, "DELIVERING", "Đơn hàng đang được vận chuyển đến bạn");

            sendOrderStatusEmail(order, "DELIVERING");
            sendOrderSocket(orderId);

            return;
        }

        if (status.equals("DELIVERED")) {
            if (!currentStatus.equals("DELIVERING")) {
                throw new BadRequestException("Chỉ đơn hàng đang giao mới được chuyển sang đã giao");
            }

            order.setStatus("DELIVERED");
            order.setShippingStatus("DELIVERED");
            order.setUpdatedAt(LocalDateTime.now());

            if (order.getPaymentMethod().equals("COD")) {
                order.setPaymentStatus("PAID");
            }

            orderRepository.save(order);

            saveHistory(order, "DELIVERED", "Đơn hàng đã được giao thành công");

            sendOrderStatusEmail(order, "DELIVERED");
            sendOrderSocket(orderId);

            return;
        }

        if (status.equals("CANCELLED")) {
            if (!List.of("PENDING", "CONFIRMED").contains(currentStatus)) {
                throw new BadRequestException("Đơn hàng hiện tại không thể hủy");
            }

            order.setStatus("CANCELLED");
            order.setUpdatedAt(LocalDateTime.now());

            if (order.getPaymentStatus().equals("PAID")) {
                order.setPaymentStatus("REFUNDED");
            }

            if (order.getGhnOrderCode() != null) {
                order.setShippingStatus("CANCEL");
            }

            restoreStock(order);

            orderRepository.save(order);

            saveHistory(order, "CANCELLED", "Đơn hàng đã được hủy");

            sendOrderStatusEmail(order, "CANCELLED");
            sendOrderSocket(orderId);

            return;
        }
    }

    private void validateRequest(OrderCreateRequest request) {
        if (request.getAddressId() == null) {
            throw new BadRequestException("Vui lòng chọn địa chỉ nhận hàng");
        }

//        if (request.getShippingMethod() == null || request.getShippingMethod().isBlank()) {
//            throw new BadRequestException("Vui lòng chọn phương thức vận chuyển");
//        }

        if (request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()) {
            throw new BadRequestException("Vui lòng chọn phương thức thanh toán");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ít nhất 1 sản phẩm");
        }
    }

    private String generateCode(String s) {
        long now = System.currentTimeMillis();
        long last4 = now % 10000;
        if(s.equals("DH")) return String.format("DH%04d", last4);
        return String.format("HD%04d", last4);
    }

    private void saveHistory(
            OrderEntity order,
            String status,
            String note
    ) {
        OrderStatusEntity history = new OrderStatusEntity();

        history.setOrder(order);
        history.setStatus(status);
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());

        orderStatusRepository.save(history);
    }

    private void restoreStock(OrderEntity order) {

        List<OrderItemEntity> orderItems =
                orderItemRepository.findByOrderId(order.getId());

        for (OrderItemEntity item : orderItems) {

            ProductVariantEntity variant = item.getVariant();

            variant.setStock(
                    variant.getStock() + item.getQuantity()
            );

            productVariantRepository.save(variant);

            ProductEntity product = variant.getProduct();

            product.setSoldCount(
                    product.getSoldCount() - item.getQuantity()
            );

            productRepository.save(product);
        }
    }

    private void sendOrderStatusEmail(OrderEntity order, String status) {
        UserEntity customer = order.getUser();

        emailService.sendOrderStatusEmail(
                customer.getEmail(),
                customer.getFullName(),
                order.getCode(),
                status
        );
    }

    private void sendOrderSocket(Integer orderId) {
        webSocketService.sendToTopic("/topic/orders/" + orderId, new OrderStatusEvent("ORDER_UPDATED_STATUS", orderId));
        webSocketService.sendToTopic("/topic/orders", new OrderStatusEvent("ORDER_UPDATED_STATUS", orderId));
    }

    private String mapStatusTitle(String status) {
        return switch (status) {
            case "PENDING" ->
                    "Đã đặt hàng";
            case "CONFIRMED" ->
                    "Đã xác nhận";
            case "READY_TO_PICK" ->
                    "Chờ lấy hàng";
            case "PICKING" ->
                    "Đang lấy hàng";
            case "PICKED" ->
                    "Đã lấy hàng";
            case "TRANSPORTING" ->
                    "Đang trung chuyển";
            case "DELIVERING" ->
                    "Đang giao hàng";
            case "DELIVERED" ->
                    "Đã giao thành công";
            case "COMPLETED" ->
                    "Hoàn tất";
            case "RETURNING" ->
                    "Đang hoàn hàng";
            case "RETURNED" ->
                    "Đã hoàn hàng";
            case "CANCELLED" ->
                    "Đã hủy";
            default ->
                    "Cập nhật đơn hàng";
        };
    }

}
