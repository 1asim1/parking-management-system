package com.parking.dto.request;

import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import jakarta.validation.constraints.NotNull;

public record UpdateSlotRequest(

        @NotNull(message = "Slot type is required")
        SlotType slotType,

        @NotNull(message = "Slot status is required")
        SlotStatus status
) {
}
