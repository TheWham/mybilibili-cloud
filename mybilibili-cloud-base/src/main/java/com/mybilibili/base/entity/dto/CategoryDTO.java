package com.mybilibili.base.entity.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryDTO {
    @NotNull
    String categoryCode;
    @NotNull
    String categoryName;
    Integer pCategoryId;
    String icon;
    String background;
    Integer categoryId;

}
