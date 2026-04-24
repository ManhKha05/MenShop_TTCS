package com.ttcs.menshop.modules.category.converter;

import com.ttcs.menshop.modules.category.dto.request.CategoryRequest;
import com.ttcs.menshop.modules.category.dto.response.CategoryResponse;
import com.ttcs.menshop.modules.category.entity.CategoryEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class CategoryConverter {

    private final ModelMapper modelMapper;
    public CategoryConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CategoryResponse toResponse(CategoryEntity categoryEntity) {
        return modelMapper.map(categoryEntity, CategoryResponse.class);
    }

    public CategoryEntity toEntity(CategoryRequest categoryRequest) {
        return modelMapper.map(categoryRequest, CategoryEntity.class);
    }
}
