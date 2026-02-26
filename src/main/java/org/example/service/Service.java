package org.example.service;

import org.example.model.Athlete;
import org.example.model.Effort;
import org.example.repository.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Service {
    Repository repository;

    public Service() {
        this.repository = new Repository();
    }

    public List<Effort> getEfforts() {
        return this.repository.getEfforts();
    }

    public void addEffort(Effort effort) {
        repository.addEffort(effort);
        Athlete athlete = (Athlete) repository.getAthletes().stream().filter(x -> x.getId().equals(effort.getAthlete()));
        athlete.addEffort(effort.getId());
    }

    public void addAthlete(Athlete athlete) {
        repository.addAthlete(athlete);
    }

    public List<Effort> getEffortsByAthlete(Athlete athlete) {
        List<Effort> listOfEfforts = new ArrayList<>();
        for(UUID id : athlete.getEffortsId()) {
            repository.getEfforts().stream().filter(x -> x.getId().equals(id)).findFirst().ifPresent(listOfEfforts::add);
        }
        return listOfEfforts;
    }


}
