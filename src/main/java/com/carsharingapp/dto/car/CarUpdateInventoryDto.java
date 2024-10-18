package com.carsharingapp.dto.car;

import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CarUpdateInventoryDto {
    @Positive(message = "Inventory should be positive")
    private int inventory;
}
