package com.example.gps_backfill.controller;

import com.example.gps_backfill.model.Ping;
import com.example.gps_backfill.model.TripResult;
import com.example.gps_backfill.Service.TripProcessor;
import com.example.gps_backfill.Service.RoutingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripProcessor tripProcessor;
    private final RoutingService routingService;

    // Inject RoutingService here
    public TripController(RoutingService routingService) {
        this.tripProcessor = new TripProcessor();
        this.routingService = routingService;
    }

    @PostMapping("/process")
    public ResponseEntity<TripResult> processTrip(@RequestBody List<Ping> pings) {
        if (pings == null || pings.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Pass the injected service into the plain Java algorithm
        TripResult result = tripProcessor.processTrip(pings, routingService);
        return ResponseEntity.ok(result);
    }
}