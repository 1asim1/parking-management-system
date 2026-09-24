package com.parking.service;

import com.parking.dto.request.VehicleEntryRequest;
import com.parking.dto.response.ParkingTicketResponse;
import com.parking.entity.*;
import com.parking.enums.*;
import com.parking.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class ParkingServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private ParkingSlotRepository slotRepository;

    @Mock
    private ParkingTicketRepository ticketRepository;

    @InjectMocks
    private ParkingService parkingService;

    @Test
    void bike30MinutesShouldCost20() {

        LocalDateTime entry =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime exit =
                entry.plusMinutes(30);

        BigDecimal fee =
                parkingService.calculateFee(
                        VehicleType.BIKE,
                        entry,
                        exit
                );

        assertEquals(
                BigDecimal.valueOf(20),
                fee
        );
    }

    @Test
    void bikeExactlyOneHourShouldCost20() {

        LocalDateTime entry =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime exit =
                entry.plusHours(1);

        BigDecimal fee =
                parkingService.calculateFee(
                        VehicleType.BIKE,
                        entry,
                        exit
                );

        assertEquals(
                BigDecimal.valueOf(20),
                fee
        );
    }

    @Test
    void bikeOneHourTenMinutesShouldCost30() {

        LocalDateTime entry =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime exit =
                entry.plusMinutes(70);

        BigDecimal fee =
                parkingService.calculateFee(
                        VehicleType.BIKE,
                        entry,
                        exit
                );

        assertEquals(
                BigDecimal.valueOf(30),
                fee
        );
    }

    @Test
    void carTwoHoursTenMinutesShouldCost60() {

        LocalDateTime entry =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime exit =
                entry.plusMinutes(130);

        BigDecimal fee =
                parkingService.calculateFee(
                        VehicleType.CAR,
                        entry,
                        exit
                );

        assertEquals(
                BigDecimal.valueOf(80),
                fee
        );
    }

    @Test
    void suvThreeHoursShouldCost110() {

        LocalDateTime entry =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime exit =
                entry.plusHours(3);

        BigDecimal fee =
                parkingService.calculateFee(
                        VehicleType.SUV,
                        entry,
                        exit
                );

        assertEquals(
                BigDecimal.valueOf(110),
                fee
        );
    }

    @Test
    void evTwoHoursShouldCost60() {

        LocalDateTime entry =
                LocalDateTime.of(2026, 1, 1, 10, 0);

        LocalDateTime exit =
                entry.plusHours(2);

        BigDecimal fee =
                parkingService.calculateFee(
                        VehicleType.EV,
                        entry,
                        exit
                );

        assertEquals(
                BigDecimal.valueOf(60),
                fee
        );
    }

    @Test
    void shouldAllocateAvailableSlot() {

        VehicleEntryRequest request =
                new VehicleEntryRequest(
                        "MH12AB1234",
                        VehicleType.CAR
                );

        Vehicle vehicle = Vehicle.builder()
                .id(1L)
                .vehicleNumber("MH12AB1234")
                .vehicleType(VehicleType.CAR)
                .active(false)
                .build();

        ParkingFloor floor = ParkingFloor.builder()
                .id(1L)
                .floorNumber(1)
                .name("Ground Floor")
                .build();

        ParkingSlot slot = ParkingSlot.builder()
                .id(1L)
                .slotNumber("M-001")
                .slotType(SlotType.MEDIUM)
                .occupied(false)
                .floor(floor)
                .build();

        when(vehicleRepository.findByVehicleNumber(
                "MH12AB1234"
        )).thenReturn(Optional.of(vehicle));

        when(slotRepository.findAvailableSlotsForUpdate(
                anyList()
        )).thenReturn(List.of(slot));

        when(vehicleRepository.save(any(Vehicle.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(slotRepository.save(any(ParkingSlot.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        when(ticketRepository.save(any(ParkingTicket.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));

        ParkingTicketResponse response =
                parkingService.vehicleEntry(request);

        assertNotNull(response);
        assertEquals(
                "MH12AB1234",
                response.vehicleNumber()
        );
        assertEquals(
                "M-001",
                response.slotNumber()
        );
        assertEquals(
                TicketStatus.ACTIVE,
                response.status()
        );

        assertTrue(slot.isOccupied());
        assertTrue(vehicle.isActive());

        verify(
                slotRepository,
                times(1)
        ).findAvailableSlotsForUpdate(anyList());
    }
}
