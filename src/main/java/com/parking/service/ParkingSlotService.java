package com.parking.service;

import com.parking.dto.request.CreateSlotRequest;
import com.parking.dto.request.UpdateSlotRequest;
import com.parking.dto.response.SlotResponse;
import com.parking.dto.request.CreateSlotRequest;
import com.parking.dto.response.SlotResponse;
import com.parking.entity.ParkingFloor;
import com.parking.entity.ParkingSlot;
import com.parking.exception.BadRequestException;
import com.parking.exception.ResourceNotFoundException;
import com.parking.repository.ParkingFloorRepository;
import com.parking.repository.ParkingSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingSlotService {

    private final ParkingSlotRepository slotRepository;
    private final ParkingFloorRepository floorRepository;

    @Transactional
    public SlotResponse createSlot(
            Long floorId,
            CreateSlotRequest request) {

        ParkingFloor floor = floorRepository.findById(floorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Floor not found: " + floorId
                        )
                );

        if (slotRepository.existsByFloorIdAndSlotNumber(
                floorId,
                request.slotNumber())) {

            throw new BadRequestException(
                    "Slot already exists on this floor: "
                            + request.slotNumber()
            );
        }

        ParkingSlot slot = ParkingSlot.builder()
                .slotNumber(request.slotNumber())
                .slotType(request.slotType())
                .occupied(false)
                .floor(floor)
                .build();

        return toResponse(slotRepository.save(slot));
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> getSlots(Long floorId) {

        if (!floorRepository.existsById(floorId)) {
            throw new ResourceNotFoundException(
                    "Floor not found: " + floorId
            );
        }

        return slotRepository.findByFloorId(floorId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SlotResponse updateSlot(
            Long slotId,
            CreateSlotRequest request) {

        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Slot not found: " + slotId
                        )
                );

        if (slot.isOccupied()) {
            throw new BadRequestException(
                    "Cannot modify an occupied slot"
            );
        }

        slot.setSlotNumber(request.slotNumber());
        slot.setSlotType(request.slotType());

        return toResponse(slotRepository.save(slot));
    }

    @Transactional
    public void deleteSlot(Long slotId) {

        ParkingSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Slot not found: " + slotId
                        )
                );

        if (slot.isOccupied()) {
            throw new BadRequestException(
                    "Cannot delete an occupied slot"
            );
        }

        slotRepository.delete(slot);
    }

    private SlotResponse toResponse(ParkingSlot slot) {

        return new SlotResponse(
                slot.getId(),
                slot.getSlotNumber(),
                slot.getSlotType(),
                slot.isOccupied(),
                slot.getFloor().getId()
        );
    }

}
