package org.example.controller;
import org.example.model.DTOs.EffortDTO;
import org.example.model.Effort;
import org.example.service.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class EffortController {
    private final Service service;

    public EffortController(Service service) {
        this.service = service ;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }


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

}

