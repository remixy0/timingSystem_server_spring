package org.example.model.DTOs;

import java.util.List;
import java.util.UUID;

/**
 * What GET /api/get-lidar returns: the raw capture plus the few things about the effort
 * that are needed to analyse it (total time, distance, flying start).
 */
public record LidarDTO(
        UUID effortId,
        int formatVersion,
        String deviceName,
        String startedAt,
        List<Integer> timeMs,
        List<Integer> distanceCm,
        List<Integer> strength,
        int lostPackets,
        boolean endedByLidar,
        double timerOffset,
        boolean sawStart,
        Double totalTime,
        int distanceInMeters,
        String distanceName,
        boolean flyingMode
) {}
