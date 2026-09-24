package com.parking.repository;

import com.parking.entity.Vehicle;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface VehicleRepository
        extends JpaRepository<Vehicle, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}


