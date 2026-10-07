package org.example.service;
import jakarta.transaction.Transactional;
import org.example.model.Athlete;
import org.example.model.DTOs.EffortDTOmini;
import org.example.model.Distance;
import org.example.model.Effort;
import org.example.model.DTOs.EffortDTO;
import org.example.model.UserEntity;
import org.example.repository.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;


@org.springframework.stereotype.Service
public class Service {
    private final AthleteRepository athleteRepository;
    private final EffortRepository effortRepository;
    private final DistanceRepository distanceRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public Service(AthleteRepository athleteRepository,EffortRepository effortRepository, DistanceRepository distanceRepository, UserRepository userRepository, SimpMessagingTemplate messagingTemplate) {
        this.effortRepository = effortRepository;
        this.athleteRepository = athleteRepository;
        this.distanceRepository = distanceRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /*
     * Building effort DTOs needs the athlete's name and the distance for every effort.
     * Instead of 3 queries per effort (one of which loaded the whole athlete row,
     * including the full-size photo), these helpers load all names and distances
     * for a list of efforts in 2 queries total.
     */
    private record Lookups(Map<UUID, String> athleteNames, Map<UUID, Distance> distances) {}

    private Lookups lookupsFor(List<Effort> efforts) {
        Set<UUID> athleteIds = new HashSet<>();
        Set<UUID> distanceIds = new HashSet<>();
        for (Effort effort : efforts) {
            if (effort.getAthleteId() != null) athleteIds.add(effort.getAthleteId());
            if (effort.getDistanceId() != null) distanceIds.add(effort.getDistanceId());
        }

        Map<UUID, String> names = new HashMap<>();
        if (!athleteIds.isEmpty()) {
            for (AthleteRepository.AthleteName a : athleteRepository.findNamesByIdIn(athleteIds)) {
                names.put(a.getId(), a.getName() + " " + a.getSurname()); // same as Athlete.toString()
            }
        }

        Map<UUID, Distance> distances = distanceIds.isEmpty()
                ? Map.of()
                : distanceRepository.findAllById(distanceIds).stream()
                        .collect(Collectors.toMap(Distance::getId, Function.identity()));

        return new Lookups(names, distances);
    }

    /*
     * Effort dates are free-text strings, so sorting them as text only works for
     * "yyyy-MM-dd ..." dates. This turns the common formats into a sortable key
     * ("yyyyMMddHHmmss"), and the comparator puts the newest effort first.
     * Supported: 2026-03-14T22:26:17+0100 (iOS app), 2026-09-28, 2026-09-28 18:10, 2026-09-28T18:10:05(.123),
     *            28.09.2026, 28.09.2026 18:10, 28/09/2026, 28-09-2026 (day first).
     * Anything else falls back to the original text and goes below the recognised dates.
     */
    private static final java.util.regex.Pattern ISO_DATE = java.util.regex.Pattern.compile(
            "^(\\d{4})-(\\d{1,2})-(\\d{1,2})(?:[T ]+(\\d{1,2}):(\\d{2})(?::(\\d{2}))?)?.*");
    private static final java.util.regex.Pattern DAY_FIRST_DATE = java.util.regex.Pattern.compile(
            "^(\\d{1,2})[./-](\\d{1,2})[./-](\\d{4})(?:[ ,T]+(\\d{1,2}):(\\d{2})(?::(\\d{2}))?)?.*");

    // What the iOS app sends: 2026-03-14T22:26:17+0100 (offset optional, may also be +01:00 or Z)
    private static final java.time.format.DateTimeFormatter ISO_WITH_OFFSET =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX][XX]");
    private static final java.time.format.DateTimeFormatter KEY_FORMAT =
            java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    static String dateSortKey(String date) {
        if (date == null) return null;
        String d = date.trim();
        // Dates with a timezone are compared as real moments in time (in UTC),
        // so +0100 / +0200 (winter / summer time) efforts still sort correctly
        try {
            return "1" + java.time.OffsetDateTime.parse(d, ISO_WITH_OFFSET)
                    .withOffsetSameInstant(java.time.ZoneOffset.UTC)
                    .format(KEY_FORMAT);
        } catch (java.time.format.DateTimeParseException ignored) {
            // no timezone or another format – handled below
        }
        java.util.regex.Matcher m = ISO_DATE.matcher(d);
        if (m.matches()) return "1" + sortKey(m.group(1), m.group(2), m.group(3), m.group(4), m.group(5), m.group(6));
        m = DAY_FIRST_DATE.matcher(d);
        if (m.matches()) return "1" + sortKey(m.group(3), m.group(2), m.group(1), m.group(4), m.group(5), m.group(6));
        return "0" + d; // unrecognised format: after all recognised dates
    }

