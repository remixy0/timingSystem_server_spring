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

    private String ownerId;

    public Distance() {}

    public Distance(UUID id, boolean show, String displayName, int distanceInMeters, Disciplines discipline, Configuration distanceConfiguration) {
        this.id = id;
        this.show = show;
        this.displayName = displayName;
        this.distanceInMeters = distanceInMeters;
        this.discipline = discipline;
        this.distanceConfiguration = distanceConfiguration;
    }

    @Override
    public String toString() {
        return displayName;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getDistanceInMeters() {
        return distanceInMeters;
    }

    public void setDistanceInMeters(int distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }

    public Disciplines getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Disciplines discipline) {
        this.discipline = discipline;
    }

    public Configuration getDistanceConfiguration() {
        return distanceConfiguration;
    }

    public void setDistanceConfiguration(Configuration distanceConfiguration) {
        this.distanceConfiguration = distanceConfiguration;
    }
}
