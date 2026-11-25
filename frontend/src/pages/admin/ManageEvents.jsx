import React, { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  Calendar,
  Search,
  Plus,
  Send,
  XCircle,
  Trash2,
  Edit3,
  Users,
  Clock,
  MapPin,
  CheckCircle2,
  AlertTriangle,
  Eye,
} from "lucide-react";
import { eventService } from "../../services/eventService";
import { LoadingSpinner } from "../../components/common/LoadingSpinner";
import { EmptyState } from "../../components/common/EmptyState";
import { ConfirmModal } from "../../components/common/ConfirmModal";
import { Notification } from "../../components/common/Notification";

export const ManageEvents = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("ALL");
  const [notification, setNotification] = useState(null);

  // Modal actions
  const [activeModal, setActiveModal] = useState(null); // { type: 'PUBLISH' | 'CANCEL' | 'DELETE', event }
  const [isSubmitting, setIsSubmitting] = useState(false);

  const navigate = useNavigate();

  const fetchEvents = async () => {
    try {
      setLoading(true);
      const data = await eventService.getAllEventsForAdmin();
      setEvents(data || []);
    } catch (err) {
      setNotification({
        type: "error",
        message: err.response?.data?.message || "Failed to load events.",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const handleActionConfirm = async () => {
    if (!activeModal) return;
    const { type, event } = activeModal;
    try {
      setIsSubmitting(true);
      if (type === "PUBLISH") {
        await eventService.publishEvent(event.id);
        setNotification({
          type: "success",
          message: `Event "${event.title}" is now PUBLISHED and live on the student portal!`,
        });
      } else if (type === "CANCEL") {
        await eventService.cancelEvent(event.id);
        setNotification({
          type: "warning",
          message: `Event "${event.title}" has been CANCELLED.`,
        });
      } else if (type === "DELETE") {
        await eventService.deleteEvent(event.id);
        setNotification({
          type: "success",
          message: `Draft event "${event.title}" was permanently deleted.`,
        });
      }
      setActiveModal(null);
      await fetchEvents();
    } catch (err) {
      setNotification({
        type: "error",
        message: err.response?.data?.message || `Failed to ${type.toLowerCase()} event.`,
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "TBA";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const filteredEvents = events.filter((event) => {
    const matchesStatus = statusFilter === "ALL" || event.status === statusFilter;
    const matchesSearch =
      event.title.toLowerCase().includes(search.toLowerCase()) ||
      (event.venue && event.venue.toLowerCase().includes(search.toLowerCase()));
    return matchesStatus && matchesSearch;
  });

  return (
    <div className="space-y-6">
      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}

      {/* Header Area (Inspired by RegFox Image 3) */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-slate-200 pb-5">
        <div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
            Event Management
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            Create, publish, edit, or cancel campus events and track registration capacities.
          </p>
        </div>

        <Link
          to="/admin/events/new"
          className="inline-flex items-center gap-2 px-4 py-2.5 bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 text-white font-semibold text-xs rounded-xl shadow-xs shadow-orange-500/20 transition self-start sm:self-auto cursor-pointer"
        >
          <Plus className="w-4 h-4" />
          <span>Add New Event</span>
        </Link>
      </div>

      {/* Filter and Search Bar (Inspired by RegFox Image 3) */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-3.5 rounded-2xl border border-slate-200 shadow-xs">
        {/* Status Filters */}
        <div className="flex items-center gap-1.5 overflow-x-auto pb-1 md:pb-0">
          {[
            { label: "View All", value: "ALL", count: events.length },
            {
              label: "Published",
              value: "PUBLISHED",
              count: events.filter((e) => e.status === "PUBLISHED").length,
            },
            {
              label: "Drafts",
              value: "DRAFT",
              count: events.filter((e) => e.status === "DRAFT").length,
            },
            {
              label: "Cancelled",
              value: "CANCELLED",
              count: events.filter((e) => e.status === "CANCELLED").length,
            },
          ].map((pill) => (
            <button
              key={pill.value}
              onClick={() => setStatusFilter(pill.value)}
              className={`px-3 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition cursor-pointer flex items-center gap-1.5 ${
                statusFilter === pill.value
                  ? "bg-slate-900 text-white shadow-xs"
                  : "bg-slate-50 text-slate-600 hover:bg-slate-100"
              }`}
            >
              <span>{pill.label}</span>
              <span
                className={`px-1.5 py-0.2 rounded-full text-[10px] ${
                  statusFilter === pill.value
                    ? "bg-slate-700 text-white"
                    : "bg-slate-200 text-slate-600"
                }`}
              >
                {pill.count}
              </span>
            </button>
          ))}
        </div>

        {/* Search Input */}
        <div className="relative min-w-[260px]">
          <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search by title or venue..."
            className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
          />
        </div>
      </div>

      {/* Events Table (Inspired by RegFox Image 3) */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        {loading ? (
          <div className="py-16">
            <LoadingSpinner text="Loading events catalog..." />
          </div>
        ) : filteredEvents.length === 0 ? (
          <EmptyState
            icon={Calendar}
            title="No Events Found"
            description={
              search
                ? `No events match "${search}". Try adjusting your search query.`
                : "No events found in this status category."
            }
            actionLabel="Create Event"
            onAction={() => navigate("/admin/events/new")}
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/80 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-200/70">
                  <th className="py-3.5 px-6">Event Details</th>
                  <th className="py-3.5 px-6">Schedule & Venue</th>
                  <th className="py-3.5 px-6">Capacity / Regs</th>
                  <th className="py-3.5 px-6">Status</th>
                  <th className="py-3.5 px-6 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {filteredEvents.map((event) => {
                  const percent = Math.min(
                    100,
                    Math.round(
                      ((event.registeredCount || 0) / Math.max(1, event.maxParticipants)) * 100
                    )
                  );

                  return (
                    <tr key={event.id} className="hover:bg-slate-50/50 transition">
                      {/* Title & Description */}
                      <td className="py-4 px-6 max-w-xs">
                        <Link
                          to={`/events/${event.id}`}
                          className="font-bold text-slate-900 hover:text-orange-600 transition line-clamp-1 text-sm"
                        >
                          {event.title}
                        </Link>
                        <p className="text-slate-400 text-[11px] line-clamp-1 mt-0.5">
                          {event.description}
                        </p>
                      </td>

                      {/* Schedule & Venue */}
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-1.5 text-slate-700 font-medium">
                          <Clock className="w-3.5 h-3.5 text-orange-500 shrink-0" />
                          <span>{formatDate(event.eventDate)}</span>
                        </div>
                        <div className="flex items-center gap-1.5 text-slate-400 text-[11px] mt-0.5">
                          <MapPin className="w-3.5 h-3.5 shrink-0" />
                          <span className="truncate">{event.venue || "Campus Venue"}</span>
                        </div>
                        <p className="text-[10px] text-slate-400 mt-1">
                          Deadline: {formatDate(event.registrationDeadline)}
                        </p>
                      </td>

                      {/* Capacity Bar */}
                      <td className="py-4 px-6 min-w-[140px]">
                        <div className="flex items-center justify-between text-[11px] font-semibold text-slate-700 mb-1">
                          <span>
                            {event.registeredCount || 0} / {event.maxParticipants}
                          </span>
                          <span className="text-slate-500">{percent}%</span>
                        </div>
                        <div className="w-full bg-slate-100 rounded-full h-1.5 overflow-hidden">
                          <div
                            className={`h-full rounded-full transition-all duration-300 ${
                              percent >= 100
                                ? "bg-rose-500"
                                : percent >= 80
                                ? "bg-amber-500"
                                : "bg-orange-500"
                            }`}
                            style={{ width: `${percent}%` }}
                          ></div>
                        </div>
                      </td>

                      {/* Status */}
                      <td className="py-4 px-6">
                        <span
                          className={`inline-flex px-2.5 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wide ${
                            event.status === "PUBLISHED"
                              ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                              : event.status === "DRAFT"
                              ? "bg-slate-100 text-slate-600 border border-slate-200"
                              : event.status === "CANCELLED"
                              ? "bg-rose-50 text-rose-700 border border-rose-200"
                              : "bg-blue-50 text-blue-700 border border-blue-200"
                          }`}
                        >
                          {event.status}
                        </span>
                      </td>

                      {/* Actions */}
                      <td className="py-4 px-6 text-right space-x-1.5 shrink-0 whitespace-nowrap">
                        {/* If DRAFT: Can publish, edit, delete */}
                        {event.status === "DRAFT" && (
                          <>
                            <button
                              onClick={() => setActiveModal({ type: "PUBLISH", event })}
                              title="Publish Event to Students"
                              className="inline-flex items-center gap-1 px-2.5 py-1.5 bg-emerald-50 text-emerald-700 hover:bg-emerald-100 font-semibold rounded-lg text-xs transition cursor-pointer"
                            >
                              <Send className="w-3.5 h-3.5" />
                              <span>Publish</span>
                            </button>
                            <Link
                              to={`/admin/events/edit/${event.id}`}
                              title="Edit Event"
                              className="inline-flex items-center gap-1 px-2 py-1.5 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg text-xs transition"
                            >
                              <Edit3 className="w-3.5 h-3.5" />
                            </Link>
                            <button
                              onClick={() => setActiveModal({ type: "DELETE", event })}
                              title="Delete Draft"
                              className="inline-flex items-center gap-1 px-2 py-1.5 text-rose-600 hover:text-rose-700 hover:bg-rose-50 rounded-lg text-xs transition cursor-pointer"
                            >
                              <Trash2 className="w-3.5 h-3.5" />
                            </button>
                          </>
                        )}

                        {/* If PUBLISHED: Can edit, cancel, attendance */}
                        {event.status === "PUBLISHED" && (
                          <>
                            <Link
                              to={`/admin/attendance?eventId=${event.id}`}
                              title="Check Attendance"
                              className="inline-flex items-center gap-1 px-2.5 py-1.5 bg-orange-50 text-orange-700 hover:bg-orange-100 font-semibold rounded-lg text-xs transition"
                            >
                              <Users className="w-3.5 h-3.5" />
                              <span>Attendance</span>
                            </Link>
                            <Link
                              to={`/admin/events/edit/${event.id}`}
                              title="Edit Event"
                              className="inline-flex items-center gap-1 px-2 py-1.5 text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-lg text-xs transition"
                            >
                              <Edit3 className="w-3.5 h-3.5" />
                            </Link>
                            <button
                              onClick={() => setActiveModal({ type: "CANCEL", event })}
                              title="Cancel Event"
                              className="inline-flex items-center gap-1 px-2 py-1.5 text-rose-600 hover:text-rose-700 hover:bg-rose-50 rounded-lg text-xs transition cursor-pointer"
                            >
                              <XCircle className="w-3.5 h-3.5" />
                            </button>
                          </>
                        )}

                        {/* If CANCELLED or other: View attendance */}
                        {event.status === "CANCELLED" && (
                          <Link
                            to={`/admin/attendance?eventId=${event.id}`}
                            className="inline-flex items-center gap-1 px-2.5 py-1.5 bg-slate-100 text-slate-600 hover:bg-slate-200 font-semibold rounded-lg text-xs transition"
                          >
                            <Users className="w-3.5 h-3.5" />
                            <span>Participants</span>
                          </Link>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Confirmation Modals for Publish, Cancel, Delete */}
      <ConfirmModal
        isOpen={activeModal?.type === "PUBLISH"}
        title="Publish Campus Event?"
        message={`Are you sure you want to publish "${activeModal?.event?.title}"? Once published, students will immediately be able to view and register for this event on the portal.`}
        confirmLabel={isSubmitting ? "Publishing..." : "Yes, Publish Event"}
        cancelLabel="Keep as Draft"
        confirmVariant="primary"
        onConfirm={handleActionConfirm}
        onCancel={() => setActiveModal(null)}
      />

      <ConfirmModal
        isOpen={activeModal?.type === "CANCEL"}
        title="Cancel This Event?"
        message={`Are you sure you want to cancel "${activeModal?.event?.title}"? Once cancelled, no new registrations will be accepted. Existing student passes will be marked for a cancelled event.`}
        confirmLabel={isSubmitting ? "Cancelling..." : "Yes, Cancel Event"}
        cancelLabel="Go Back"
        confirmVariant="danger"
        onConfirm={handleActionConfirm}
        onCancel={() => setActiveModal(null)}
      />

      <ConfirmModal
        isOpen={activeModal?.type === "DELETE"}
        title="Delete Draft Event?"
        message={`Are you sure you want to permanently delete draft event "${activeModal?.event?.title}"? This action cannot be undone.`}
        confirmLabel={isSubmitting ? "Deleting..." : "Yes, Delete Draft"}
        cancelLabel="Cancel"
        confirmVariant="danger"
        onConfirm={handleActionConfirm}
        onCancel={() => setActiveModal(null)}
      />
    </div>
  );
};