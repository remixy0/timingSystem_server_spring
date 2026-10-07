package org.example.controller;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.Athlete;
import org.example.model.DTOs.AthleteDTO;
import org.example.model.DTOs.EffortDTO;
import org.example.model.DTOs.EffortDTOmini;
import org.example.model.DTOs.UserData;
import org.example.model.UserEntity;
import org.example.repository.CoachVerifyRequestRepository;
import org.example.service.Service;
import org.example.service.PhotoThumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/coach")
@Tag(name = "Coaches")
public class CoachController {
    private final Service service;
    private final CoachVerifyRequestRepository coachVerifyRequestRepository;

    public CoachController(Service service, CoachVerifyRequestRepository coachVerifyRequestRepository) {
        this.service = service ;
        this.coachVerifyRequestRepository = coachVerifyRequestRepository;
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

        if(user.equals(coach)) {return ResponseEntity.badRequest().body(Map.of("message", "You cant coach yourself"));}

        if (!service.doesUserExist(coachUsername)){return ResponseEntity.badRequest().body(Map.of("message", "Cannot send request."));}

        if(user.getCoaches().contains(coach)){
            return ResponseEntity.badRequest().body(Map.of("message", "Coach already exist"));
        }

        if (!coachVerifyRequestRepository.addCoachRequest(user, coach)) {
            return ResponseEntity.status(503).body(Map.of("message", "Cannot send requests right now. Please try again later."));
        }

        return ResponseEntity.ok(Map.of("message", "Sent coaching request!"));
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

        if(user == null) return ResponseEntity.badRequest().body(Map.of("message", "Coach doesn't exist"));

        if(coach == null) return ResponseEntity.badRequest().body(Map.of("message", "Coach doesn't exist"));

        if(!user.getCoaches().contains(coach)){
            return ResponseEntity.badRequest().body(Map.of("message", "Coach doesn't exist"));
        }

        user.removeCoach(coach);
        coach.removeCoachingAthlete(user);
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
        UserEntity user = service.getUserByUsername(username);
        UserEntity currentUser = service.getUserByUsername(getCurrentUserId());

        if(user == null) {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");}

        if (!user.isCoach(currentUser)){throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You are not a coach");}

        List<EffortDTO> efforts = service.getEffortsDTO(username);

        return efforts;
    }

    @GetMapping("/athletes")
    public List<AthleteDTO> getAthletesAsCoach(@RequestParam String username) {
        UserEntity user = service.getUserByUsername(username);
        UserEntity currentUser = service.getUserByUsername(getCurrentUserId());

        if(user == null) {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");}

        if (!user.isCoach(currentUser)){throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You are not a coach");}

        List<Athlete> athletes = service.getAthletesForUser(username);
        athletes.stream().map(Athlete::getPhoto).toList()
                .parallelStream().forEach(PhotoThumbnails::thumbnail);

        return athletes.stream().map(AthleteDTO::from).toList();
    }

    @GetMapping("/athletes/efforts")
    public List<EffortDTO> getAthletesAsCoach(@RequestParam String username, @RequestParam UUID athleteID) {
        UserEntity user = service.getUserByUsername(username);
        UserEntity currentUser = service.getUserByUsername(getCurrentUserId());

        if(user == null) {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");}

        if (!user.isCoach(currentUser)){throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "You are not a coach");}

        return service.getEffortsDTOofAthlete(athleteID,username);
    }


    @GetMapping("/users")
    public List<UserData> getUsersAsCoach() {
        String currentUser = getCurrentUserId();
        UserEntity user = service.getUserByUsername(currentUser);
        if(user == null) {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");}
        List<UserData> data = new ArrayList<>();
        for(UserEntity userI : user.getCoachingAthletes()){
            data.add(new UserData(
                    userI.getUsername(),
                    userI.getEmail(),
                    PhotoThumbnails.thumbnail(userI.getPhoto())
            ));
        }

        return data;
    }

    @GetMapping("/coaches")
    public List<UserData> getCoaches() {
        String currentUser = getCurrentUserId();
        UserEntity user = service.getUserByUsername(currentUser);
        if(user == null) {throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");}

        List<UserData> data = new ArrayList<>();
        for(UserEntity userI : user.getCoaches()){
            data.add(new UserData(
                    userI.getUsername(),
                    userI.getEmail(),
                    PhotoThumbnails.thumbnail(userI.getPhoto())
            ));
        }

        return data;
    }

}


