
package com.example.gps_backfill.model;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class TripResult {
    private double totalDistanceKm;
    private List<Segment> segments;
}