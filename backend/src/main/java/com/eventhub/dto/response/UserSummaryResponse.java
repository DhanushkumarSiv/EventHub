package com.eventhub.dto.response;

import java.time.Instant;

import com.eventhub.entity.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private Instant createdAt;
}
