package com.carsharingapp.dto.rental;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SearchRentalByIsActive {
    private String userId;
    @NotBlank(message = "IsActive can't be blank")
    private String isActive;
}
