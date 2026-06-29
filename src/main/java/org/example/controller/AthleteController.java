package org.example.controller;
import org.example.model.Athlete;
import org.example.service.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class AthleteController {
    private final Service service;

    public AthleteController(Service service) {
        this.service = service;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }


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
}

