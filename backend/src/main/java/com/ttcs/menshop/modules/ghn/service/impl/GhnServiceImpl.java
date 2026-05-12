package com.ttcs.menshop.modules.ghn.service.impl;

import com.ttcs.menshop.modules.address.repository.GhnDistrictRepository;
import com.ttcs.menshop.modules.address.repository.GhnProvinceRepository;
import com.ttcs.menshop.modules.address.repository.GhnWardRepository;
import com.ttcs.menshop.modules.ghn.dto.response.GhnCreateOrderResponse;
import com.ttcs.menshop.modules.ghn.service.GhnService;
import com.ttcs.menshop.modules.order.entity.OrderEntity;
import com.ttcs.menshop.modules.order_item.entity.OrderItemEntity;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GhnServiceImpl implements GhnService {

    private final RestTemplate restTemplate;
    private final GhnWardRepository ghnWardRepository;
    private final GhnDistrictRepository ghnDistrictRepository;
    private final GhnProvinceRepository ghnProvinceRepository;

    @Value("${ghn.token}")
    private String token;

    @Value("${ghn.shop-id}")
    private Integer ghnShopId;

    @Override
    public GhnCreateOrderResponse createOrder(OrderEntity order) {
        ShopEntity shop = order.getShop();
        HttpHeaders headers = new HttpHeaders();

        headers.set("Token", token);
        headers.set("ShopId", ghnShopId.toString());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("payment_type_id",
                order.getPaymentMethod().equals("COD") ? 2 : 1);
        body.put("note", order.getNote());
        body.put("required_note", "CHOXEMHANGKHONGTHU");
        body.put("client_order_code", order.getCode());

        body.put("from_name", shop.getName());
        body.put("from_phone", shop.getPhone());
        body.put("from_address", shop.getAddress());
        body.put("from_ward_name", ghnWardRepository.findById(shop.getWardId()).get().getWardName());
        body.put("from_district_name", ghnDistrictRepository.findById(shop.getDistrictId()).get().getDistrictName());
        body.put("from_province_name", ghnProvinceRepository.findById(shop.getProvinceId()).get().getProvinceName());

        body.put("to_name", order.getReceiverName());
        body.put("to_phone", order.getReceiverPhone());
        body.put("to_address", order.getAddress());
        body.put("to_ward_code", order.getReceiverWardCode());
        body.put("to_district_id", order.getReceiverDistrictId());
        body.put("service_id", order.getShippingServiceId());
        body.put("service_type_id", 2);
        body.put("cod_amount",
                order.getPaymentMethod().equals("COD")
                        ? order.getFinalTotal().intValue()
                        : 0);
        body.put("weight", calculateWeight(order));

        body.put("length", calculateLength(order));

        body.put("width", calculateWidth(order));

        body.put("height", calculateHeight(order));

        List<Map<String, Object>> items = new ArrayList<>();

        for(OrderItemEntity item : order.getOrderItems()) {
            Map<String, Object> i = new HashMap<>();
            i.put("name",
                    item.getVariant().getProduct().getName());
            i.put("quantity",
                    item.getQuantity());
            i.put("price",
                    item.getUnitPrice().intValue());
            items.add(i);
        }
        body.put("items", items);

        HttpEntity<?> entity = new HttpEntity<>(body, headers);

        ResponseEntity<GhnCreateOrderResponse> response =
                restTemplate.exchange(
                        "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/create",
                        HttpMethod.POST,
                        entity,
                        GhnCreateOrderResponse.class
                );

        return response.getBody();
    }

    private int calculateWeight(OrderEntity order) {
        return order.getOrderItems()
                .stream()
                .mapToInt(item -> {
                    Integer weight =
                            item.getVariant()
                                    .getProduct()
                                    .getWeight();
                    if(weight == null) {
                        weight = 200;
                    }
                    return weight * item.getQuantity();
                })
                .sum();
    }

    private int calculateLength(OrderEntity order) {
        return order.getOrderItems()
                .stream()
                .mapToInt(item -> {
                    Integer length =
                            item.getVariant()
                                    .getProduct()
                                    .getLength();
                    return length != null ? length : 20;
                })
                .max()
                .orElse(20);
    }

    private int calculateWidth(OrderEntity order) {
        return order.getOrderItems()
                .stream()
                .mapToInt(item -> {
                    Integer width =
                            item.getVariant()
                                    .getProduct()
                                    .getWidth();

                    return width != null ? width : 50;
                })
                .max()
                .orElse(50);
    }

    private int calculateHeight(OrderEntity order) {
        return order.getOrderItems()
                .stream()
                .mapToInt(item -> {
                    Integer height =
                            item.getVariant()
                                    .getProduct()
                                    .getHeight();
                    if(height == null) {
                        height = 3;
                    }
                    return height * item.getQuantity();
                })
                .sum();
    }
}
