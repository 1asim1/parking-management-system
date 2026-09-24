package com.parking.repository;

import com.parking.entity.ParkingTicket;
import com.parking.enums.TicketStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ParkingTicketRepository
        extends JpaRepository<ParkingTicket, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t
        FROM ParkingTicket t
        JOIN FETCH t.vehicle v
        JOIN FETCH t.slot s
        JOIN FETCH s.floor f
        WHERE t.ticketNumber = :ticketNumber
        """)
    Optional<ParkingTicket> findByTicketNumberForUpdate(
            String ticketNumber
    );

    Optional<ParkingTicket> findByTicketNumber(
            String ticketNumber
    );

    Optional<ParkingTicket> findFirstByVehicleIdAndStatus(
            Long vehicleId,
            TicketStatus status
    );

    Page<ParkingTicket> findAllByOrderByEntryTimeDesc(
            Pageable pageable
    );

    Page<ParkingTicket> findByStatusOrderByEntryTimeDesc(
            TicketStatus status,
            Pageable pageable
    );
}


