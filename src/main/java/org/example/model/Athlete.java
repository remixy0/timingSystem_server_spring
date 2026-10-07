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
    boolean show;

    private String ownerId;

    public Athlete() {
        this.id = UUID.randomUUID();
    }

    public Athlete(UUID id,String name, String surname, List<String> listOfEffortsId, byte[] photo, boolean show) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.listOfEffortsId = listOfEffortsId.stream()
                .map(UUID::fromString)
                .collect(Collectors.toCollection(ArrayList::new));
        this.photo = photo;
        this.show = show;
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


    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public List<UUID> getListOfEffortsId() {
        return listOfEffortsId;
    }

    public void setListOfEffortsId(List<UUID> listOfEffortsId) {
        this.listOfEffortsId = listOfEffortsId;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }
}
