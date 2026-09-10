package org.example.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "coach_usernames")
    private List<String> coaches = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @Column(name = "athletes_usernames")
    private List<String> coachingAthletes = new ArrayList<>();



    public void addCoach(String username){
        this.coaches.add(username);
    }

    public void removeCoach(String username){
        this.coaches.remove(username);
    }

    public List<String> getCoaches() {
        return coaches;
    }

    public boolean isCoach(String username){
        return this.coaches.contains(username);
    }

    public void addCoachingAthlete(String username){
        this.coachingAthletes.add(username);
    }

    public void removeCoachingAthlete(String username){
        this.coachingAthletes.remove(username);
    }

    public List<String> getCoachingAthletes() {
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