package com.eventhub.service;

import com.eventhub.dto.request.AttendanceRequest;
import com.eventhub.dto.response.AttendanceResponse;

public interface AttendanceService {

    AttendanceResponse markAttendance(Long registrationId, AttendanceRequest request, Long adminUserId);

    AttendanceResponse getAttendanceByRegistrationId(Long registrationId);
}
