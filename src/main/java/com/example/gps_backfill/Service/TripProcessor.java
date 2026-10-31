package com.example.gps_backfill.Service;

import com.example.gps_backfill.model.Ping;
import com.example.gps_backfill.model.Segment;
import com.example.gps_backfill.model.TripResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TripProcessor {
    private static final double MAX_SPEED_KMH = 120.0;
    private static final double PARKED_SPEED_KMH = 5.0;
    private static final long SHORT_GAP_SECONDS = 70;

    // Updated to accept the RoutingService
    public TripResult processTrip(List<Ping> pings, RoutingService routingService) {
        double totalDistanceKm = 0.0;
        List<Segment> segments = new ArrayList<>();

        if (pings == null || pings.size() < 2) {
            return new TripResult(totalDistanceKm, segments);
        }

        pings.sort(Comparator.comparingLong(Ping::getTimestamp));
        List<Ping> cleanedPings = new ArrayList<>();
        cleanedPings.add(pings.get(0));

        for (int i = 1; i < pings.size(); i++) {
            if (pings.get(i).getTimestamp() != pings.get(i - 1).getTimestamp()) {
                cleanedPings.add(pings.get(i));
            }
        }

        Ping prev = cleanedPings.get(0);
        for (int i = 1; i < cleanedPings.size(); i++) {
            Ping curr = cleanedPings.get(i);
            long timeDeltaSeconds = curr.getTimestamp() - prev.getTimestamp();
            
            if (timeDeltaSeconds == 0) continue;

            double haversineDist = calculateHaversine(prev, curr);
            double speedKmh = (haversineDist / (timeDeltaSeconds / 3600.0));

            if (speedKmh > MAX_SPEED_KMH) {
                continue; 
            }

            if (timeDeltaSeconds < SHORT_GAP_SECONDS || speedKmh < PARKED_SPEED_KMH) {
                totalDistanceKm += haversineDist;
                segments.add(new Segment(prev, curr, haversineDist, "SHORT_GAP_AERIAL"));
            } else {
                // Now calling the external service through the Circuit Breaker & Redis Cache
                double routedDist = routingService.getRouteDistance(prev, curr); 
                totalDistanceKm += routedDist;
                segments.add(new Segment(prev, curr, routedDist, "ROUTED_API"));
            }
            prev = curr;
        }

        return new TripResult(totalDistanceKm, segments);
    }

    private double calculateHaversine(Ping p1, Ping p2) {
        final int R = 6371; 
        double latDistance = Math.toRadians(p2.getLatitude() - p1.getLatitude());
        double lonDistance = Math.toRadians(p2.getLongitude() - p1.getLongitude());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(p1.getLatitude())) * Math.cos(Math.toRadians(p2.getLatitude()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}