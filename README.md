# MoveInSync Backend Case Study 2026: GPS Gap Backfill & Billable Distance Reconstruction

## Overview
Vendors who supply cabs to MoveInSync are paid per kilometre, meaning trip distance directly correlates with cost[cite: 8]. Because GPS devices suffer from tunnels, basement parking, dead batteries, and late pings, naive distance sums underpay vendors, while querying external maps APIs for every gap is expensive[cite: 8]. 

This project implements a robust, production-grade **GPS gap backfill and billable distance reconstruction service** built with **Java 25 and Spring Boot**, featuring intelligent stream cleaning, anchor point patching, Redis caching, and Resilience4j circuit breakers.

---

## Architecture & Core Modules

1. **Stream Cleaning**:
   - Drops duplicate pings, low-accuracy fixes, and out-of-bounds geographic coordinates.
   - Filters out "teleport" jumps where implied speed exceeds a strict configurable limit ($120\text{ km/h}$)[cite: 8].
2. **Anchor Point Patching**:
   - Integrates employee pickup/drop anchors, no-show locations, and office boundary validation ($\le 400\text{ m}$ rule)[cite: 8].
3. **Distance Calculation & Segments**:
   - **Short Gaps / Parked Vehicles**: Utilizes Haversine great-circle distance for consecutive points $<70$ seconds apart or speeds $<5\text{ km/h}$[cite: 8].
   - **Real Gaps**: Interfaces with routing providers, validating thresholds to reject unrealistic loops or provider spikes[cite: 8].
4. **Resilience & Caching**:
   - **Redis Caching**: Caches snapped start/end cell routing results to minimize redundant third-party API costs[cite: 9, 10].
   - **Resilience4j Circuit Breaker**: Trips on provider failure, falling back automatically to a penalized straight-line distance ($\times 1.4$) and marking trips as `DEGRADED`.

---

## Complexity Analysis & Performance Benchmarks

* **Time Complexity**: $\mathcal{O}(N \log N)$ due to chronological sorting of raw device pings to manage out-of-order data, followed by an $\mathcal{O}(N)$ linear scan and interpolation pass.
* **Space Complexity**: $\mathcal{O}(N)$ scaling linearly with the input size plus reconstructed intermediate points.
* **Throughput & Load**: Designed to handle high-density streams of 1,500+ pings per trip with sub-millisecond route calculations when cached.

---

## Setup & Execution (One-Command Run)

### Prerequisites
* Java 25 & Maven
* Docker & Docker Compose

### Step-by-Step Execution Guide

1. **Start Infrastructure Services (PostgreSQL & Redis)**:
   Open a terminal in your project root and start the containerized databases:
   ```bash
   docker compose up -d
