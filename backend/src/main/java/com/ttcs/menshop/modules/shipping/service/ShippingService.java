package com.ttcs.menshop.modules.shipping.service;

import com.ttcs.menshop.exception.BadRequestException;
import com.ttcs.menshop.exception.NotFoundException;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.shipping.dto.request.PackageInfo;
import com.ttcs.menshop.modules.shipping.dto.request.ShippingServiceRequest;
import com.ttcs.menshop.modules.shipping.dto.response.GhnAvailableServiceResponse;
import com.ttcs.menshop.modules.shipping.dto.response.GhnFeeResponse;
import com.ttcs.menshop.modules.shipping.dto.response.GhnLeadTimeResponse;
import com.ttcs.menshop.modules.shipping.dto.response.ShippingServiceResponse;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import com.ttcs.menshop.modules.shop.repository.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final RestTemplate restTemplate;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;

    @Value("${ghn.token}")
    private String ghnToken;

    @Value("${ghn.shop-id}")
    private Integer ghnShopId;

    public List<ShippingServiceResponse> getAvailableServices(ShippingServiceRequest request) {
        ShopEntity shop = shopRepository.findById(request.getShopId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy shop"));

        if (shop.getDistrictId() == null || shop.getWardId() == null) {
            throw new BadRequestException("Shop chưa cấu hình địa chỉ gửi hàng");
        }

        List<GhnAvailableServiceResponse.ServiceData> services =
                getAvailableServicesFromGhn(shop.getDistrictId(), request.getToDistrictId());

        List<ShippingServiceResponse> result = new ArrayList<>();

        for (GhnAvailableServiceResponse.ServiceData service : services) {
            try {
                Integer fee = getFee(shop, request, service.getService_id());
                String leadtime = getLeadTime(shop, request, service.getService_id());

                result.add(ShippingServiceResponse.builder()
                        .serviceId(service.getService_id())
                        .serviceTypeId(service.getService_type_id())
                        .name(mapServiceName(service.getShort_name()))
                        .description(leadtime)
                        .fee(fee)
                        .expectedDeliveryTime(leadtime)
                        .build());

            } catch (HttpClientErrorException e) {
                System.out.println("GHN service lỗi, bỏ qua serviceId = "
                        + service.getService_id()
                        + ", message = "
                        + e.getResponseBodyAsString());
            }
        }


        result.sort(Comparator.comparing(ShippingServiceResponse::getFee));

        return result;
    }

    private PackageInfo calculatePackageInfo(ShippingServiceRequest request) {

        int totalWeight = 0;

        int maxLength = 0;
        int maxWidth = 0;
        int totalHeight = 0;

        for (ShippingServiceRequest.ItemRequest item : request.getItems()) {

            ProductEntity product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));

            int quantity = item.getQuantity();

            int weight = Optional.ofNullable(product.getWeight()).orElse(6000);

            int length = Optional.ofNullable(product.getLength()).orElse(20);

            int width = Optional.ofNullable(product.getWidth()).orElse(20);

            int height = Optional.ofNullable(product.getHeight()).orElse(10);

            totalWeight += weight * quantity;

            maxLength = Math.max(maxLength, length);

            maxWidth = Math.max(maxWidth, width);

            totalHeight += height * quantity;
        }

        if (totalWeight <= 0) {
            totalWeight = 1000;
        }

        return new PackageInfo(
                totalWeight,
                maxLength,
                maxWidth,
                totalHeight
        );
    }

    private List<GhnAvailableServiceResponse.ServiceData> getAvailableServicesFromGhn(
            Integer fromDistrictId,
            Integer toDistrictId
    ) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/available-services";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);

        Map<String, Object> body = new HashMap<>();
        body.put("shop_id", ghnShopId);
        body.put("from_district", fromDistrictId);
        body.put("to_district", toDistrictId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<GhnAvailableServiceResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        GhnAvailableServiceResponse.class
                );

        if (response.getBody() == null || response.getBody().getData() == null) {
            return List.of();
        }

        return response.getBody().getData();
    }

    private Integer getFee(
            ShopEntity shop,
            ShippingServiceRequest request,
            Integer serviceId
    ) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";

        HttpHeaders headers = ghnHeadersWithShopId();

        Map<String, Object> body = new HashMap<>();
        body.put("service_id", serviceId);
        body.put("insurance_value", request.getInsuranceValue() == null ? 0 : request.getInsuranceValue());

        body.put("from_district_id", shop.getDistrictId());
        body.put("from_ward_code", shop.getWardId());

        body.put("to_district_id", request.getToDistrictId());
        body.put("to_ward_code", request.getToWardCode());

        PackageInfo packageInfo = calculatePackageInfo(request);

        body.put("weight", packageInfo.getWeight());
        body.put("length", packageInfo.getLength());
        body.put("width", packageInfo.getWidth());
        body.put("height", packageInfo.getHeight());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<GhnFeeResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        GhnFeeResponse.class
                );

        if (response.getBody() == null || response.getBody().getData() == null) {
            return 0;
        }

        return response.getBody().getData().getTotal();
    }

    private String getLeadTime(
            ShopEntity shop,
            ShippingServiceRequest request,
            Integer serviceId
    ) {
        String url = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/leadtime";

        HttpHeaders headers = ghnHeadersWithShopId();

        Map<String, Object> body = new HashMap<>();
        body.put("from_district_id", shop.getDistrictId());
        body.put("from_ward_code", shop.getWardId());

        body.put("to_district_id", request.getToDistrictId());
        body.put("to_ward_code", request.getToWardCode());

        body.put("service_id", serviceId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<GhnLeadTimeResponse> response =
                restTemplate.exchange(
                        url,
                        HttpMethod.POST,
                        entity,
                        GhnLeadTimeResponse.class
                );

        if (
                response.getBody() == null ||
                        response.getBody().getData() == null ||
                        response.getBody().getData().getLeadtime() == null
        ) {
            return "Chưa có thời gian dự kiến";
        }

        Long timestamp = response.getBody().getData().getLeadtime();

        LocalDate date = Instant.ofEpochSecond(timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return "Dự kiến nhận hàng: " +
                date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private HttpHeaders ghnHeadersWithShopId() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnToken);
        headers.set("ShopId", String.valueOf(ghnShopId));
        return headers;
    }

    private String mapServiceName(String shortName) {
        if ("Hàng nhẹ".equalsIgnoreCase(shortName)) {
            return "Giao hàng tiêu chuẩn";
        }

        if ("Hàng nặng".equalsIgnoreCase(shortName)) {
            return "Giao hàng hàng cồng kềnh";
        }

        return shortName;
    }
}