package com.parking.dto.response;

import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;

public record SlotResponse(
        Long id,
        String slotNumber,
        SlotType slotType,
        boolean occupied,
        Long floorId
) {
}
