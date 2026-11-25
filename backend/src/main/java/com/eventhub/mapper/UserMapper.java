package com.eventhub.mapper;

import com.eventhub.dto.response.UserSummaryResponse;
import com.eventhub.entity.User;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserSummaryResponse toSummaryResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserSummaryResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
