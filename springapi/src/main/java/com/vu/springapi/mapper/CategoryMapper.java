package com.vu.springapi.mapper;

import com.vu.springapi.dto.request.CategoryRequest;
import com.vu.springapi.dto.response.CategoryResponse;
import com.vu.springapi.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toCategory(CategoryRequest request);

    CategoryResponse toCategoryResponse(Category category);

    void updateCategory(@MappingTarget Category category, CategoryRequest request);
}

