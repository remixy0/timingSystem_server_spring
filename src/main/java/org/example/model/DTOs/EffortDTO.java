package org.example.model.DTOs;

import java.util.List;

public class EffortDTO {

    private String athleteName; // Zgadza się z Reactem!
    private String date;
    private String distance;
    private Double totalTime;
    private String speed;
    private String averageLapTime;
    private List<Double> lapTimes;

    public EffortDTO(String athleteName, String date, String distance, Double totalTime, String speed, String averageLapTime, List<Double> lapTimes) {
        this.athleteName = athleteName;
        this.date = date;
        this.distance = distance;
        this.totalTime = totalTime;
        this.speed = speed;
        this.averageLapTime = averageLapTime;
        this.lapTimes = lapTimes;
    }

    public String getDate() {
        return date;
    }

    public List<Double> getLapTimes() {
        return lapTimes;
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

}
