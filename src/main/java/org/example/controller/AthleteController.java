package org.example.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Athletes")
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

    @Operation(
            summary = "returns list of Athletes"
    )
    @GetMapping("/get-athletes")
    public List<Athlete> getAthletes() {
        String userId = getCurrentUserId();
        return service.getAthletesForUser(userId);
    }

    @Operation(
            summary = "adds athlete"
    )
    @PostMapping("/add-athlete")
    public ResponseEntity<?> addNewAthlete(@RequestBody Athlete athlete) {
        String userId = getCurrentUserId();

        athlete.setOwnerId(userId);
        service.addAthlete(athlete);

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

    @Operation(
            summary = "adds list of athletes"
    )
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

