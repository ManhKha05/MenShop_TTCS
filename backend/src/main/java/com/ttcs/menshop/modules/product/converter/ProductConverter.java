package com.ttcs.menshop.modules.product.converter;

import com.ttcs.menshop.modules.product.dto.response.ProductResponse;
import com.ttcs.menshop.modules.product.entity.ProductEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ProductConverter {

    private final ModelMapper modelMapper;
    public ProductConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ProductResponse mapToResponse(ProductEntity p) {
        ProductResponse res = new ProductResponse();

        res.setId(p.getId());
        res.setName(p.getName());
        res.setPrice(p.getSalePrice() != null ? p.getSalePrice() : p.getPrice());
        res.setStatus(p.getStatus());

        // category
        res.setCategory(p.getCategory() != null ? p.getCategory().getName() : null);

        // shop
        res.setShop(p.getShop() != null ? p.getShop().getName() : null);

        // image (lấy ảnh đầu tiên)
        if (!p.getImages().isEmpty()) {
            res.setImage(p.getImages().get(0).getImageUrl());
        }

        // stock = tổng variants
        int stock = p.getVariants().stream()
                .mapToInt(v -> v.getStock())
                .sum();

        res.setStock(stock);

        return res;
    }
}
