package com.ttcs.menshop.modules.brand.service;

import com.ttcs.menshop.modules.brand.entity.BrandEntity;

public interface BrandService {
    BrandEntity getOrCreateBrand(String name);
}
