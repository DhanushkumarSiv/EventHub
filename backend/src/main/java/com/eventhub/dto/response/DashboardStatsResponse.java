package com.eventhub.dto.response;

import java.util.List;

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
public class DashboardStatsResponse {

    private long totalEvents;
    private long publishedEvents;
    private long draftEvents;
    private long completedEvents;
    private long cancelledEvents;
    private long totalRegistrations;
    private long totalAttendance;
    private List<EventResponse> upcomingEvents;
    private List<EventRegistrationStat> registrationsByEvent;
}
