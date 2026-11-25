package com.eventhub.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eventhub.dto.request.AttendanceRequest;
import com.eventhub.dto.response.AttendanceResponse;
import com.eventhub.entity.Attendance;
import com.eventhub.entity.EventStatus;
import com.eventhub.entity.Registration;
import com.eventhub.entity.RegistrationStatus;
import com.eventhub.entity.User;
import com.eventhub.exception.BadRequestException;
import com.eventhub.exception.ConflictException;
import com.eventhub.exception.ResourceNotFoundException;
import com.eventhub.mapper.RegistrationMapper;
import com.eventhub.repository.AttendanceRepository;
import com.eventhub.repository.RegistrationRepository;
import com.eventhub.repository.UserRepository;
import com.eventhub.service.AttendanceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;

    public AttendanceServiceImpl(
            AttendanceRepository attendanceRepository,
            RegistrationRepository registrationRepository,
            UserRepository userRepository) {
        this.attendanceRepository = attendanceRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AttendanceResponse markAttendance(Long registrationId, AttendanceRequest request, Long adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        Registration registration = registrationRepository.findByIdWithDetails(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", registrationId));

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new ConflictException("Cannot mark attendance for a cancelled registration");
        }

        if (registration.getEvent() != null && registration.getEvent().getStatus() == EventStatus.DRAFT) {
            throw new BadRequestException("Cannot mark attendance for a draft event");
        }

        Attendance attendance = attendanceRepository.findByRegistrationId(registrationId)
                .map(existing -> {
                    existing.setStatus(request.getStatus());
                    existing.setMarkedBy(admin);
                    existing.setAttendanceTime(Instant.now());
                    log.info("Updated attendance for registration {} to {}", registrationId, request.getStatus());
                    return existing;
                })
                .orElseGet(() -> {
                    Attendance newAttendance = Attendance.builder()
                            .registration(registration)
                            .markedBy(admin)
                            .attendanceTime(Instant.now())
                            .status(request.getStatus())
                            .build();
                    registration.setAttendance(newAttendance);
                    log.info("Created new attendance for registration {} as {}", registrationId, request.getStatus());
                    return newAttendance;
                });

        Attendance saved = attendanceRepository.save(attendance);
        return RegistrationMapper.toAttendanceResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceByRegistrationId(Long registrationId) {
        Attendance attendance = attendanceRepository.findByRegistrationId(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "registrationId", registrationId));

        return RegistrationMapper.toAttendanceResponse(attendance);
    }
}
