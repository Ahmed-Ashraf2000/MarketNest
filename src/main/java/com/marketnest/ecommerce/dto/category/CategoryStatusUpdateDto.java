package com.marketnest.ecommerce.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for updating category active status (admin operation)")
public class CategoryStatusUpdateDto {

    @Schema(
            description = "Flag to activate or deactivate the category. Inactive categories are hidden from customers",
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private boolean active;
}