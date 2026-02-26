package org.example.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Effort {

    UUID Id;
    UUID athlete;
    String distance;
    Double totalTime;
    List<Double>  lapTimes;
    int distanceInMeters;
    LocalDate date;

    public Effort(String Id,String athleteId, String distance,Double totalTime, List<Double> lapTimes,int distanceInMeters) {
        this.Id = UUID.fromString(Id);
        this.athlete = UUID.fromString(athleteId);
        this.distance = distance;
        this.totalTime = totalTime;
        if(lapTimes == null){
            this.lapTimes = new ArrayList<>();
            this.lapTimes.add(totalTime);
        }else{
            this.lapTimes = lapTimes;
        }
        this.distanceInMeters = distanceInMeters;
        this.date = LocalDate.now();
        System.out.println(athlete + " " + distance + " " + totalTime + " " + lapTimes + " " + distanceInMeters);
    }

    private int distanceInMeters(){
        String var = "";
        for(int i=0;i<distance.length();i++){
            char c = distance.charAt(i);
            if(Character.isDigit(c)){
                var += c;
            }
        }
        return Integer.parseInt(var);
    }

    public UUID getId() {
        return Id;
    }

    public UUID getAthlete() {
        return athlete;
    }

    public List<Double> getLapTimes(){
        return lapTimes;
    }

    public String getDate(){
        return date.toString();
    }

    public String getDistance() {
        return distance;
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
        var number = (double) Math.round(distanceInMeters() * 360 / totalTime.doubleValue());
        number = number/100;
        return number;
    }

    public int getLapBarWidth(double currentLap){
        int barWidth = (int) (getFastestLap() * 100/ currentLap);
        System.out.println(barWidth);
        return barWidth;

    }
}
