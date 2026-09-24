package com.parking.dto.request;

import jakarta.validation.constraints.*;

public record CreateFloorRequest(

        @NotNull
        @Min(1)
        Integer floorNumber,

        @NotBlank
        @Size(max = 100)
        String name
) {
}
