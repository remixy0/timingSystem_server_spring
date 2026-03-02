package org.example.service;
import jakarta.transaction.Transactional;
import org.example.model.Athlete;
import org.example.model.Effort;
import org.example.model.DTOs.EffortDTO;
import org.example.repository.AthleteRepository;
import org.example.repository.EffortRepository;
import org.example.repository.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Service {
    Repository repository;
    private final AthleteRepository athleteRepository;
    private final EffortRepository effortRepository;



    public Service(AthleteRepository athleteRepository,EffortRepository effortRepository) {
        this.effortRepository = effortRepository;
        this.athleteRepository = athleteRepository;
        this.repository = new Repository();
    }

    public List<EffortDTO> getEffortsDTO() {
        List<EffortDTO> listOfEffortsDTO = new ArrayList<>();
        for (Effort effort : this.effortRepository.findAll()) {
            listOfEffortsDTO.add(new EffortDTO(
                    athleteRepository.findById(effort.getAthleteId()).get().toString(),
                    effort.getDate(),
                    effort.getDistance(),
                    effort.getTotalTime(),
                    effort.calculateSpeed().toString(),
                    effort.getAverageLapTime().toString(),
                    effort.getLapTimes()));
        }
        return listOfEffortsDTO;
    }


    public List<EffortDTO> getEffortsDTOofAthlete(UUID athleteId) {
        System.out.println("athlete Id: " + athleteId);
        List<EffortDTO> listOfEffortsDTO = new ArrayList<>();
        for (UUID effortId : this.athleteRepository.findById(athleteId).get().getListOfEffortsId()) {
            Effort effort = this.effortRepository.findById(effortId).get();
            System.out.println(effort);
            listOfEffortsDTO.add(new EffortDTO(
                    athleteRepository.findById(effort.getAthleteId()).get().toString(),
                    effort.getDate(),
                    effort.getDistance(),
                    effort.getTotalTime(),
                    effort.calculateSpeed().toString(),
                    effort.getAverageLapTime().toString(),
                    effort.getLapTimes()));
        }
        return listOfEffortsDTO;
    }


    @Transactional
    public void addEffort(Effort effort) {
        if (effort != null && athleteRepository.findById(effort.getAthleteId()) != null) {
            effortRepository.save(effort);
            Athlete athlete = athleteRepository.findById(effort.getAthleteId()).orElse(null);
            if (athlete != null) {
                athlete.addEffort(effort.getId());
                athleteRepository.save(athlete);
            }
        }
    }

    public void addAthlete(Athlete athlete) {
        if (athlete != null && repository.getAthleteById(athlete.getId()) == null) {
            athleteRepository.save(athlete);
        }
    }

    public List<Athlete> getAthletes(){
        return athleteRepository.findAll();
    }

//    public List<Effort> getEffortsByAthlete(Athlete athlete) {
//        List<Effort> listOfEfforts = new ArrayList<>();
//        for(UUID id : athlete.getEffortsId()) {
//            repository.getEfforts().stream().filter(x -> x.getId().equals(id)).findFirst().ifPresent(listOfEfforts::add);
//        }
//        return listOfEfforts;
//    }





}