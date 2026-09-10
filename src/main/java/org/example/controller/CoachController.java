package org.example.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.DTOs.EffortDTO;
import org.example.model.UserEntity;
import org.example.service.Service;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coach")
@Tag(name = "Coaches")
@CrossOrigin(origins = "http://localhost:5173")
public class CoachController {
    private final Service service;

    public CoachController(Service service) {
        this.service = service ;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }


    @Operation(
            summary = "Adds a coach to profile",
            description = "Adds a coach to profile. Coach will get access to efforts of the profile."
    )
    @PostMapping
    public ResponseEntity<?> addCoach(@RequestParam String coachUsername) {
        String userId = getCurrentUserId();

        UserEntity user = service.getUserByUsername(userId);
        UserEntity coach = service.getUserByUsername(coachUsername);
        if (!service.doesUserExist(coachUsername)){return ResponseEntity.badRequest().body(Map.of("message", "User doesn't exist"));}

        if(user.getCoaches().contains(coachUsername)){
            return ResponseEntity.badRequest().body(Map.of("message", "Coach already exist"));
        }

        user.addCoach(coachUsername);
        coach.addCoachingAthlete(userId);

        service.saveUser(user);

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

    @Operation(
            summary = "Removes coach",
            description = "Removes coach from user profile"
    )
    @DeleteMapping
    public ResponseEntity<?> removeCoach(@RequestParam String coachUsername) {
        String userId = getCurrentUserId();

        UserEntity user = service.getUserByUsername(userId);
        UserEntity coach = service.getUserByUsername(coachUsername);

        if(!user.getCoaches().contains(coachUsername)){
            return ResponseEntity.badRequest().body(Map.of("message", "Coach doesn't exist"));
        }

        user.removeCoach(coachUsername);
        coach.removeCoachingAthlete(userId);
        service.saveUser(user);
        service.saveUser(coach);

        return ResponseEntity.ok(Map.of("message", "Deleted successfully!"));
    }

    @Operation(
            summary = "Returns efforts of the given user",
            description = "Returns list of efforts object for given user account, coach has to be added in the user profile to acces the data."
    )
    @GetMapping
    public List<EffortDTO> getDataAsCoach(@RequestParam String username) {
        String currentUser = getCurrentUserId();
        UserEntity user = service.getUserByUsername(username);

        if(user == null) {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");}

        if (!user.isCoach(currentUser)){throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You are not a coach");}

        List<EffortDTO> efforts = service.getEffortsDTO(username);

        return efforts;
    }

    @GetMapping("/users")
    public List<String> getUsersAsCoach() {
        String currentUser = getCurrentUserId();
        UserEntity user = service.getUserByUsername(currentUser);
        if(user == null) {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");}

        return user.getCoachingAthletes();
    }

}


