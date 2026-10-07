package org.example.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.Athlete;
import org.example.model.DTOs.AthleteDTO;
import org.example.service.Service;
import org.example.service.PhotoThumbnails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Tag(name = "Athletes")
public class AthleteController {
    private final Service service;

    /** Most items accepted in one batch upload (protects the server from huge requests). */
    static final int MAX_BATCH_SIZE = 5000;
    /** Largest athlete photo accepted. Phone photos are a few MB; thumbnails are made on the server. */
    static final int MAX_PHOTO_BYTES = 20 * 1024 * 1024;

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
    public List<AthleteDTO> getAthletes() {
        String userId = getCurrentUserId();
        List<Athlete> athletes = service.getAthletesForUser(userId);
        athletes.stream().map(Athlete::getPhoto).toList()
                .parallelStream().forEach(PhotoThumbnails::thumbnail);

        return athletes.stream().map(AthleteDTO::from).toList();
    }

    @Operation(
            summary = "returns Athlete of Id"
    )
    @GetMapping("/get-athlete-id")
    public AthleteDTO getAthleteById(@RequestParam UUID athleteId) {
        String userId = getCurrentUserId();
        return AthleteDTO.from(service.getAthleteofId(userId, athleteId));

    }

    @Operation(
            summary = "adds athlete"
    )
    @PostMapping("/add-athlete")
    public ResponseEntity<?> addNewAthlete(@RequestBody Athlete athlete) {
        String userId = getCurrentUserId();
        if (athlete == null) return ResponseEntity.badRequest().body(Map.of("message", "Failed to add athlete!"));
        if (photoTooLarge(athlete)) return photoTooLargeResponse();
        athlete.setOwnerId(userId);

        if(service.addAthlete(athlete)) return ResponseEntity.ok(Map.of("message", "Added successfully!"));
        return ResponseEntity.badRequest().body(Map.of("message", "Failed to add athlete!"));
    }

    @Operation(
            summary = "adds list of athletes"
    )
    @PostMapping("/add-athletes")
    public ResponseEntity<?> addListOfAthletes(@RequestBody List<Athlete> athletes) {
        String userId = getCurrentUserId();
        if (athletes == null || athletes.size() > MAX_BATCH_SIZE) {
            return ResponseEntity.badRequest().body(Map.of("message", "Too many athletes in one request (max " + MAX_BATCH_SIZE + ")."));
        }
        if (athletes.stream().anyMatch(AthleteController::photoTooLarge)) return photoTooLargeResponse();

        athletes.stream().forEach(athlete -> {
            athlete.setOwnerId(userId);
            service.upsertAthlete(athlete);
        });

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

    private static boolean photoTooLarge(Athlete athlete) {
        return athlete != null && athlete.getPhoto() != null && athlete.getPhoto().length > MAX_PHOTO_BYTES;
    }

    private static ResponseEntity<?> photoTooLargeResponse() {
        return ResponseEntity.status(413).body(Map.of("message", "Athlete photo is too large (max 20 MB)."));
    }
}
