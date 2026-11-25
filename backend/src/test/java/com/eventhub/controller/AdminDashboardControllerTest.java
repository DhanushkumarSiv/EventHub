package com.eventhub.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.eventhub.dto.response.ApiResponse;
import com.eventhub.dto.response.DashboardStatsResponse;
import com.eventhub.service.DashboardService;

class AdminDashboardControllerTest {

    private DashboardService dashboardService;
    private AdminDashboardController controller;

    @BeforeEach
    void setUp() {
        DashboardStatsResponse sampleStats = DashboardStatsResponse.builder()
                .totalEvents(10L)
                .publishedEvents(6L)
                .totalRegistrations(45L)
                .totalAttendance(20L)
                .build();

        dashboardService = (DashboardService) Proxy.newProxyInstance(
                DashboardService.class.getClassLoader(),
                new Class<?>[]{DashboardService.class},
                (proxy, method, args) -> {
                    if ("getDashboardStats".equals(method.getName())) {
                        return sampleStats;
                    }
                    return null;
                }
        );

        controller = new AdminDashboardController(dashboardService);
    }

    @Test
    void getDashboardStatsReturnsOkWithStats() {
        ResponseEntity<ApiResponse<DashboardStatsResponse>> response = controller.getDashboardStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getData().getTotalEvents());
        assertEquals(45L, response.getBody().getData().getTotalRegistrations());
    }
}
