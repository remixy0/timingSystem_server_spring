package org.example.service;

import org.example.model.DTOs.LidarDTO;
import org.example.model.Distance;
import org.example.model.Effort;
import org.example.model.LidarData;
import org.example.repository.DistanceRepository;
import org.example.repository.EffortRepository;
import org.example.repository.LidarRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class LidarService {

    /** 20 minutes at 100 samples per second. Longer captures are refused. */
    public static final int MAX_SAMPLES = 120_000;
    private static final int MAX_TEXT_LENGTH = 100;

    public enum SaveResult {
        SAVED("Added successfully!"),
        INVALID("The LiDAR data is not valid."),
        EFFORT_NOT_FOUND("There is no such effort."),
        ERROR("The LiDAR data could not be saved.");

        public final String message;

        SaveResult(String message) {
            this.message = message;
        }
    }

    private final LidarRepository lidarRepository;
    private final EffortRepository effortRepository;
    private final DistanceRepository distanceRepository;

    public LidarService(LidarRepository lidarRepository, EffortRepository effortRepository, DistanceRepository distanceRepository) {
        this.lidarRepository = lidarRepository;
        this.effortRepository = effortRepository;
        this.distanceRepository = distanceRepository;
    }

    /**
     * Saves (or replaces) the LiDAR capture of one of the user's own efforts.
     * The effort has to be on the server already, so the app uploads efforts first.
     */
    @Transactional
    public SaveResult save(LidarData lidar, String userId) {
        if (userId == null || !isValid(lidar)) return SaveResult.INVALID;

        Effort effort = effortRepository.findById(lidar.getEffortId()).orElse(null);
        // An effort of another user is reported the same way as a missing one,
        // so effort ids can't be probed
        if (effort == null || !userId.equals(effort.getOwnerId())) return SaveResult.EFFORT_NOT_FOUND;

        lidar.setOwnerId(userId);
        lidarRepository.save(lidar);
        return SaveResult.SAVED;
    }

    private static boolean isValid(LidarData lidar) {
        if (lidar == null || lidar.getEffortId() == null) return false;
        List<Integer> t = lidar.getTimeMs();
        List<Integer> d = lidar.getDistanceCm();
        List<Integer> s = lidar.getStrength();
        if (t == null || d == null || s == null) return false;
        if (t.isEmpty() || t.size() > MAX_SAMPLES) return false;
        if (t.size() != d.size() || t.size() != s.size()) return false;
        if (t.contains(null) || d.contains(null) || s.contains(null)) return false;
        if (lidar.getDeviceName() != null && lidar.getDeviceName().length() > MAX_TEXT_LENGTH) return false;
        if (lidar.getStartedAt() != null && lidar.getStartedAt().length() > MAX_TEXT_LENGTH) return false;
        if (Double.isNaN(lidar.getTimerOffset()) || Double.isInfinite(lidar.getTimerOffset())) return false;
        return true;
    }

    /** The capture of an effort that belongs to ownerId, or null when there is none. */
    @Transactional(readOnly = true)
    public LidarDTO getForEffort(UUID effortId, String ownerId) {
        if (effortId == null || ownerId == null) return null;

        LidarData lidar = lidarRepository.findByEffortIdAndOwnerId(effortId, ownerId).orElse(null);
        if (lidar == null) return null;

        Effort effort = effortRepository.findById(effortId)
                .filter(e -> ownerId.equals(e.getOwnerId()))
                .orElse(null);
        if (effort == null) return null;

        Distance distance = effort.getDistanceId() == null
                ? null
                : distanceRepository.findById(effort.getDistanceId()).orElse(null);
        boolean flyingMode = distance != null
                && distance.getDistanceConfiguration() != null
                && distance.getDistanceConfiguration().isFlyingMode();

        return new LidarDTO(
                lidar.getEffortId(),
                lidar.getFormatVersion(),
                lidar.getDeviceName(),
                lidar.getStartedAt(),
                lidar.getTimeMs(),
                lidar.getDistanceCm(),
                lidar.getStrength(),
                lidar.getLostPackets(),
                lidar.isEndedByLidar(),
                lidar.getTimerOffset(),
                lidar.isSawStart(),
                effort.getTotalTime(),
                distance != null ? distance.getDistanceInMeters() : 0,
                distance != null ? distance.getDisplayName() : "",
                flyingMode
        );
    }

    /** Ids of the user's efforts that have a LiDAR capture. */
    public List<UUID> getEffortIdsWithLidar(String ownerId) {
        return lidarRepository.findEffortIdsByOwnerId(ownerId);
    }

    /** Deletes the capture of one of the user's own efforts. False when there was none. */
    @Transactional
    public boolean delete(UUID effortId, String userId) {
        if (effortId == null || userId == null) return false;
        LidarData lidar = lidarRepository.findByEffortIdAndOwnerId(effortId, userId).orElse(null);
        if (lidar == null) return false;
        lidarRepository.delete(lidar);
        return true;
    }
}
