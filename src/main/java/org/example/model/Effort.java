package org.example.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Effort {
    String name, surname;
    int Id;
    String distance;
    List<Double>  lapTimes;
    Double totalTime;
    int distanceInMeters;
    LocalDate date;

    public Effort(String name, String surname,String distance,List<Double> lapTimes, Double totalTime) {
        this.name = name;
        this.surname = surname;
        this.distance = distance;
        this.lapTimes = lapTimes;
        this.totalTime = totalTime;
        date = LocalDate.now();
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

    public List<Double> getLapTimes(){
        return lapTimes;
    }

    public String getDate(){
        return date.toString();
    }


//    public String getLapTimes(){
//        String lapTimesString = "";
//        for(Double lapTime: lapTimes){
//            lapTimesString = lapTimesString +  "  " + lapTime;
//        }
//        return lapTimesString;
//    }

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
