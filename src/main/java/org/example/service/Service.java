package org.example.service;
import jakarta.transaction.Transactional;
import org.example.model.Athlete;
import org.example.model.Distance;
import org.example.model.Effort;
import org.example.model.DTOs.EffortDTO;
import org.example.repository.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Service {
    Repository repository;
    private final AthleteRepository athleteRepository;
    private final EffortRepository effortRepository;
    private final DistanceRepository distanceRepository;

    public Service(AthleteRepository athleteRepository,EffortRepository effortRepository, DistanceRepository distanceRepository) {
        this.effortRepository = effortRepository;
        this.athleteRepository = athleteRepository;
        this.distanceRepository = distanceRepository;
        this.repository = new Repository();
    }

    public List<EffortDTO> getEffortsDTO() {
        List<EffortDTO> listOfEffortsDTO = new ArrayList<>();
        for (Effort effort : this.effortRepository.findAll()) {
            listOfEffortsDTO.add(new EffortDTO(
                    athleteRepository.findById(effort.getAthleteId()).get().toString(),
                    effort.getDate(),
//                    distanceRepository.findById(effort.getDistanceId()).get().toString(),
                    "test distance",
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
//                    distanceRepository.findById(effort.getDistanceId()).get().toString(),
                    "test distance",
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

    public void addDistance(Distance distance) {
        distanceRepository.save(distance);
    }

    public List<Distance> getDistances() {
        return distanceRepository.findAll();
    }




}