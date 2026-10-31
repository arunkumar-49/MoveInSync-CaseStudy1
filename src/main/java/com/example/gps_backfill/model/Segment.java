package com.example.gps_backfill.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Segment {
    private Ping startPing;
    private Ping endPing;
    private double distanceKm;
    private String ruleApplied; // e.g., "SHORT_GAP_AERIAL", "ROUTED"
}