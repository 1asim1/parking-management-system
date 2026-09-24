package com.parking.controller;

import com.parking.dto.response.*;
import com.parking.dto.request.*;
import com.parking.dto.request.VehicleEntryRequest;
import com.parking.dto.response.ParkingHistoryResponse;
import com.parking.dto.response.ParkingTicketResponse;
import com.parking.dto.response.VehicleExitResponse;
import com.parking.service.ParkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {

    private final ParkingService parkingService;

    @PostMapping("/entry")
    public ResponseEntity<ParkingTicketResponse> vehicleEntry(
            @Valid @RequestBody VehicleEntryRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        parkingService.vehicleEntry(request)
                );
    }

    @PostMapping("/exit/{ticketNumber}")
    public VehicleExitResponse vehicleExit(
            @PathVariable String ticketNumber) {

        return parkingService.vehicleExit(ticketNumber);
    }

    @GetMapping("/vehicles/{vehicleNumber}/active-ticket")
    public ParkingTicketResponse getActiveTicket(
            @PathVariable String vehicleNumber) {

        return parkingService.getActiveTicket(
                vehicleNumber
        );
    }

    @GetMapping("/tickets/{ticketNumber}")
    public ParkingTicketResponse getTicket(
            @PathVariable String ticketNumber) {

        return parkingService.getTicket(
                ticketNumber
        );
    }

    @GetMapping("/history")
    public Page<ParkingHistoryResponse> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 100) {
            size = 20;
        }

        return parkingService.getHistory(page, size);
    }

    @GetMapping("/history/completed")
    public Page<ParkingHistoryResponse> getCompletedHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (page < 0) {
            page = 0;
        }

        if (size < 1 || size > 100) {
            size = 20;
        }

        return parkingService.getCompletedHistory(
                page,
                size
        );
    }
}
