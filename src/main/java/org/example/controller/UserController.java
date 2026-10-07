package org.example.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.model.DTOs.UserData;
import org.example.model.UserEntity;
import org.example.repository.UserRepository;
import org.example.service.Service;
import org.example.service.PhotoThumbnails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
@Tag(name = "Users")
public class UserController {
    private final Service service;
    private final UserRepository userRepository;

    public UserController(Service service, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.service = service;
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }

    @GetMapping("/user/get")
    public UserData getCurrentUser(@RequestParam String username) {
        UserEntity currentuser = userRepository.findByUsername(this.getCurrentUserId()).orElse(null);
        UserEntity user = userRepository.findByUsername(username).orElse(null);
        if (currentuser == null || user == null) {return null;}
        if(!user.isCoach(currentuser)) {return null;}
        return new UserData(
                user.getUsername(),
                user.getEmail(),
                PhotoThumbnails.thumbnail(user.getPhoto())
        );
    }

    @GetMapping("/user/profile")
    public UserData getCurrentUser() {
        UserEntity currentuser = userRepository.findByUsername(this.getCurrentUserId()).orElse(null);
        return new UserData(
                currentuser.getUsername(),
                currentuser.getEmail(),
                PhotoThumbnails.thumbnail(currentuser.getPhoto())
        );
    }




}