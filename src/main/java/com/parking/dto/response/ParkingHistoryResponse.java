package com.parking.dto.response;

import com.parking.enums.TicketStatus;
import com.parking.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ParkingHistoryResponse(
        String ticketNumber,
        String vehicleNumber,
        VehicleType vehicleType,
        Integer floorNumber,
        String slotNumber,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        BigDecimal parkingFee,
        TicketStatus status
) {
}
