package org.example.model;

import lombok.Getter;
import lombok.Setter;

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

    public Effort(String name, String surname,String distance,List<Double> lapTimes, Double totalTime) {
        this.name = name;
        this.surname = surname;
        this.distance = distance;
        this.lapTimes = lapTimes;
        this.totalTime = totalTime;
    }

    public String getLapTimes(){
        String lapTimesString = "";
        for(Double lapTime: lapTimes){
            lapTimesString = lapTimesString +  "  " + lapTime;
        }
        return lapTimesString;
    }
}
