package org.example.model.DTOs;
import java.util.List;
import java.util.UUID;

public class EffortDTOmini{

    private UUID id;
    private String athleteName;
    private String date;
    private String distance;
    private Double totalTime;
    private String speed;
    private String averageLapTime;
    boolean show;

    public EffortDTOmini(UUID id,String athleteName, String date, String distance, Double totalTime, String speed, String averageLapTime, boolean show) {
        this.id = id;
        this.athleteName = athleteName;
        this.date = date;
        this.distance = distance;
        this.totalTime = totalTime;
        this.speed = speed;
        this.averageLapTime = averageLapTime;
        this.show = show;
    }

    public UUID getId() {
        return id;
    }
    public String getDate() {
        return date;
    }

    public String getAverageLapTime() {
        return averageLapTime;
    }

    public String getSpeed() {
        return speed;
    }

    public Double getTotalTime() {
        return totalTime;
    }

    public String getDistance() {
        return distance;
    }

    public String getAthleteName() {
        return athleteName;
    }

    public boolean isShow() {
        return show;
    }
}
