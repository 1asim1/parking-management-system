package com.parking.dto.request;

import com.parking.enums.SlotType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSlotRequest(

        @NotBlank
        @Size(max = 50)
        String slotNumber,

        @NotNull
        SlotType slotType
) {
}
