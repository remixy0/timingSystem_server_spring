package org.example.model;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
public class Athlete {
    @Id
    UUID id;
    String name;
    String surname;
    List<UUID> listOfEffortsId;
    byte[] photo;

    public Athlete() {
        this.id = UUID.randomUUID();
    }

    public Athlete(UUID id,String name, String surname, List<String> listOfEffortsId, byte[] photo) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.listOfEffortsId = listOfEffortsId.stream()
                .map(UUID::fromString)
                .collect(Collectors.toCollection(ArrayList::new));
        this.photo = photo;
    }

    public void addEffort(UUID effortId){
        this.listOfEffortsId.add(effortId);
    }

    public UUID getId() {
        return id;
    }

    @Override
    public String toString(){
        return this.name + " " + this.surname;
    }


}
