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
    List<Double>  lapTimes;
    Double totalTime;

    public Effort(String name, String surname, int Id, List<Double> lapTimes, Double totalTime) {
        this.name = name;
        this.surname = surname;
        this.Id = Id;
        this.lapTimes = lapTimes;
        this.totalTime = totalTime;
    }
}
