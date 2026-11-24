package com.eventhub.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final DataSource dataSource;
    private final Environment environment;

    public HealthController(DataSource dataSource, Environment environment) {
        this.dataSource = dataSource;
        this.environment = environment;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("application", "EventHub");
        body.put("timestamp", Instant.now().toString());
        body.put("profiles", environment.getActiveProfiles());

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT current_database() AS database_name");
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            body.put("status", "UP");
            body.put("database", resultSet.getString("database_name"));
            return ResponseEntity.ok(body);
        } catch (SQLException ex) {
            body.put("status", "DOWN");
            body.put("error", "Unable to reach PostgreSQL");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
        }
    }
}
