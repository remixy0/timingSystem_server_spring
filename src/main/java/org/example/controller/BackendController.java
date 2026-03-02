package org.example.controller;
import org.example.model.Athlete;
import org.example.model.DTOs.EffortDTO;
import org.example.model.Distance;
import org.example.model.Effort;
import org.example.repository.AthleteRepository;
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
    private final Service service;

    public BackendController(AthleteRepository athleteRepository, EffortRepository effortRepository) {
        this.effortRepository = effortRepository;
        this.athleteRepository = athleteRepository;
        this.service = new Service(athleteRepository, effortRepository);
    }

    //efforts
    @GetMapping("/api/get-efforts")
    public List<EffortDTO> getEfforts() {
        return service.getEffortsDTO();
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


    //Distance
    @PostMapping("/api/add-disatne")
    public String addNewAthlete(@RequestBody Distance distance) {
        service.addAthlete(athlete);
        return "added new athlete";
    }


    //Configuration

}

