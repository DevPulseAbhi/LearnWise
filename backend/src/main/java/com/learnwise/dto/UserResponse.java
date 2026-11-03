package com.learnwise.dto;

import com.learnwise.entities.User;

public record UserResponse(long id, String email, String displayName, String avatarUrl) {
    public static UserResponse from(User user) { return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getAvatarUrl()); }
}
