import { api } from "./api";

export const registrationService = {
  registerForEvent: async (eventId) => {
    const response = await api.post(`/events/${eventId}/register`);
    return response.data.data;
  },

  getMyRegistrations: async () => {
    const response = await api.get("/registrations/my");
    return response.data.data;
  },

  getRegistrationById: async (id) => {
    const response = await api.get(`/registrations/${id}`);
    return response.data.data;
  },

  cancelRegistration: async (id) => {
    const response = await api.delete(`/registrations/${id}`);
    return response.data.data;
  },

  getEventRegistrationsForAdmin: async (eventId, search = "") => {
    const params = search ? { search } : {};
    const response = await api.get(`/admin/events/${eventId}/registrations`, { params });
    return response.data.data;
  },
};
