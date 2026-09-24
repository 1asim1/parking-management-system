package com.parking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;


import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "parking_floors",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_floor_number", columnNames = "floor_number")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParkingFloor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(nullable = false)
    private String name;

    @OneToMany(
            mappedBy = "floor",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ParkingSlot> slots = new ArrayList<>();
}
