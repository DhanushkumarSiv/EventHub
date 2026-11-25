import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { ProtectedRoute } from "./ProtectedRoute";

// Public & Student Pages
import { Events } from "../pages/student/Events";
import { EventDetails } from "../pages/student/EventDetails";
import { MyRegistrations } from "../pages/student/MyRegistrations";
import { Login } from "../pages/auth/Login";
import { Register } from "../pages/auth/Register";

// Admin Pages & Layout
import { AdminLayout } from "../components/layout/AdminLayout";
import { Dashboard } from "../pages/admin/Dashboard";
import { ManageEvents } from "../pages/admin/ManageEvents";
import { CreateEvent } from "../pages/admin/CreateEvent";
import { Attendance } from "../pages/admin/Attendance";
import { Participants } from "../pages/admin/Participants";

export const AppRoutes = () => {
  return (
    <Routes>
      {/* Public Pages */}
      <Route path="/" element={<Navigate to="/events" replace />} />
      <Route path="/events" element={<Events />} />
      <Route path="/events/:id" element={<EventDetails />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      {/* Student Protected Routes */}
      <Route
        path="/registrations/my"
        element={
          <ProtectedRoute requiredRole="STUDENT">
            <MyRegistrations />
          </ProtectedRoute>
        }
      />

      {/* Admin Protected Routes with AdminLayout (Sidebar + Content) */}
      <Route
        path="/admin"
        element={
          <ProtectedRoute requiredRole="ADMIN">
            <AdminLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Navigate to="/admin/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="events" element={<ManageEvents />} />
        <Route path="events/new" element={<CreateEvent />} />
        <Route path="events/edit/:id" element={<CreateEvent />} />
        <Route path="attendance" element={<Attendance />} />
        <Route path="participants" element={<Participants />} />
      </Route>

      {/* Fallback */}
      <Route path="*" element={<Navigate to="/events" replace />} />
    </Routes>
  );
};