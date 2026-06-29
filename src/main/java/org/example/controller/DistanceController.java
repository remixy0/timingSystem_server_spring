package org.example.controller;
import org.example.model.Distance;
import org.example.service.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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


