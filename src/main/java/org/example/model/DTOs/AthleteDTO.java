package org.example.model.DTOs;

import org.example.model.Athlete;
import org.example.service.PhotoThumbnails;

import java.util.List;
import java.util.UUID;


public record AthleteDTO(
        UUID id,
        String name,
        String surname,
        List<UUID> listOfEffortsId,
        byte[] photo,
        boolean show,
        String ownerId
) {
    public static AthleteDTO from(Athlete athlete) {
        if (athlete == null) return null;
        return new AthleteDTO(
                athlete.getId(),
                athlete.getName(),
                athlete.getSurname(),
                athlete.getListOfEffortsId(),
                PhotoThumbnails.thumbnail(athlete.getPhoto()),
                athlete.isShow(),
                athlete.getOwnerId()
        );
    }
}
