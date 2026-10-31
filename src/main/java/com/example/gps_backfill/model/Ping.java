package com.example.gps_backfill.model;

import lombok.Data;

@Data
public class Ping {
    private double latitude;
    private double longitude;
    private long timestamp;
    private double accuracy;
}