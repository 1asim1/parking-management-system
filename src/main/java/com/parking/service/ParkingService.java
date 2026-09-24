package com.parking.service;

import com.parking.dto.response.*;
import com.parking.dto.request.*;
import com.parking.dto.request.VehicleEntryRequest;
import com.parking.dto.response.ParkingHistoryResponse;
import com.parking.dto.response.ParkingTicketResponse;
import com.parking.dto.response.VehicleExitResponse;
import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.exception.*;
import com.parking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParkingService {

    private final VehicleRepository vehicleRepository;
    private final ParkingSlotRepository slotRepository;
    private final ParkingTicketRepository ticketRepository;

    /**
     * Vehicle entry.
     *
     * The transaction locks the vehicle row and then locks candidate
     * parking slots. This prevents two concurrent requests from
     * allocating the same slot or allowing the same vehicle to
     * enter twice.
     */
    @Transactional
    public ParkingTicketResponse vehicleEntry(
            VehicleEntryRequest request) {

        String vehicleNumber = normalizeVehicleNumber(
                request.vehicleNumber()
        );

        /*
         * Pessimistically lock the vehicle row if it already exists.
         *
         * If the vehicle doesn't exist, we create it.
         */
        Vehicle vehicle = vehicleRepository
                .findByVehicleNumber(vehicleNumber)
                .orElseGet(() ->
                        vehicleRepository.save(
                                Vehicle.builder()
                                        .vehicleNumber(vehicleNumber)
                                        .vehicleType(request.vehicleType())
                                        .active(false)
                                        .build()
                        )
                );

        if (vehicle.isActive()) {
            throw new DuplicateVehicleException(
                    "Vehicle is already inside parking: "
                            + vehicleNumber
            );
        }

        /*
         * Keep the registered vehicle type synchronized with the
         * current request.
         */
        vehicle.setVehicleType(request.vehicleType());

        List<SlotType> compatibleSlotTypes =
                getCompatibleSlotTypes(request.vehicleType());

        /*
         * IMPORTANT:
         * findAvailableSlotsForUpdate() uses SELECT ... FOR UPDATE.
         *
         * Therefore another transaction cannot simultaneously
         * select the same slot.
         */
        List<ParkingSlot> slots =
                slotRepository.findAvailableSlotsForUpdate(
                        compatibleSlotTypes
                );

        if (slots.isEmpty()) {
            throw new NoAvailableSlotException(
                    "No available slot for vehicle type: "
                            + request.vehicleType()
            );
        }

        ParkingSlot slot = slots.get(0);

        slot.setOccupied(true);

        LocalDateTime entryTime = LocalDateTime.now();

        String ticketNumber =
                "TKT-" + UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 16)
                        .toUpperCase();

        ParkingTicket ticket = ParkingTicket.builder()
                .ticketNumber(ticketNumber)
                .vehicle(vehicle)
                .slot(slot)
                .entryTime(entryTime)
                .status(TicketStatus.ACTIVE)
                .build();

        vehicle.setActive(true);

        vehicleRepository.save(vehicle);
        slotRepository.save(slot);
        ticketRepository.save(ticket);

        return toResponse(ticket);
    }

    @Transactional
    public VehicleExitResponse vehicleExit(String ticketNumber) {

        ParkingTicket ticket =
                ticketRepository.findByTicketNumberForUpdate(ticketNumber)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Ticket not found: " + ticketNumber
                                )
                        );

        if (ticket.getStatus() == TicketStatus.COMPLETED) {
            throw new BadRequestException(
                    "Ticket has already been completed: "
                            + ticketNumber
            );
        }

        LocalDateTime exitTime = LocalDateTime.now();

        long parkedMinutes = Duration.between(
                ticket.getEntryTime(),
                exitTime
        ).toMinutes();

        BigDecimal fee = calculateFee(
                ticket.getVehicle().getVehicleType(),
                ticket.getEntryTime(),
                exitTime
        );

        /*
         * Ticket is already locked by findByTicketNumberForUpdate().
         */
        ticket.setExitTime(exitTime);
        ticket.setParkingFee(fee);
        ticket.setStatus(TicketStatus.COMPLETED);

        /*
         * Lock the slot before releasing it.
         */
        ParkingSlot slot =
                slotRepository.findByIdForUpdate(
                        ticket.getSlot().getId()
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Slot not found: "
                                        + ticket.getSlot().getId()
                        )
                );

        slot.setOccupied(false);

        Vehicle vehicle = ticket.getVehicle();
        vehicle.setActive(false);

        slotRepository.save(slot);
        vehicleRepository.save(vehicle);
        ticketRepository.save(ticket);

        return new VehicleExitResponse(
                ticket.getTicketNumber(),
                vehicle.getVehicleNumber(),
                slot.getSlotNumber(),
                ticket.getEntryTime(),
                exitTime,
                parkedMinutes,
                fee
        );
    }

    @Transactional(readOnly = true)
    public ParkingTicketResponse getActiveTicket(
            String vehicleNumber) {

        String normalized =
                normalizeVehicleNumber(vehicleNumber);

        Vehicle vehicle =
                vehicleRepository.findByVehicleNumber(normalized)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Vehicle not found: " + normalized
                                )
                        );

        ParkingTicket ticket =
                ticketRepository.findFirstByVehicleIdAndStatus(
                        vehicle.getId(),
                        TicketStatus.ACTIVE
                ).orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Vehicle is not currently parked"
                        )
                );

        return toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public ParkingTicketResponse getTicket(
            String ticketNumber) {

        ParkingTicket ticket =
                ticketRepository.findByTicketNumber(ticketNumber)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Ticket not found: " + ticketNumber
                                )
                        );

        return toResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Page<ParkingHistoryResponse> getHistory(
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "entryTime"
                )
        );

        return ticketRepository
                .findAllByOrderByEntryTimeDesc(pageable)
                .map(this::toHistoryResponse);
    }

    @Transactional(readOnly = true)
    public Page<ParkingHistoryResponse> getCompletedHistory(
            int page,
            int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "entryTime"
                )
        );

        return ticketRepository
                .findByStatusOrderByEntryTimeDesc(
                        TicketStatus.COMPLETED,
                        pageable
                )
                .map(this::toHistoryResponse);
    }

    /**
     * Parking fee calculation.
     *
     * First started hour:
     * BIKE = 20
     * CAR  = 40
     * SUV  = 50
     * EV   = 40
     *
     * Every additional started hour:
     * BIKE = 10
     * CAR  = 20
     * SUV  = 30
     * EV   = 20
     */
    public BigDecimal calculateFee(
            VehicleType vehicleType,
            LocalDateTime entryTime,
            LocalDateTime exitTime) {

        long minutes = Duration.between(
                entryTime,
                exitTime
        ).toMinutes();

        if (minutes < 0) {
            throw new BadRequestException(
                    "Exit time cannot be before entry time"
            );
        }

        /*
         * Minimum charge is the first hour.
         *
         * 0-60 minutes => 1 hour
         * 61-120       => 2 hours
         * etc.
         */
        long startedHours =
                Math.max(1, (minutes + 59) / 60);

        BigDecimal firstHour;
        BigDecimal additionalHour;

        switch (vehicleType) {

            case BIKE -> {
                firstHour = BigDecimal.valueOf(20);
                additionalHour = BigDecimal.valueOf(10);
            }

            case CAR -> {
                firstHour = BigDecimal.valueOf(40);
                additionalHour = BigDecimal.valueOf(20);
            }

            case SUV -> {
                firstHour = BigDecimal.valueOf(50);
                additionalHour = BigDecimal.valueOf(30);
            }

            case EV -> {
                firstHour = BigDecimal.valueOf(40);
                additionalHour = BigDecimal.valueOf(20);
            }

            default -> throw new BadRequestException(
                    "Unsupported vehicle type: " + vehicleType
            );
        }

        if (startedHours == 1) {
            return firstHour;
        }

        return firstHour.add(
                additionalHour.multiply(
                        BigDecimal.valueOf(startedHours - 1)
                )
        );
    }

    private List<SlotType> getCompatibleSlotTypes(
            VehicleType vehicleType) {

        return switch (vehicleType) {

            /*
             * Bike can use small first, but medium/large are
             * also physically compatible.
             */
            case BIKE ->
                    List.of(
                            SlotType.SMALL,
                            SlotType.MEDIUM,
                            SlotType.LARGE
                    );

            /*
             * Cars cannot use SMALL.
             */
            case CAR ->
                    List.of(
                            SlotType.MEDIUM,
                            SlotType.LARGE
                    );

            /*
             * SUV requires a large slot.
             */
            case SUV ->
                    List.of(SlotType.LARGE);

            /*
             * EV vehicles must use EV charging slots.
             */
            case EV ->
                    List.of(SlotType.EV);
        };
    }

    private String normalizeVehicleNumber(
            String vehicleNumber) {

        return vehicleNumber
                .trim()
                .toUpperCase();
    }

    private ParkingTicketResponse toResponse(
            ParkingTicket ticket) {

        return new ParkingTicketResponse(
                ticket.getTicketNumber(),
                ticket.getVehicle().getVehicleNumber(),
                ticket.getVehicle().getVehicleType(),
                ticket.getSlot().getFloor().getFloorNumber(),
                ticket.getSlot().getSlotNumber(),
                ticket.getEntryTime(),
                ticket.getExitTime(),
                ticket.getParkingFee(),
                ticket.getStatus()
        );
    }

    private ParkingHistoryResponse toHistoryResponse(
            ParkingTicket ticket) {

        return new ParkingHistoryResponse(
                ticket.getTicketNumber(),
                ticket.getVehicle().getVehicleNumber(),
                ticket.getVehicle().getVehicleType(),
                ticket.getSlot().getFloor().getFloorNumber(),
                ticket.getSlot().getSlotNumber(),
                ticket.getEntryTime(),
                ticket.getExitTime(),
                ticket.getParkingFee(),
                ticket.getStatus()
        );
    }



}

