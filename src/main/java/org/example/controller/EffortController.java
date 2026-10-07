package org.example.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.DTOs.EffortDTO;
import org.example.model.DTOs.EffortDTOmini;
import org.example.model.Effort;
import org.example.model.UserEntity;
import org.example.repository.UserRepository;
import org.example.service.Service;
import org.springframework.http.HttpStatus;
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
public class EffortController {
    private final Service service;
    private final UserRepository userRepository;

    public EffortController(Service service, UserRepository userRepository) {
        this.service = service ;
        this.userRepository =  userRepository;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
    public ResponseEntity<EffortDTO> getEffortDTO(@RequestParam UUID effortId, @RequestParam String ownerId) {
        String parameter;
        String userId = getCurrentUserId();
        UserEntity currentuser = userRepository.findByUsername(userId).get();

        if (ownerId.equals("s")) {
            parameter = userId;
        } else {
            UserEntity owner = userRepository.findByUsername(ownerId).get();
            if (!owner.isCoach(currentuser)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            parameter = ownerId;
        }

        return ResponseEntity.ok(service.getEffortById(effortId, parameter));
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
        if (efforts == null || efforts.size() > AthleteController.MAX_BATCH_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("message", "Too many efforts in one request (max " + AthleteController.MAX_BATCH_SIZE + ")."));
        }

        efforts.stream().forEach(effort -> {
            effort.setOwnerId(userId);
            service.addEffort(effort);
        });

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

}

