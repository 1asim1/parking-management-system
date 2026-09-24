package com.parking.service;

import com.parking.enums.VehicleType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ParkingFeeService {

    BigDecimal calculateFee(VehicleType vehicleType, LocalDateTime entryTime,
                            LocalDateTime exitTime);
}
