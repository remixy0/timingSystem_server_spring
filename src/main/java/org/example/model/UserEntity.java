package org.example.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

// equals/hashCode/toString use only the id: the coach links point back at each other,
// so including them (as @Data did) recurses forever.
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    @ToString.Include
    private UUID id;

    @Column(unique = true, nullable = false)
    @ToString.Include
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private byte[] photo;

    /**
     * Written into every JWT this user gets. Increasing it invalidates all of the user's
     * existing tokens (see TokenVersionService). Existing rows start at 0.
     */
    @Column(name = "token_version", nullable = false, columnDefinition = "integer default 0")
    private int tokenVersion = 0;

    /**
     * Coaching relationship, stored once in user_coaches (athlete_id, coach_id).
     * An athlete can have many coaches and a coach can have many athletes.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_coaches",
            joinColumns = @JoinColumn(name = "athlete_id"),
            inverseJoinColumns = @JoinColumn(name = "coach_id"))
    private Set<UserEntity> coaches = new HashSet<>();

    /** Inverse side of {@link #coaches}; change it through addCoach/removeCoach. */
    @ManyToMany(mappedBy = "coaches", fetch = FetchType.EAGER)
    private Set<UserEntity> coachingAthletes = new HashSet<>();


    /** Makes `coach` a coach of this user (updates both sides; save this user). */
    public void addCoach(UserEntity coach) {
        this.coaches.add(coach);
        coach.coachingAthletes.add(this);
    }

    /** Removes `coach` from this user's coaches (updates both sides; save this user). */
    public void removeCoach(UserEntity coach) {
        this.coaches.remove(coach);
        coach.coachingAthletes.remove(this);
    }

    public Set<UserEntity> getCoaches() {
        return coaches;
    }

    public boolean isCoach(UserEntity coach) {
        return this.coaches.contains(coach);
    }

    public void addCoachingAthlete(UserEntity coachingAthlete) {
        coachingAthlete.addCoach(this);
    }

    public void removeCoachingAthlete(UserEntity coachingAthlete) {
        coachingAthlete.removeCoach(this);
    }

    public Set<UserEntity> getCoachingAthletes() {
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