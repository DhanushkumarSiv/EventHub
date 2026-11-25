import { api } from "./api";

export const eventService = {
  // Public & Student endpoints
  getPublishedEvents: async () => {
    const response = await api.get("/events");
    return response.data.data;
  },

  getPublishedEventById: async (id) => {
    const response = await api.get(`/events/${id}`);
    return response.data.data;
  },

  // Admin endpoints
  getAllEventsForAdmin: async () => {
    const response = await api.get("/admin/events");
    return response.data.data;
  },

  getEventByIdForAdmin: async (id) => {
    const response = await api.get(`/admin/events/${id}`);
    return response.data.data;
  },

  createEvent: async (eventData) => {
    const response = await api.post("/admin/events", eventData);
    return response.data.data;
  },

  updateEvent: async (id, eventData) => {
    const response = await api.put(`/admin/events/${id}`, eventData);
    return response.data.data;
  },

  publishEvent: async (id) => {
    const response = await api.patch(`/admin/events/${id}/publish`);
    return response.data.data;
  },

  cancelEvent: async (id) => {
    const response = await api.patch(`/admin/events/${id}/cancel`);
    return response.data.data;
  },

  deleteEvent: async (id) => {
    const response = await api.delete(`/admin/events/${id}`);
    return response.data;
  },
};
