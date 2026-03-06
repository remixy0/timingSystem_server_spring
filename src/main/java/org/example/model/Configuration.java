package org.example.model;

import lombok.*;


@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Configuration {
    int numberOfCrossings;
    boolean flyingMode;
    int delayBeetwenCrossings;
    boolean multipleSkaters;
    int waitingTime;
    boolean setupMode;
    boolean manualMode;


    public Configuration(boolean flyingMode, int numberOfCrossings, int delayBeetwenCrossings, boolean multipleSkaters,int waitingTime, boolean setupMode, boolean manualMode) {
        this.flyingMode = flyingMode;
        this.numberOfCrossings = numberOfCrossings;
        this.delayBeetwenCrossings = delayBeetwenCrossings;
        this.multipleSkaters = multipleSkaters;
        this.waitingTime = waitingTime;
        this.setupMode = setupMode;
        this.manualMode = manualMode;
    }


}
