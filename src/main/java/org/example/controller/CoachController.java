package org.example.controller;
import org.example.model.UserEntity;
import org.example.service.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api")
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


    @PostMapping("/add-coach")
    public String addCoach(@RequestBody Map<String, String> request) {
        String coachUsername = request.get("username");
        String userId = getCurrentUserId();

        // do poprawy - dodac do service
        UserEntity user = service.getUserById(userId);

        user.addCoach(coachUsername);

        return "added coach";
    }


}


