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


    public Configuration(boolean flyingMode, int numberOfCrossings, int delayBeetwenCrossings, boolean multipleSkaters,int waitingTime) {
        this.flyingMode = flyingMode;
        this.numberOfCrossings = numberOfCrossings;
        this.delayBeetwenCrossings = delayBeetwenCrossings;
        this.multipleSkaters = multipleSkaters;
        this.waitingTime = waitingTime;
    }


}
