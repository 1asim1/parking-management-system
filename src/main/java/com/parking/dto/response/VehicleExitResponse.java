package com.parking.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VehicleExitResponse(
        String ticketNumber,
        String vehicleNumber,
        String slotNumber,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        long parkedMinutes,
        BigDecimal parkingFee
) {
}
