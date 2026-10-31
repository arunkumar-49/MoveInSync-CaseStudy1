package com.example.gps_backfill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import java.util.TimeZone;

@SpringBootApplication
@EnableCaching
public class GpsBackfillApplication {
    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(GpsBackfillApplication.class, args);
    }
}