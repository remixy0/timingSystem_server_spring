package org.example.model;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Id;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
public class Effort {
    @Id
    UUID id;
    UUID athleteId;
    UUID distanceId;
    Double totalTime;
    List<Double>  lapTimes;
    int distanceInMeters;
    LocalDate date;

    public Effort() {
        this.id = UUID.randomUUID();
        this.date = LocalDate.now();
    }

    public Effort(UUID id,UUID athleteId, UUID distanceId,Double totalTime, List<Double> lapTimes,int distanceInMeters) {
        this.id = id;
        this.athleteId = athleteId;
        this.distanceId = distanceId;
        this.totalTime = totalTime;
        if(lapTimes == null){
            this.lapTimes = new ArrayList<>();
            this.lapTimes.add(totalTime);
        }else{
            this.lapTimes = lapTimes;
        }
        this.distanceInMeters = distanceInMeters;
        this.date = LocalDate.now();
    }


    public UUID getId() {
        return id;
    }

    public UUID getAthleteId() {
        return athleteId;
    }

    public List<Double> getLapTimes(){
        return lapTimes;
    }

    public String getDate(){
        if(date == null){
            return "";
        }
        return this.date.toString();
    }

    public UUID getDistanceId() {
        return distanceId;
    }

    public Double getTotalTime() {
        return totalTime;
    }

    public Double getAverageLapTime(){
        var number = (double) Math.round(lapTimes.stream().mapToDouble(Double::doubleValue).average().orElse(0.0) * 100);
        number = number/100;
        return number;
    }

    public Double getFastestLap(){
        return lapTimes.stream()
                .mapToDouble(Double::doubleValue)
                .min()
                .orElse(0.0);
    }

    public Double getSlowestLap(){
        return lapTimes.stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
    }

    public Double calculateSpeed(){
        var number = (double) Math.round(distanceInMeters * 360 / totalTime.doubleValue());
        number = number/100;
        return number;
    }

    public int getLapBarWidth(double currentLap){
        int barWidth = (int) (getFastestLap() * 100/ currentLap);
        System.out.println(barWidth);
        return barWidth;

    }
}
