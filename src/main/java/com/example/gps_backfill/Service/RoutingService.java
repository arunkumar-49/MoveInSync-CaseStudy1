package com.example.gps_backfill.Service;

import com.example.gps_backfill.model.Ping;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class RoutingService {

    // Caches the result in Redis using the coordinates as a unique key
    @Cacheable(value = "routeCache", key = "#p1.latitude + '-' + #p1.longitude + '-' + #p2.latitude + '-' + #p2.longitude")
    @CircuitBreaker(name = "routingService", fallbackMethod = "fallbackDistance")
    public double getRouteDistance(Ping p1, Ping p2) {
        // Simulate an external API that randomly fails 20% of the time to trigger the circuit breaker
        if (Math.random() > 0.8) {
            throw new RuntimeException("External routing provider timeout");
        }
        return calculateHaversine(p1, p2) * 1.3; // Standard routed distance mock
    }

    // Fallback method called when the external API throws an exception or the circuit is OPEN
    public double fallbackDistance(Ping p1, Ping p2, Throwable t) {
        return calculateHaversine(p1, p2) * 1.1; // Penalized aerial distance fallback
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