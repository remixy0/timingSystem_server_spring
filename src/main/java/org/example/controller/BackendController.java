package org.example.controller;
import org.example.model.Athlete;
import org.example.model.DTOs.EffortDTO;
import org.example.model.Distance;
import org.example.model.Effort;
import org.example.repository.AthleteRepository;
import org.example.repository.DistanceRepository;
import org.example.repository.EffortRepository;
import org.example.service.Service;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class BackendController {
    private final EffortRepository effortRepository;
    private final AthleteRepository athleteRepository;
    private final DistanceRepository distanceRepository;
    private final Service service;

    public BackendController(AthleteRepository athleteRepository, EffortRepository effortRepository, DistanceRepository distanceRepository) {
        this.distanceRepository = distanceRepository;
        this.effortRepository = effortRepository;
        this.athleteRepository = athleteRepository;
        this.service = new Service(athleteRepository, effortRepository, distanceRepository);
    }

    //efforts
    @GetMapping("/api/get-efforts-dto")
    public List<EffortDTO> getEffortsDTO() {
        return service.getEffortsDTO();
    }

    @GetMapping("/api/get-efforts")
    public List<Effort> getEfforts() {
        return service.getEfforts();
    }

    @GetMapping("/api/get-efforts-of-athlete-id")
    public List<EffortDTO> getEffortsOfAthleteId(@RequestParam UUID athleteId) {
        return service.getEffortsDTOofAthlete(athleteId);
    }

    @PostMapping("/api/add-effort")
    public String addNewEffort(@RequestBody Effort effort) {
        service.addEffort(effort);
        return "added new effort";
    }

    @PostMapping("/api/add-efforts")
    public String addListOfEfforts(@RequestBody List<Effort> efforts) {
        efforts.stream().forEach(effort -> service.addEffort(effort));
        return "added list of efforts";
    }


    //athletes
    @GetMapping("/api/get-athletes")
    public List<Athlete> getAthletes() {
        return service.getAthletes();
    }

    @PostMapping("/api/add-athlete")
    public String addNewAthlete(@RequestBody Athlete athlete) {
        service.addAthlete(athlete);
        return "added new athlete";
    }

    @PostMapping("/api/add-athletes")
    public String addListOfAthletes(@RequestBody List<Athlete> athletes) {
        athletes.stream().forEach(athlete -> service.addAthlete(athlete));
        return "added list of athletes";
    }


    //Distance
    @PostMapping("/api/add-distance")
    public String addNewAthlete(@RequestBody Distance distance) {
        service.addDistance(distance);
        return "added new distance";
    }

    @PostMapping("/api/add-distances")
    public String addNewAthlete(@RequestBody List<Distance> distances) {
        distances.stream().forEach(distance -> service.addDistance(distance));
        return "added list of distances";
    }

    @GetMapping("/api/get-distances")
    public List<Distance> getDistances() {
        return service.getDistances();
    }


}

