package com.parking.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.UUID;

@Component
public class TicketNumberGenerator {

    public String generate() {

        // eg PARK-2026-09-10-2F84A10BCD
        return "PARK-"
                + LocalDate.now()
                + "-"
                + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }
}
