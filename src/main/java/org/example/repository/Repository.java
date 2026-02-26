package org.example.repository;

import org.example.model.Athlete;
import org.example.model.Effort;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    List<Effort> efforts;
    List<Athlete> athletes;

    public Repository() {
        this.efforts = new ArrayList<>();
        this.athletes = new ArrayList<>();
    }

    public List<Effort> getEfforts() {
        return efforts;
    }

    public List<Effort> getAthletes() {
        return athletes;
    }

    public void addEffort(Effort effort) {
        efforts.add(effort);
    }

    public void addAthlete(Athlete athlete) {
        athletes.add(athlete);
    }

}
