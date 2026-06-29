package org.example.controller;
import org.example.model.Athlete;
import org.example.model.DTOs.EffortDTO;
import org.example.model.Distance;
import org.example.model.Effort;
import org.example.repository.AthleteRepository;
import org.example.repository.DistanceRepository;
import org.example.repository.EffortRepository;
import org.example.service.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class BackendController {
    private final Service service;

    public BackendController(Service service) {
        this.service = service ;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // Zakładamy, że name to unikalny login/ID z tokenu JWT
    }

    //efforts
    @GetMapping("/get-efforts-dto")
    public List<EffortDTO> getEffortsDTO() {
        return service.getEffortsDTO();
    }

    @GetMapping("/get-effort-dto-with-id")
    public EffortDTO getEffortDTO(@RequestParam UUID effortId) {
        return service.getEffortById(effortId);
    }

    @GetMapping("/get-efforts")
    public List<Effort> getEfforts() {
        String userId = getCurrentUserId();
        return service.getEffortsForUser(userId);
    }

    @GetMapping("/get-efforts-of-athlete-id")
    public List<EffortDTO> getEffortsOfAthleteId(@RequestParam UUID athleteId) {
        return service.getEffortsDTOofAthlete(athleteId);
    }

    @PostMapping("/add-effort")
    public String addNewEffort(@RequestBody Effort effort) {
        String userId = getCurrentUserId();

        effort.setOwnerId(userId);
        service.addEffort(effort);

        return "added new effort";
    }

    @PostMapping("/add-efforts")
    public String addListOfEfforts(@RequestBody List<Effort> efforts) {
        String userId = getCurrentUserId();

        efforts.stream().forEach(effort -> {
            effort.setOwnerId(userId);
            service.addEffort(effort);
        });

        return "added list of efforts";
    }


    //athletes
    @GetMapping("/get-athletes")
    public List<Athlete> getAthletes() {
        return service.getAthletes();
    }

    @PostMapping("/add-athlete")
    public String addNewAthlete(@RequestBody Athlete athlete) {
        service.addAthlete(athlete);
        return "added new athlete";
    }

    @PostMapping("/add-athletes")
    public String addListOfAthletes(@RequestBody List<Athlete> athletes) {
        athletes.stream().forEach(athlete -> service.addAthlete(athlete));
        return "added list of athletes";
    }


    //Distance
    @PostMapping("/add-distance")
    public String addNewAthlete(@RequestBody Distance distance) {
        service.addDistance(distance);
        return "added new distance";
    }

    @PostMapping("/add-distances")
    public String addNewAthlete(@RequestBody List<Distance> distances) {
        distances.stream().forEach(distance -> service.addDistance(distance));
        return "added list of distances";
    }

    @GetMapping("/get-distances")
    public List<Distance> getDistances() {
        return service.getDistances();
    }


}

