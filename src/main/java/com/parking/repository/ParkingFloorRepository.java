package com.parking.repository;

import com.parking.entity.ParkingFloor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParkingFloorRepository
        extends JpaRepository<ParkingFloor, Long> {

    boolean existsByFloorNumber(Integer floorNumber);

    Optional<ParkingFloor> findByFloorNumber(Integer floorNumber);
}



