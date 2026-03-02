package org.example.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@Setter
@Data
public class Distance {
    @Id
    UUID id;
    boolean show;
    String displayName;
    int distanceInMeters;
    Disciplines discipline;
    @JdbcTypeCode(SqlTypes.JSON)
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

    @Override
    public String toString() {
        return displayName;
    }

}
