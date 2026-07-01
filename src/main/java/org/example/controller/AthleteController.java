package org.example.controller;
import org.example.model.Athlete;
import org.example.service.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

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
        String userId = getCurrentUserId();
        return service.getAthletesForUser(userId);
    }

    @PostMapping("/add-athlete")
    public ResponseEntity<?> addNewAthlete(@RequestBody Athlete athlete) {
        String userId = getCurrentUserId();

        athlete.setOwnerId(userId);
        service.addAthlete(athlete);

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

    @PostMapping("/add-athletes")
    public ResponseEntity<?> addListOfAthletes(@RequestBody List<Athlete> athletes) {
        String userId = getCurrentUserId();

        athletes.stream().forEach(athlete -> {
            athlete.setOwnerId(userId);
            service.addAthlete(athlete);
        });

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }
}

