package com.eventhub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.eventhub.entity.User;
import com.eventhub.entity.UserRole;
import com.eventhub.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DataSeedService implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.seed.enabled:true}")
    private boolean seedEnabled;

    @Value("${app.admin.seed.email:admin@eventhub.com}")
    private String adminEmail;

    @Value("${app.admin.seed.password:Admin@1234}")
    private String adminPassword;

    @Value("${app.admin.seed.name:EventHub Administrator}")
    private String adminName;

    public DataSeedService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled) {
            log.info("Data seeding is disabled.");
            return;
        }

        seedAdminUser();
        seedSampleStudents();
    }

    private void seedAdminUser() {
        String email = adminEmail.trim().toLowerCase();
        if (!userRepository.existsByEmail(email)) {
            User admin = User.builder()
                    .name(adminName)
                    .email(email)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(UserRole.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Default development ADMIN account seeded: {}", email);
        } else {
            log.debug("ADMIN account already exists: {}", email);
        }
    }

    private void seedSampleStudents() {
        String defaultStudentPassword = "Student@1234";

        String[][] sampleStudents = {
                {"Alice Student", "student1@eventhub.com"},
                {"Bob Student", "student2@eventhub.com"},
                {"Charlie Student", "student3@eventhub.com"}
        };

        for (String[] studentData : sampleStudents) {
            String name = studentData[0];
            String email = studentData[1].toLowerCase();

            if (!userRepository.existsByEmail(email)) {
                User student = User.builder()
                        .name(name)
                        .email(email)
                        .password(passwordEncoder.encode(defaultStudentPassword))
                        .role(UserRole.STUDENT)
                        .build();
                userRepository.save(student);
                log.info("Sample STUDENT account seeded: {}", email);
            }
        }
    }
}
