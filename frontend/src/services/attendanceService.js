import { api } from "./api";

export const attendanceService = {
  markAttendance: async (registrationId, status) => {
    const response = await api.patch(`/admin/registrations/${registrationId}/attendance`, { status });
    return response.data.data;
  },
};
