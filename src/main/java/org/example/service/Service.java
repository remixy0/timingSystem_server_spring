package org.example.service;
import jakarta.transaction.Transactional;
import org.example.model.Athlete;
import org.example.model.Distance;
import org.example.model.Effort;
import org.example.model.DTOs.EffortDTO;
import org.example.model.UserEntity;
import org.example.repository.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@org.springframework.stereotype.Service
public class Service {
    Repository repository;
    private final AthleteRepository athleteRepository;
    private final EffortRepository effortRepository;
    private final DistanceRepository distanceRepository;
    private final UserRepository userRepository;

    public Service(AthleteRepository athleteRepository,EffortRepository effortRepository, DistanceRepository distanceRepository, UserRepository userRepository) {
        this.effortRepository = effortRepository;
        this.athleteRepository = athleteRepository;
        this.distanceRepository = distanceRepository;
        this.userRepository = userRepository;
        this.repository = new Repository();
    }

    public List<EffortDTO> getEffortsDTO(String userId) {
        List<EffortDTO> listOfEffortsDTO = new ArrayList<>();
        for (Effort effort : this.getEffortsForUser(userId)) {
            Double speed = (double) Math.round( distanceRepository.findById(effort.getDistanceId()).get().getDistanceInMeters() * 360 / effort.getTotalTime());
            speed = speed/100;
            listOfEffortsDTO.add(new EffortDTO(
                        athleteRepository.findById(effort.getAthleteId()).get().toString(),
                        effort.getDate(),
                        distanceRepository.findById(effort.getDistanceId()).get().getDisplayName(),
                        effort.getTotalTime(),
                        speed.toString(),
                        effort.getAverageLapTime().toString(),
                        effort.getLapTimes(),
                        effort.isShow()
            ));
        }
        return listOfEffortsDTO.stream().sorted(Comparator.comparing(EffortDTO::getDate)).toList();
    }

    public List<EffortDTO> getEffortsDTOofAthlete(UUID athleteId, String userId) {
        System.out.println("athlete Id: " + athleteId);
        List<EffortDTO> listOfEffortsDTO = new ArrayList<>();
        Athlete athlete = athleteRepository.findAllByOwnerId(userId).stream().filter(x -> x.getId().equals(athleteId)).findFirst().orElse(null);
        for (UUID effortId : athlete.getListOfEffortsId()) {
            Effort effort = this.effortRepository.findById(effortId).get();
            Double speed = (double) Math.round(distanceRepository.findById(effort.getDistanceId()).get().getDistanceInMeters() * 360 / effort.getTotalTime());
            speed = speed / 100;
            if(effort.isShow()){
                listOfEffortsDTO.add(new EffortDTO(
                        athleteRepository.findById(effort.getAthleteId()).get().toString(),
                        effort.getDate(),
                        distanceRepository.findById(effort.getDistanceId()).get().getDisplayName(),
                        effort.getTotalTime(),
                        speed.toString(),
                        effort.getAverageLapTime().toString(),
                        effort.getLapTimes(),
                        effort.isShow()

                ));
            }
        }
        return listOfEffortsDTO;
    }

    @Transactional
    public void addEffort(Effort effort) {
        if (effort != null && athleteRepository.findById(effort.getAthleteId()) != null) {

            effortRepository.save(effort);
            Athlete athlete = athleteRepository.findById(effort.getAthleteId()).orElse(null);
            if (athlete != null && !athlete.getListOfEffortsId().contains(effort.getAthleteId())) {
                athlete.addEffort(effort.getId());
                athleteRepository.save(athlete);
            }
        }
    }

    public EffortDTO getEffortById(UUID id, String userId) {
        Effort effort = this.getEffortsForUser(userId).stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);

        Double speed = (double) Math.round( distanceRepository.findById(effort.getDistanceId()).get().getDistanceInMeters() * 360 / effort.getTotalTime());
        speed = speed/100;
        return new EffortDTO(
                athleteRepository.findById(effort.getAthleteId()).get().toString(),
                effort.getDate(),
                distanceRepository.findById(effort.getDistanceId()).get().getDisplayName(),
                effort.getTotalTime(),
                speed.toString(),
                effort.getAverageLapTime().toString(),
                effort.getLapTimes(),
                effort.isShow()

        );
    }


    public UserEntity getUserById(String userId) {
        UserEntity userEntity = userRepository.findByUsername(userId).orElse(null);
        return userEntity;
    }

    public void addAthlete(Athlete athlete) {
        if (athlete != null && repository.getAthleteById(athlete.getId()) == null) {
            athleteRepository.save(athlete);
        }
    }

    public void deleteAthlete(UUID athleteId) {
        athleteRepository.deleteById(athleteId);
    }

    public List<Athlete> getAthletesForUser(String userId) {
        return athleteRepository.findAllByOwnerId(userId);
    }

    public void addDistance(Distance distance) {
        distanceRepository.save(distance);
    }

    public List<Distance> getDistancesForUser(String userId) {
        return distanceRepository.findAllByOwnerId(userId);
    }

    public List<Effort> getEfforts() {
        return effortRepository.findAll();
    }

    public List<Effort> getEffortsForUser(String userID){
       return effortRepository.findAllByOwnerId(userID);
    }



}