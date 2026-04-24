package com.ttcs.menshop.modules.flash_sale.converter;

import com.ttcs.menshop.modules.flash_sale.dto.request.SaleRequest;
import com.ttcs.menshop.modules.flash_sale.dto.response.SaleResponse;
import com.ttcs.menshop.modules.flash_sale.entity.SaleEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class SaleConverter {

    private final ModelMapper modelMapper;
    public SaleConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public SaleResponse toReponse(SaleEntity saleEntity) {
        return modelMapper.map(saleEntity, SaleResponse.class);
    }
    public SaleEntity toEntity(SaleRequest saleRequest) {
        return modelMapper.map(saleRequest, SaleEntity.class);
    }
}
