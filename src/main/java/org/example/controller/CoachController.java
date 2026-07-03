package org.example.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.Effort;
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
@RequestMapping("/api")
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
    @PostMapping("/add-coach")
    public ResponseEntity<?> addCoach(@RequestBody Map<String, String> request) {
        String coachUsername = request.get("username");
        String userId = getCurrentUserId();

        UserEntity user = service.getUserById(userId);
        if (!service.doesUserExist(coachUsername)){return ResponseEntity.badRequest().body(Map.of("message", "User doesn't exist"));}

        if(user.getCoaches().contains(coachUsername)){
            return ResponseEntity.badRequest().body(Map.of("message", "Coach already exist"));
        }

        user.addCoach(coachUsername);
        service.saveUser(user);

        return ResponseEntity.ok(Map.of("message", "Added successfully!"));
    }

    @Operation(
            summary = "Removes coach",
            description = "Removes coach from user profile"
    )
    @DeleteMapping("/remove-coach")
    public ResponseEntity<?> removeCoach(@RequestBody Map<String, String> request) {
        String coachUsername = request.get("username");
        String userId = getCurrentUserId();

        UserEntity user = service.getUserById(userId);

        if(!user.getCoaches().contains(coachUsername)){
            return ResponseEntity.badRequest().body(Map.of("message", "Coach doesn't exist"));
        }

        user.removeCoach(coachUsername);

        return ResponseEntity.ok(Map.of("message", "Deleted successfully!"));
    }

    @Operation(
            summary = "Returns efforts of the given user",
            description = "Returns list of efforts object for given user account, coach has to be added in the user profile to acces the data."
    )
    @GetMapping("/get-efforts-as-coach")
    public List<Effort> getDataAsCoach(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String userId = getCurrentUserId();

        UserEntity user = service.getUserById(username);

        System.out.println(username);
        if(user == null) {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");}

        if (!user.isCoach(userId)){throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You are not a coach");}

        List<Effort> efforts = service.getEffortsForUser(username);

        return efforts;
    }

}


