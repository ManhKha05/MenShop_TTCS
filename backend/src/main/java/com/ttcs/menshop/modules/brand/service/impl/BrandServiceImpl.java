package com.ttcs.menshop.modules.brand.service.impl;

import com.ttcs.menshop.modules.brand.entity.BrandEntity;
import com.ttcs.menshop.modules.brand.repository.BrandRepository;
import com.ttcs.menshop.modules.brand.service.BrandService;
import com.ttcs.menshop.modules.product.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    public BrandServiceImpl(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }


    @Override
    public BrandEntity getOrCreateBrand(String brandName) {
        if (brandName == null || brandName.trim().isEmpty()) {
            return null;
        }

        return brandRepository.findByNameIgnoreCase(brandName.trim())
                .orElseGet(() -> {
                    BrandEntity newBrand = new BrandEntity();
                    newBrand.setName(brandName.trim());
                    return brandRepository.save(newBrand);
                });
    }
}
