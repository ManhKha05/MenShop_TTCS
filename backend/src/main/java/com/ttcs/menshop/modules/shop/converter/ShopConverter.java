package com.ttcs.menshop.modules.shop.converter;

import com.ttcs.menshop.modules.product.repository.ProductRepository;
import com.ttcs.menshop.modules.shop.dto.request.ShopRequest;
import com.ttcs.menshop.modules.shop.dto.response.ShopResponse;
import com.ttcs.menshop.modules.shop.entity.ShopEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class ShopConverter {

    private final ModelMapper modelMapper;
    private final ProductRepository productRepository;

    public ShopConverter(ModelMapper modelMapper, ProductRepository productRepository) {
        this.modelMapper = modelMapper;
        this.productRepository = productRepository;
    }

    public ShopResponse toResponse(ShopEntity shopEntity) {
        ShopResponse response = modelMapper.map(shopEntity, ShopResponse.class);
        response.setOwnerName(shopEntity.getUser().getFullName());

        response.setTotalProducts((int)productRepository.countActiveProductsByShopId(shopEntity.getId()));
        response.setJoinedDays(ChronoUnit.DAYS.between(shopEntity.getCreatedAt().toLocalDate(), LocalDate.now()));
        response.setTotalOrders((int)shopEntity.getOrders().size());
        return response;
    }

    public ShopEntity toEntity(ShopRequest shopRequest) {
        return modelMapper.map(shopRequest, ShopEntity.class);
    }
}
