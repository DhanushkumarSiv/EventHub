import React, { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import {
  Calendar,
  MapPin,
  Clock,
  Users,
  FileText,
  ArrowLeft,
  Sparkles,
  Save,
  AlertCircle,
} from "lucide-react";
import { eventService } from "../../services/eventService";
import { LoadingSpinner } from "../../components/common/LoadingSpinner";
import { Notification } from "../../components/common/Notification";

export const CreateEvent = () => {
  const { id } = useParams(); // If present, edit mode
  const isEditMode = Boolean(id);
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    title: "",
    description: "",
    venue: "",
    eventDate: "",
    registrationDeadline: "",
    maxParticipants: 100,
  });

  const [loading, setLoading] = useState(isEditMode);
  const [submitting, setSubmitting] = useState(false);
  const [notification, setNotification] = useState(null);
  const [validationError, setValidationError] = useState("");

  // Convert LocalDateTime ISO string to datetime-local format: YYYY-MM-DDTHH:mm
  const toInputDatetime = (isoStr) => {
    if (!isoStr) return "";
    const date = new Date(isoStr);
    const pad = (num) => String(num).padStart(2, "0");
    const year = date.getFullYear();
    const month = pad(date.getMonth() + 1);
    const day = pad(date.getDate());
    const hours = pad(date.getHours());
    const minutes = pad(date.getMinutes());
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  useEffect(() => {
    if (isEditMode) {
      const fetchEvent = async () => {
        try {
          setLoading(true);
          const data = await eventService.getEventByIdForAdmin(id);
          setFormData({
            title: data.title || "",
            description: data.description || "",
            venue: data.venue || "",
            eventDate: toInputDatetime(data.eventDate),
            registrationDeadline: toInputDatetime(data.registrationDeadline),
            maxParticipants: data.maxParticipants || 100,
          });
        } catch (err) {
          setNotification({
            type: "error",
            message: err.response?.data?.message || "Failed to load event details.",
          });
        } finally {
          setLoading(false);
        }
      };
      fetchEvent();
    }
  }, [id, isEditMode]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === "maxParticipants" ? parseInt(value, 10) || "" : value,
    }));
    setValidationError("");
  };

  const validate = () => {
    if (!formData.title.trim()) return "Event title is required.";
    if (!formData.description.trim()) return "Event description is required.";
    if (!formData.venue.trim()) return "Venue location is required.";
    if (!formData.eventDate) return "Event date and time is required.";
    if (!formData.registrationDeadline) return "Registration deadline is required.";
    if (!formData.maxParticipants || formData.maxParticipants <= 0) {
      return "Maximum participants must be at least 1.";
    }

    const eventTime = new Date(formData.eventDate).getTime();
    const deadlineTime = new Date(formData.registrationDeadline).getTime();

    if (deadlineTime >= eventTime) {
      return "Registration deadline must be strictly before the event start date.";
    }

    return null;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errorMsg = validate();
    if (errorMsg) {
      setValidationError(errorMsg);
      return;
    }

    try {
      setSubmitting(true);
      setValidationError("");

      const payload = {
        title: formData.title.trim(),
        description: formData.description.trim(),
        venue: formData.venue.trim(),
        eventDate: new Date(formData.eventDate).toISOString(),
        registrationDeadline: new Date(formData.registrationDeadline).toISOString(),
        maxParticipants: Number(formData.maxParticipants),
      };

      if (isEditMode) {
        await eventService.updateEvent(id, payload);
      } else {
        await eventService.createEvent(payload);
      }

      navigate("/admin/events");
    } catch (err) {
      setNotification({
        type: "error",
        message:
          err.response?.data?.message ||
          `Failed to ${isEditMode ? "update" : "create"} event. Please verify all fields.`,
      });
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return (
      <div className="py-20">
        <LoadingSpinner text="Fetching event details..." />
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}

      {/* Back button & Page title */}
      <div className="flex items-center justify-between border-b border-slate-200 pb-5">
        <div className="flex items-center gap-3">
          <Link
            to="/admin/events"
            className="p-2 text-slate-500 hover:text-slate-900 hover:bg-slate-100 rounded-xl transition"
          >
            <ArrowLeft className="w-5 h-5" />
          </Link>
          <div>
            <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
              {isEditMode ? "Edit Campus Event" : "Create New Campus Event"}
            </h1>
            <p className="text-xs text-slate-500 mt-0.5">
              {isEditMode
                ? "Update event schedules, venue, and capacity limits."
                : "Fill out the information below to schedule a new campus event."}
            </p>
          </div>
        </div>
      </div>

      {validationError && (
        <div className="p-3.5 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-xs font-medium flex items-center gap-2">
          <AlertCircle className="w-4 h-4 shrink-0 text-rose-500" />
          <span>{validationError}</span>
        </div>
      )}

      {/* Form Card */}
      <form
        onSubmit={handleSubmit}
        className="bg-white rounded-2xl border border-slate-200/90 shadow-xs p-6 sm:p-8 space-y-6"
      >
        {/* Title */}
        <div>
          <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
            Event Title *
          </label>
          <div className="relative">
            <FileText className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              name="title"
              value={formData.title}
              onChange={handleChange}
              placeholder="e.g., Hackathon 2026: Build the Future"
              className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
              required
            />
          </div>
        </div>

        {/* Description */}
        <div>
          <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
            Event Description *
          </label>
          <textarea
            name="description"
            rows="4"
            value={formData.description}
            onChange={handleChange}
            placeholder="Detailed overview, prerequisites, eligibility, and agenda..."
            className="w-full p-3.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
            required
          ></textarea>
        </div>

        {/* Venue & Capacity Row */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
              Venue / Location *
            </label>
            <div className="relative">
              <MapPin className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                name="venue"
                value={formData.venue}
                onChange={handleChange}
                placeholder="e.g., Main Auditorium / Lab 3"
                className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
                required
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
              Max Seat Capacity *
            </label>
            <div className="relative">
              <Users className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="number"
                name="maxParticipants"
                min="1"
                value={formData.maxParticipants}
                onChange={handleChange}
                placeholder="100"
                className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
                required
              />
            </div>
          </div>
        </div>

        {/* Dates Row */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
              Registration Deadline *
            </label>
            <div className="relative">
              <Clock className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="datetime-local"
                name="registrationDeadline"
                value={formData.registrationDeadline}
                onChange={handleChange}
                className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
                required
              />
            </div>
            <p className="text-[11px] text-slate-400 mt-1">Must be before event date.</p>
          </div>

          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
              Event Date & Time *
            </label>
            <div className="relative">
              <Calendar className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="datetime-local"
                name="eventDate"
                value={formData.eventDate}
                onChange={handleChange}
                className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-sm text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
                required
              />
            </div>
            <p className="text-[11px] text-slate-400 mt-1">When the event commences.</p>
          </div>
        </div>

        {/* Submit and Cancel Buttons */}
        <div className="pt-4 border-t border-slate-100 flex items-center justify-end gap-3">
          <Link
            to="/admin/events"
            className="px-5 py-2.5 rounded-xl text-xs font-semibold text-slate-600 hover:text-slate-900 hover:bg-slate-100 transition"
          >
            Cancel
          </Link>
          <button
            type="submit"
            disabled={submitting}
            className="inline-flex items-center gap-2 px-6 py-2.5 bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 text-white font-semibold text-xs rounded-xl shadow-xs shadow-orange-500/20 transition cursor-pointer disabled:opacity-50"
          >
            <Save className="w-4 h-4" />
            <span>
              {submitting
                ? "Saving..."
                : isEditMode
                ? "Save Event Changes"
                : "Create Event (Draft)"}
            </span>
          </button>
        </div>
      </form>
    </div>
  );
};