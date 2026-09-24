package com.parking.controller;

import com.parking.dto.request.CreateSlotRequest;
import com.parking.dto.response.SlotResponse;
import com.parking.service.ParkingSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/floors/{floorId}/slots")
@RequiredArgsConstructor
public class ParkingSlotController {

    private final ParkingSlotService slotService;

    @PostMapping
    public ResponseEntity<SlotResponse> createSlot(
            @PathVariable Long floorId,
            @Valid @RequestBody CreateSlotRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        slotService.createSlot(
                                floorId,
                                request
                        )
                );
    }

    @GetMapping
    public List<SlotResponse> getSlots(
            @PathVariable Long floorId) {

        return slotService.getSlots(floorId);
    }
}
