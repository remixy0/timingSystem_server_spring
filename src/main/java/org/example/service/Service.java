package org.example.service;
import jakarta.transaction.Transactional;
import org.example.model.Athlete;
import org.example.model.DTOs.EffortDTOmini;
import org.example.model.Distance;
import org.example.model.Effort;
import org.example.model.DTOs.EffortDTO;
import org.example.model.UserEntity;
import org.example.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@org.springframework.stereotype.Service
public class Service {
    private final AthleteRepository athleteRepository;
    private final EffortRepository effortRepository;
    private final DistanceRepository distanceRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Service(AthleteRepository athleteRepository,EffortRepository effortRepository, DistanceRepository distanceRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.effortRepository = effortRepository;
        this.athleteRepository = athleteRepository;
        this.distanceRepository = distanceRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    public List<EffortDTO> getEffortsDTO(String userId) {
        List<EffortDTO> listOfEffortsDTO = new ArrayList<>();
        for (Effort effort : this.getEffortsForUser(userId)) {
            Double speed = (double) Math.round( distanceRepository.findById(effort.getDistanceId()).get().getDistanceInMeters() * 360 / effort.getTotalTime());
            speed = speed/100;
            listOfEffortsDTO.add(new EffortDTO(
                        effort.getId(),
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

    public List<EffortDTOmini> getEffortsDTOmini(String userId) {
        List<EffortDTOmini> listOfEffortsDTOmini = new ArrayList<>();
        for (Effort effort : this.getEffortsForUser(userId)) {
            Double speed = (double) Math.round( distanceRepository.findById(effort.getDistanceId()).get().getDistanceInMeters() * 360 / effort.getTotalTime());
            speed = speed/100;
            listOfEffortsDTOmini.add(new EffortDTOmini(
                    effort.getId(),
                    athleteRepository.findById(effort.getAthleteId()).get().toString(),
                    effort.getDate(),
                    distanceRepository.findById(effort.getDistanceId()).get().getDisplayName(),
                    effort.getTotalTime(),
                    speed.toString(),
                    effort.getAverageLapTime().toString(),
                    effort.isShow()
            ));
        }
        return listOfEffortsDTOmini.stream().sorted(Comparator.comparing(EffortDTOmini::getDate)).toList();
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
                        effort.getId(),
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
        if (effort == null || effort.getAthleteId() == null) return;

        Athlete athlete = athleteRepository.findById(effort.getAthleteId()).orElse(null);
        if (athlete == null) return;

        Effort savedEffort = effortRepository.save(effort);

        if (athlete.getListOfEffortsId() != null && !athlete.getListOfEffortsId().contains(savedEffort.getId())) {
            athlete.addEffort(savedEffort.getId());
            athleteRepository.save(athlete);
        }

        System.out.println("WYKONANO FUNKCJE DODANIA!!");
        messagingTemplate.convertAndSendToUser(
                savedEffort.getOwnerId(),
                "/queue/efforts",
                this.effortToEffortDTO(savedEffort)
        );
    }

    public EffortDTO getEffortById(UUID id, String userId) {
        Effort effort = this.getEffortsForUser(userId).stream()
                .filter(e -> e.getId().equals(id))
                .findFirst()
                .orElse(null);

        Double speed = (double) Math.round( distanceRepository.findById(effort.getDistanceId()).get().getDistanceInMeters() * 360 / effort.getTotalTime());
        speed = speed/100;
        return new EffortDTO(
                effort.getId(),
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

    public UserEntity getUserByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
        return userEntity;
    }

    public boolean addAthlete(Athlete athlete) {
        if (athlete == null || athleteRepository.existsById(athlete.getId())) return false;

        athleteRepository.save(athlete);
        return true;

    }

    public void deleteAthlete(UUID athleteId) {
        athleteRepository.deleteById(athleteId);
    }

    public List<Athlete> getAthletesForUser(String userId) {
        return athleteRepository.findAllByOwnerId(userId);
    }

    public Athlete getAthleteofId(String userId, UUID athleteId) {
        Athlete athlete =  athleteRepository.findAllByOwnerId(userId).stream()
                .filter(e -> e.getId().equals(athleteId))
                .findFirst()
                .orElse(null);
        return athlete;
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

    public boolean doesUserExist(String userId) {
        return userRepository.findByUsername(userId).isPresent();
    }

    public void saveUser(UserEntity user) {
        userRepository.save(user);
    }


    public EffortDTO effortToEffortDTO(Effort effort) {
            Double speed = (double) Math.round( distanceRepository.findById(effort.getDistanceId()).get().getDistanceInMeters() * 360 / effort.getTotalTime());
            speed = speed/100;
            return new EffortDTO(
                    effort.getId(),
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

    }



