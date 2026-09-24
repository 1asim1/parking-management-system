package com.parking.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.parking.dto.request.CreateFloorRequest;
import com.parking.dto.response.FloorResponse;
import com.parking.service.ParkingFloorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/floors")
@RequiredArgsConstructor
public class ParkingFloorController {

    private final ParkingFloorService floorService;

    @PostMapping
    public ResponseEntity<FloorResponse> createFloor(
            @Valid @RequestBody CreateFloorRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(floorService.createFloor(request));
    }

    @GetMapping
    public List<FloorResponse> getFloors() {
        return floorService.getAllFloors();
    }

    @PutMapping("/{floorId}")
    public FloorResponse updateFloor(
            @PathVariable Long floorId,
            @Valid @RequestBody CreateFloorRequest request) {

        return floorService.updateFloor(
                floorId,
                request
        );
    }

    @DeleteMapping("/{floorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFloor(
            @PathVariable Long floorId) {

        floorService.deleteFloor(floorId);
    }
}
