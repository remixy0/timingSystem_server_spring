package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.DTOs.LidarDTO;
import org.example.model.LidarData;
import org.example.model.UserEntity;
import org.example.repository.UserRepository;
import org.example.service.LidarService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "LiDAR")
public class LidarController {

    /** Captures are big (about 30 kB each), so a batch is much smaller than for efforts. */
    static final int MAX_BATCH_SIZE = 50;

    private final LidarService lidarService;
    private final UserRepository userRepository;

    public LidarController(LidarService lidarService, UserRepository userRepository) {
        this.lidarService = lidarService;
        this.userRepository = userRepository;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    /**
     * Whose data the request is about: "s" = the logged-in user, anything else = the username
     * of an athlete the logged-in user coaches. Null when the user may not see that data.
     * (Same rule as GET /api/get-effort-dto-with-id.)
     */
    private String resolveOwner(String ownerId) {
        String userId = getCurrentUserId();
        if (ownerId == null || ownerId.equals("s") || ownerId.equals(userId)) return userId;

        UserEntity currentUser = userRepository.findByUsername(userId).orElse(null);
        UserEntity owner = userRepository.findByUsername(ownerId).orElse(null);
        if (currentUser == null || owner == null || !owner.isCoach(currentUser)) return null;
        return ownerId;
    }

    @Operation(
            summary = "adds the LiDAR capture of an effort",
            description = "The effort must already be on the server and belong to the logged-in user. Sending it again replaces the stored capture."
    )
    @PostMapping("/add-lidar")
    public ResponseEntity<?> addLidar(@RequestBody LidarData lidar) {
        String userId = getCurrentUserId();
        LidarService.SaveResult result = save(lidar, userId);

        if (result == LidarService.SaveResult.EFFORT_NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", result.message));
        }
        if (result == LidarService.SaveResult.INVALID) {
            return ResponseEntity.badRequest().body(Map.of("message", result.message));
        }
        if (result == LidarService.SaveResult.ERROR) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", result.message));
        }
        return ResponseEntity.ok(Map.of("message", result.message));
    }

    /** Saves one capture. A database problem is reported as ERROR instead of failing the whole request. */
    private LidarService.SaveResult save(LidarData lidar, String userId) {
        try {
            return lidarService.save(lidar, userId);
        } catch (RuntimeException e) {
            System.err.println("Could not save LiDAR data: " + e.getMessage());
            return LidarService.SaveResult.ERROR;
        }
    }

    @Operation(
            summary = "adds a list of LiDAR captures",
            description = "Returns the effort ids that were saved and, for the others, why they were refused (INVALID, EFFORT_NOT_FOUND or ERROR)."
    )
    @PostMapping("/add-lidars")
    public ResponseEntity<?> addListOfLidars(@RequestBody List<LidarData> lidars) {
        String userId = getCurrentUserId();
        if (lidars == null || lidars.size() > MAX_BATCH_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("message", "Too many LiDAR captures in one request (max " + MAX_BATCH_SIZE + ")."));
        }

        List<UUID> saved = new ArrayList<>();
        List<Map<String, String>> rejected = new ArrayList<>();
        for (LidarData lidar : lidars) {
            LidarService.SaveResult result = save(lidar, userId);
            if (result == LidarService.SaveResult.SAVED) {
                saved.add(lidar.getEffortId());
            } else {
                Map<String, String> entry = new LinkedHashMap<>();
                entry.put("effortId", lidar == null || lidar.getEffortId() == null ? "" : lidar.getEffortId().toString());
                entry.put("reason", result.name());
                rejected.add(entry);
            }
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Saved " + saved.size() + " of " + lidars.size() + ".");
        body.put("saved", saved);
        body.put("rejected", rejected);
        return ResponseEntity.ok(body);
    }

    @Operation(
            summary = "returns the LiDAR capture of an effort",
            description = "ownerId is \"s\" for your own effort or the username of an athlete you coach. 204 when the effort has no LiDAR capture."
    )
    @GetMapping("/get-lidar")
    public ResponseEntity<LidarDTO> getLidar(@RequestParam UUID effortId, @RequestParam(defaultValue = "s") String ownerId) {
        String owner = resolveOwner(ownerId);
        if (owner == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        LidarDTO lidar = lidarService.getForEffort(effortId, owner);
        if (lidar == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(lidar);
    }

    @Operation(
            summary = "returns the ids of the efforts that have a LiDAR capture"
    )
    @GetMapping("/get-lidar-effort-ids")
    public ResponseEntity<List<UUID>> getLidarEffortIds(@RequestParam(defaultValue = "s") String ownerId) {
        String owner = resolveOwner(ownerId);
        if (owner == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(lidarService.getEffortIdsWithLidar(owner));
    }

    @Operation(
            summary = "deletes the LiDAR capture of one of your own efforts"
    )
    @DeleteMapping("/delete-lidar")
    public ResponseEntity<?> deleteLidar(@RequestParam UUID effortId) {
        String userId = getCurrentUserId();
        if (!lidarService.delete(effortId, userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "This effort has no LiDAR data."));
        }
        return ResponseEntity.ok(Map.of("message", "Deleted successfully!"));
    }
}
