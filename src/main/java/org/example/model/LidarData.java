package org.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

/**
 * The raw LiDAR (TF02-Pro) capture of one effort, exactly as the iOS app recorded it.
 *
 * One capture per effort: the effort's id is the primary key.
 * The three lists have the same length; index i is one sample (100 samples per second).
 * Time 0 is the moment the timer started, so timeMs lines up with the effort's lap times.
 *
 * Only the raw measurements are stored. Position, speed, splits and reaction time are
 * calculated from them by the client (same filter as in the iOS app).
 */
@Getter
@Setter
@Entity
@Table(name = "lidar_data")
public class LidarData {
    @Id
    private UUID effortId;

    private String ownerId;

    /** Version of the capture format (1 for now) */
    private int formatVersion = 1;
    /** Bluetooth name of the LiDAR board */
    private String deviceName;
    /** When the capture started, same text format as Effort.date (2026-03-14T22:26:17+0100) */
    private String startedAt;

    /** Milliseconds since the timer started */
    private List<Integer> timeMs;
    /** Distance to the athlete in centimetres */
    private List<Integer> distanceCm;
    /** Signal strength (below ~100 the distance is unreliable, 65535 = saturated) */
    private List<Integer> strength;

    /** Bluetooth packets that never arrived */
    private int lostPackets;
    /** The LiDAR stopped by itself because the athlete went beyond 30 m */
    private boolean endedByLidar;
    /** (LiDAR start to finish, as seen by the phone) - (timer's total time). Close to 0 when everything lines up. */
    private double timerOffset;
    /** False when the app joined a capture that was already running */
    private boolean sawStart = true;

    public LidarData() {}
}
