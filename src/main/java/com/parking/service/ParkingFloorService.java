package com.parking.service;

import com.parking.dto.request.CreateFloorRequest;
import com.parking.dto.response.FloorResponse;
import com.parking.entity.ParkingFloor;
import com.parking.exception.BadRequestException;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.ParkingFloorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingFloorService {

    private final ParkingFloorRepository floorRepository;

    @Transactional
    public FloorResponse createFloor(CreateFloorRequest request) {

        if (floorRepository.existsByFloorNumber(request.floorNumber())) {
            throw new BadRequestException(
                    "Floor number already exists: " + request.floorNumber()
            );
        }

        ParkingFloor floor = ParkingFloor.builder()
                .floorNumber(request.floorNumber())
                .name(request.name())
                .build();

        floor = floorRepository.save(floor);

        return toResponse(floor);
    }

    @Transactional(readOnly = true)
    public List<FloorResponse> getAllFloors() {

        return floorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FloorResponse updateFloor(
            Long floorId,
            CreateFloorRequest request) {

        ParkingFloor floor = floorRepository.findById(floorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Floor not found: " + floorId
                        )
                );

        if (!floor.getFloorNumber().equals(request.floorNumber())
                && floorRepository.existsByFloorNumber(request.floorNumber())) {

            throw new BadRequestException(
                    "Floor number already exists: " + request.floorNumber()
            );
        }

        floor.setFloorNumber(request.floorNumber());
        floor.setName(request.name());

        return toResponse(floorRepository.save(floor));
    }

    @Transactional
    public void deleteFloor(Long floorId) {

        ParkingFloor floor = floorRepository.findById(floorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Floor not found: " + floorId
                        )
                );

        boolean hasOccupiedSlot = floor.getSlots()
                .stream()
                .anyMatch(slot -> slot.isOccupied());

        if (hasOccupiedSlot) {
            throw new BadRequestException(
                    "Cannot delete floor containing occupied slots"
            );
        }

        floorRepository.delete(floor);
    }

    private FloorResponse toResponse(ParkingFloor floor) {
        return new FloorResponse(
                floor.getId(),
                floor.getFloorNumber(),
                floor.getName()
        );
    }
}
