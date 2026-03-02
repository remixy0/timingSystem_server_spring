package org.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Distance {
    @Id
    UUID id;
    boolean show;
    String displayName;
    int distanceInMeters;
    Disciplines discipline;
    @OneToOne
    @JoinColumn(name = "distance_configuration_id")
    Configuration distanceConfiguration;

    public Distance() {}

    public Distance(UUID id, boolean show, String displayName, Disciplines discipline, int distanceInMeters, Configuration distanceConfiguration) {
        this.id = id;
        this.show = show;
        this.displayName = displayName;
        this.discipline = discipline;
        this.distanceInMeters = distanceInMeters;
        this.distanceConfiguration = distanceConfiguration;
    }

}
