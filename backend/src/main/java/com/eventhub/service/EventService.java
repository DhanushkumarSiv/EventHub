package com.eventhub.service;

import java.util.List;

import com.eventhub.dto.request.EventRequest;
import com.eventhub.dto.response.EventResponse;

public interface EventService {

    EventResponse createEvent(EventRequest request);

    EventResponse updateEvent(Long id, EventRequest request);

    EventResponse publishEvent(Long id);

    EventResponse cancelEvent(Long id);

    void deleteEvent(Long id);

    List<EventResponse> getAllEventsForAdmin();

    List<EventResponse> getPublishedEvents();

    EventResponse getEventById(Long id);

    EventResponse getPublishedEventById(Long id);
}
