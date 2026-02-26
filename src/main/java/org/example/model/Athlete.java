package org.example.model;

import org.example.repository.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Athlete {
    UUID id;
    String name;
    String surname;
    List<UUID> listOfEffortsId;

    public Athlete(String id, String name, String surname, List<UUID> listOfEffortsId) {
        this.id = UUID.fromString(id);
        this.name = name;
        this.surname = surname;
        this.listOfEffortsId = listOfEffortsId;
    }

    public void addEffort(UUID effortId){
        this.listOfEffortsId.add(effortId);
    }

    public List<UUID> getEffortsId() {
        return this.listOfEffortsId;
    }

}
