package org.example.model.DTOs;

import org.example.model.UserEntity;

public record AddCoachRequest(UserEntity user, UserEntity coach) {
}
