package org.example.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.DTOs.EffortDTO;
import org.example.model.DTOs.EffortDTOmini;
import org.example.model.Effort;
import org.example.service.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api")
@Tag(name = "Efforts")
@CrossOrigin(origins = "http://localhost:5173")
public class EffortController {
    private final Service service;

    public EffortController(Service service) {
        this.service = service ;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(authentication.getName());
        return authentication.getName();
    }

    @Operation(
            summary = "returns efforts DTO"
    )
    @GetMapping("/get-efforts-dto")
    public List<EffortDTO> getEffortsDTO() {
        String userId = getCurrentUserId();
        return service.getEffortsDTO(userId);
    }

    @Operation(
            summary = "returns efforts DTOmini"
    )
    @GetMapping("/get-efforts-dtom")
    public List<EffortDTOmini> getEffortsDTOmini() {
        String userId = getCurrentUserId();
        return service.getEffortsDTOmini(userId);
    }

    @Operation(
            summary = "returns effort DTO with given ID"
    )
    @GetMapping("/get-effort-dto-with-id")
    public EffortDTO getEffortDTO(@RequestParam UUID effortId) {
        String userId = getCurrentUserId();
        return service.getEffortById(effortId, userId);
    }

    @Operation(
            summary = "returns efforts"
    )
    @GetMapping("/get-efforts")
    public List<Effort> getEfforts() {
        String userId = getCurrentUserId();
        return service.getEffortsForUser(userId);
    }

    @Operation(
            summary = "returns efforts for given athleteID"
    )
    @GetMapping("/get-efforts-of-athlete-id")
    public List<EffortDTO> getEffortsOfAthleteId(@RequestParam UUID athleteId) {
        String userId = getCurrentUserId();
        return service.getEffortsDTOofAthlete(athleteId,userId);
    }

    @Operation(
            summary = "adds effort"
    )
    @PostMapping("/add-effort")
    public ResponseEntity<?> addNewEffort(@RequestBody Effort effort) {
        String userId = getCurrentUserId();

        effort.setOwnerId(userId);
        service.addEffort(effort);

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

    @Operation(
            summary = "adds list of efforts"
    )
    @PostMapping("/add-efforts")
    public ResponseEntity<?> addListOfEfforts(@RequestBody List<Effort> efforts) {
        String userId = getCurrentUserId();

        efforts.stream().forEach(effort -> {
            effort.setOwnerId(userId);
            service.addEffort(effort);
        });

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

}

