package com.eventhub.service;

import java.util.List;

import com.eventhub.dto.response.RegistrationResponse;

public interface RegistrationService {

    RegistrationResponse registerForEvent(Long eventId, Long userId);

    List<RegistrationResponse> getMyRegistrations(Long userId);

    RegistrationResponse getRegistrationById(Long registrationId, Long requestingUserId, boolean isAdmin);

    RegistrationResponse cancelRegistration(Long registrationId, Long requestingUserId, boolean isAdmin);

    List<RegistrationResponse> getEventRegistrationsForAdmin(Long eventId, String search);
}
