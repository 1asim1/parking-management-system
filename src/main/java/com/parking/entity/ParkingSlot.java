package com.parking.entity;

import com.parking.enums.SlotStatus;
import com.parking.enums.SlotType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.GenerationType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Version;
import jakarta.persistence.EnumType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;


@Entity
@Table(
        name = "parking_slots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_floor_slot_number",
                        columnNames = {"floor_id", "slot_number"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "slot_number", nullable = false)
    private String slotNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "slot_type", nullable = false)
    private SlotType slotType;

    @Column(nullable = false)
    private boolean occupied;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id", nullable = false)
    private ParkingFloor floor;
}
