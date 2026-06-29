package org.example.model;

import jakarta.persistence.*;

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

    @Column(nullable = false)
    private String password;

    private List<String> coaches;

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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}