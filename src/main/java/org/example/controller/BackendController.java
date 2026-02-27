package org.example.controller;
import org.example.model.Athlete;
import org.example.model.Effort;
import org.example.model.DTOs.EffortDTO;
import org.example.service.Service;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class BackendController {
    Service service = new Service();

    @GetMapping("/api/get-efforts")
    @CrossOrigin(origins = "http://localhost:5173")
    public List<EffortDTO> getEfforts() {
        return service.getEffortsDTO();
    }

    @GetMapping("/api/get-athletes")
    @CrossOrigin(origins = "http://localhost:5173")
    public List<Athlete> getAthletes() {
        return service.getAthletes();
    }

    @PostMapping("/api/add-effort")
    public String addNewEffort(@RequestBody Effort effort) {
        service.addEffort(effort);
        return "added new effort";
    }

    @PostMapping("/api/add-athlete")
    public String addNewAthlete(@RequestBody Athlete athlete) {
        service.addAthlete(athlete);
        return "added new athlete";
    }

}

