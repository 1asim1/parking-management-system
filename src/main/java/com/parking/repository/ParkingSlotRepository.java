package com.parking.repository;

import com.parking.entity.ParkingSlot;
import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParkingSlotRepository
        extends JpaRepository<ParkingSlot, Long> {

    List<ParkingSlot> findByFloorId(Long floorId);

    boolean existsByFloorIdAndSlotNumber(
            Long floorId,
            String slotNumber
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM ParkingSlot s
        JOIN FETCH s.floor f
        WHERE s.slotType IN :slotTypes
          AND s.occupied = false
        ORDER BY f.floorNumber ASC, s.slotNumber ASC
        """)
    List<ParkingSlot> findAvailableSlotsForUpdate(
            @Param("slotTypes") List<SlotType> slotTypes
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s
        FROM ParkingSlot s
        JOIN FETCH s.floor f
        WHERE s.id = :slotId
        """)
    Optional<ParkingSlot> findByIdForUpdate(
            @Param("slotId") Long slotId
    );
}

