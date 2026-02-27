package org.example.repository;
import org.example.model.Athlete;
import org.example.model.Effort;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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

    public List<Athlete> getAthletes() {
        return athletes;
    }

    public Athlete getAthleteById(UUID id) {
        for (Athlete athlete : this.athletes) {
            if (athlete.getId().equals(id)) {
                return athlete;
            }
        }
        return null;
    }

    public Effort getEffortById(UUID id) {
        for (Effort effort : this.efforts) {
            if (effort.getId().equals(id)) {
                return effort;
            }
        }
        return null;
    }


    public void addEffort(Effort effort) {
        efforts.add(effort);
    }

    public void addAthlete(Athlete athlete) {
        athletes.add(athlete);
    }

}
