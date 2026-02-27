package org.example.service;
import org.example.model.Athlete;
import org.example.model.Effort;
import org.example.model.DTOs.EffortDTO;
import org.example.repository.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;



public class Service {
    Repository repository;

    public Service() {
        this.repository = new Repository();
    }

    public List<EffortDTO> getEffortsDTO() {
        List<EffortDTO> listOfEffortsDTO = new ArrayList<>();
        for (Effort effort : this.repository.getEfforts()) {
            listOfEffortsDTO.add(new EffortDTO(
                    repository.getAthleteById(effort.getAthleteId()).getFullName(),
                    effort.getDate(),
                    effort.getDistance(),
                    effort.getTotalTime(),
                    effort.calculateSpeed().toString(),
                    effort.getAverageLapTime().toString(),
                    effort.getLapTimes()));
        }
        return listOfEffortsDTO;
    }

    public void addEffort(Effort effort) {
        if (effort != null && repository.getEffortById(effort.getId()) == null) {
            repository.addEffort(effort);
            Athlete athlete = repository.getAthleteById(effort.getAthleteId());
            if (athlete != null) {
                athlete.addEffort(effort.getId());
            }
        }
    }

    public void addAthlete(Athlete athlete) {
        if (athlete != null && repository.getAthleteById(athlete.getId()) == null) {
            repository.addAthlete(athlete);
        }
    }

    public List<Effort> getEffortsByAthlete(Athlete athlete) {
        List<Effort> listOfEfforts = new ArrayList<>();
        for(UUID id : athlete.getEffortsId()) {
            repository.getEfforts().stream().filter(x -> x.getId().equals(id)).findFirst().ifPresent(listOfEfforts::add);
        }
        return listOfEfforts;
    }

    public List<Athlete> getAthletes(){
        return repository.getAthletes();
    }



}
