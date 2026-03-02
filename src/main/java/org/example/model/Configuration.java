package org.example.model;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Configuration {
    int numberOfCrossings;
    boolean flyingMode;
    int delayBeetwenCrossings;
    boolean multipleSkaters;
    int waitingTime;
    boolean setupMode;
    boolean manualMode;

    public Configuration() {}

    public Configuration(boolean flyingMode, int numberOfCrossings, int delayBeetwenCrossings, boolean multipleSkaters, boolean setupMode, int waitingTime, boolean manualMode) {
        this.flyingMode = flyingMode;
        this.numberOfCrossings = numberOfCrossings;
        this.delayBeetwenCrossings = delayBeetwenCrossings;
        this.multipleSkaters = multipleSkaters;
        this.setupMode = setupMode;
        this.waitingTime = waitingTime;
        this.manualMode = manualMode;
    }


}
