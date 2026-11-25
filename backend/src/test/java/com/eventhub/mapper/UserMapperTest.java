package com.eventhub.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.eventhub.dto.response.UserSummaryResponse;
import com.eventhub.entity.User;
import com.eventhub.entity.UserRole;

class UserMapperTest {

    @Test
    void toSummaryResponseMapsCorrectly() {
        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@eventhub.com")
                .role(UserRole.STUDENT)
                .createdAt(Instant.now())
                .build();

        UserSummaryResponse response = UserMapper.toSummaryResponse(user);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Alice", response.getName());
        assertEquals("alice@eventhub.com", response.getEmail());
        assertEquals(UserRole.STUDENT, response.getRole());
    }

    @Test
    void toSummaryResponseReturnsNullWhenInputIsNull() {
        assertNull(UserMapper.toSummaryResponse(null));
    }
}
