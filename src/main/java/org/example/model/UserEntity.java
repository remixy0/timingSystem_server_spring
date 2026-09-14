package org.example.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private byte[] photo;

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "coach_usernames")
    private List<UserEntity> coaches = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "athletes_usernames")
    private List<UserEntity> coachingAthletes = new ArrayList<>();



    public void addCoach(UserEntity coach) {
        this.coaches.add(coach);
    }

    public void removeCoach(UserEntity coach) {
        this.coaches.remove(coach);
    }

    public List<UserEntity> getCoaches() {
        return coaches;
    }

    public boolean isCoach(UserEntity coach) {
        return this.coaches.contains(coach);
    }

    public void addCoachingAthlete(UserEntity coachingAthlete) {
        this.coachingAthletes.add(coachingAthlete);
    }

    public void removeCoachingAthlete(UserEntity coachingAthlete) {
        this.coachingAthletes.remove(coachingAthlete);
    }

    public List<UserEntity> getCoachingAthletes() {
        return coachingAthletes;
    }


    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

}