    private static String sortKey(String y, String mo, String d, String h, String mi, String s) {
        return String.format("%04d%02d%02d%02d%02d%02d",
                Integer.parseInt(y), Integer.parseInt(mo), Integer.parseInt(d),
                h == null ? 0 : Integer.parseInt(h), mi == null ? 0 : Integer.parseInt(mi), s == null ? 0 : Integer.parseInt(s));
    }

    static final Comparator<String> NEWEST_FIRST =
            Comparator.comparing(Service::dateSortKey, Comparator.nullsLast(Comparator.<String>reverseOrder()));

    private static String speedOf(Effort effort, Distance distance) {
        if (distance == null || effort.getTotalTime() == null || effort.getTotalTime() == 0) return "0.0";
        Double speed = (double) Math.round(distance.getDistanceInMeters() * 360 / effort.getTotalTime());
        speed = speed / 100;
        return speed.toString();
    }

    private static EffortDTO toDTO(Effort effort, Lookups lookups) {
        Distance distance = lookups.distances().get(effort.getDistanceId());
        return new EffortDTO(
                effort.getId(),
                lookups.athleteNames().getOrDefault(effort.getAthleteId(), "Unknown athlete"),
                effort.getDate(),
                distance != null ? distance.getDisplayName() : "",
                effort.getTotalTime(),
                speedOf(effort, distance),
                effort.getAverageLapTime().toString(),
                effort.getLapTimes(),
                effort.isShow()
        );
    }

    private static EffortDTOmini toMiniDTO(Effort effort, Lookups lookups) {
        Distance distance = lookups.distances().get(effort.getDistanceId());
        return new EffortDTOmini(
                effort.getId(),
                lookups.athleteNames().getOrDefault(effort.getAthleteId(), "Unknown athlete"),
                effort.getDate(),
                distance != null ? distance.getDisplayName() : "",
                effort.getTotalTime(),
                speedOf(effort, distance),
                effort.getAverageLapTime().toString(),
                effort.isShow()
        );
    }

    public List<EffortDTO> getEffortsDTO(String userId) {
        List<Effort> efforts = this.getEffortsForUser(userId);
        Lookups lookups = lookupsFor(efforts);
        return efforts.stream()
                .map(effort -> toDTO(effort, lookups))
                .sorted(Comparator.comparing(EffortDTO::getDate, NEWEST_FIRST))
                .toList();
    }

    public List<EffortDTOmini> getEffortsDTOmini(String userId) {
        List<Effort> efforts = this.getEffortsForUser(userId);
        Lookups lookups = lookupsFor(efforts);
        return efforts.stream()
                .map(effort -> toMiniDTO(effort, lookups))
                .sorted(Comparator.comparing(EffortDTOmini::getDate, NEWEST_FIRST))
                .toList();
    }


    public List<EffortDTO> getEffortsDTOofAthlete(UUID athleteId, String userId) {
        // Only check ownership here – don't load the athlete (and its photo)
        if (!athleteRepository.existsByIdAndOwnerId(athleteId, userId)) return new ArrayList<>();

        List<Effort> efforts = effortRepository.findAllByAthleteId(athleteId).stream()
                .filter(Effort::isShow)
                .filter(effort -> Objects.equals(userId, effort.getOwnerId()))
                .toList();
        Lookups lookups = lookupsFor(efforts);
        return efforts.stream()
                .map(effort -> toDTO(effort, lookups))
                .sorted(Comparator.comparing(EffortDTO::getDate, NEWEST_FIRST))
                .toList();
    }

    @Transactional
    public void addEffort(Effort effort) {
        if (effort == null || effort.getAthleteId() == null) return;

        Athlete athlete = athleteRepository.findById(effort.getAthleteId()).orElse(null);
        // Only allow efforts for your own athletes (otherwise anyone who knows an athlete id
        // could add fake results to another user's athlete)
        if (athlete == null || !Objects.equals(athlete.getOwnerId(), effort.getOwnerId())) return;

        Effort existing = effort.getId() == null ? null : effortRepository.findById(effort.getId()).orElse(null);
        // Don't let one user overwrite (and take over) another user's effort by sending its id
        if (existing != null && !Objects.equals(existing.getOwnerId(), effort.getOwnerId())) return;

        // Re-uploads (edits, "Upload all data" in the app) must not show up as live efforts
        boolean isNewEffort = existing == null;
        Effort savedEffort = effortRepository.save(effort);

        if (athlete.getListOfEffortsId() != null && !athlete.getListOfEffortsId().contains(savedEffort.getId())) {
            athlete.addEffort(savedEffort.getId());
            athleteRepository.save(athlete);
        }

        if (isNewEffort) {
            messagingTemplate.convertAndSendToUser(
                    savedEffort.getOwnerId(),
                    "/queue/efforts",
                    this.effortToEffortDTO(savedEffort)
            );
        }
    }

