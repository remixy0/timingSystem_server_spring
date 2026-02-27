package org.example.model;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Athlete {
    UUID id;
    String name;
    String surname;
    List<UUID> listOfEffortsId;

    public Athlete(UUID id, String name, String surname, List<String> listOfEffortsId) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.listOfEffortsId = listOfEffortsId.stream()
                .map(UUID::fromString)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public void addEffort(UUID effortId){
        this.listOfEffortsId.add(effortId);
    }

    public UUID getId() {
        return id;
    }

    public List<UUID> getEffortsId() {
        return this.listOfEffortsId;
    }

    public String getFullName() {
        return this.surname + " " + this.name;
    }
}
