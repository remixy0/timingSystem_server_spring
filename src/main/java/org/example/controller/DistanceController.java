package org.example.controller;
import org.example.model.Distance;
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
public class DistanceController {
    private final Service service;

    public DistanceController(Service service) {
        this.service = service ;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @PostMapping("/add-distance")
    public ResponseEntity<?> addNewAthlete(@RequestBody Distance distance) {
        String userId = getCurrentUserId();

        distance.setOwnerId(userId);
        service.addDistance(distance);

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

    @PostMapping("/add-distances")
    public ResponseEntity<?> addNewAthlete(@RequestBody List<Distance> distances) {
        String userId = getCurrentUserId();

        distances.stream().forEach(distance -> {
            distance.setOwnerId(userId);
            service.addDistance(distance);
        });
        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

    @GetMapping("/get-distances")
    public List<Distance> getDistances() {
        String userId = getCurrentUserId();

        return service.getDistancesForUser(userId);
    }

}