    public EffortDTO getEffortById(UUID id, String userId) {
        // Load just this effort instead of all of the user's efforts
        Effort effort = effortRepository.findById(id)
                .filter(e -> userId != null && userId.equals(e.getOwnerId()))
                .orElse(null);
        if (effort == null) return null;
        return effortToEffortDTO(effort);
    }

    public UserEntity getUserByUsername(String username) {
        UserEntity userEntity = userRepository.findByUsername(username).orElse(null);
        return userEntity;
    }

    public boolean addAthlete(Athlete athlete) {
        if (athlete == null || athleteRepository.existsById(athlete.getId())) return false;

        athleteRepository.save(athlete);
        return true;

    }

    /**
     * Creates the athlete, or updates it if it already exists and belongs to the same user.
     * The effort id lists are merged so efforts added elsewhere aren't dropped,
     * and the stored photo is kept when the request has none.
     */
    public boolean upsertAthlete(Athlete incoming) {
        if (incoming == null || incoming.getId() == null) return false;

        Athlete existing = athleteRepository.findById(incoming.getId()).orElse(null);
        if (existing == null) {
            athleteRepository.save(incoming);
            return true;
        }
        if (!Objects.equals(existing.getOwnerId(), incoming.getOwnerId())) return false;

        existing.setName(incoming.getName());
        existing.setSurname(incoming.getSurname());
        existing.setShow(incoming.isShow());
        // Keep the stored full-size photo if the app just sent back the thumbnail we gave it
        byte[] photo = incoming.getPhoto();
        boolean isOurThumbnail = existing.getPhoto() != null && photo != null
                && Arrays.equals(photo, PhotoThumbnails.thumbnail(existing.getPhoto()));
        if (photo != null && photo.length > 0 && !isOurThumbnail) {
            existing.setPhoto(photo);
        }

        List<UUID> mergedEfforts = new ArrayList<>();
        if (existing.getListOfEffortsId() != null) mergedEfforts.addAll(existing.getListOfEffortsId());
        if (incoming.getListOfEffortsId() != null) {
            for (UUID effortId : incoming.getListOfEffortsId()) {
                if (!mergedEfforts.contains(effortId)) mergedEfforts.add(effortId);
            }
        }
        existing.setListOfEffortsId(mergedEfforts);

        athleteRepository.save(existing);
        return true;
    }

    public void deleteAthlete(UUID athleteId) {
        athleteRepository.deleteById(athleteId);
    }

    public List<Athlete> getAthletesForUser(String userId) {
        return athleteRepository.findAllByOwnerId(userId);
    }

    public Athlete getAthleteofId(String userId, UUID athleteId) {
        // Load just this athlete instead of every athlete (and photo) the user owns
        return athleteRepository.findByIdAndOwnerId(athleteId, userId).orElse(null);
    }

    public boolean addDistance(Distance distance) {
        if (distance == null) return false;
        if (distance.getId() != null) {
            Distance existing = distanceRepository.findById(distance.getId()).orElse(null);
            // Don't let one user overwrite another user's distance
            if (existing != null && !Objects.equals(existing.getOwnerId(), distance.getOwnerId())) return false;
        }
        distanceRepository.save(distance);
        return true;
    }

    public List<Distance> getDistancesForUser(String userId) {
        return distanceRepository.findAllByOwnerId(userId);
    }

    public List<Effort> getEfforts() {
        return effortRepository.findAll();
    }

    public List<Effort> getEffortsForUser(String userID){
       return effortRepository.findAllByOwnerId(userID);
    }

    public boolean doesUserExist(String userId) {
        return userRepository.findByUsername(userId).isPresent();
    }

    public void saveUser(UserEntity user) {
        userRepository.save(user);
    }


    public EffortDTO effortToEffortDTO(Effort effort) {
        return toDTO(effort, lookupsFor(List.of(effort)));
    }
}
