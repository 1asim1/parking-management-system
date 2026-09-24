package com.parking.dto.request;

import com.parking.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VehicleEntryRequest(

        @NotBlank
        @Size(min = 2, max = 20)
        String vehicleNumber,

        @NotNull
        VehicleType vehicleType
) {
}
