import React, { useEffect, useState } from "react";
import { useSearchParams, Link } from "react-router-dom";
import {
  UserCheck,
  Search,
  CheckCircle2,
  XCircle,
  Clock,
  Calendar,
  Users,
  AlertCircle,
  Copy,
  Check,
  ChevronDown,
  Sparkles,
} from "lucide-react";
import { eventService } from "../../services/eventService";
import { registrationService } from "../../services/registrationService";
import { attendanceService } from "../../services/attendanceService";
import { LoadingSpinner } from "../../components/common/LoadingSpinner";
import { EmptyState } from "../../components/common/EmptyState";
import { Notification } from "../../components/common/Notification";

export const Attendance = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const eventIdParam = searchParams.get("eventId");

  const [events, setEvents] = useState([]);
  const [selectedEventId, setSelectedEventId] = useState(eventIdParam || "");
  const [registrations, setRegistrations] = useState([]);
  const [loadingEvents, setLoadingEvents] = useState(true);
  const [loadingRegs, setLoadingRegs] = useState(false);
  const [search, setSearch] = useState("");
  const [notification, setNotification] = useState(null);
  const [markingId, setMarkingId] = useState(null);
  const [copiedCode, setCopiedCode] = useState(null);

  // Load events list for the selector
  useEffect(() => {
    const fetchEvents = async () => {
      try {
        setLoadingEvents(true);
        const data = await eventService.getAllEventsForAdmin();
        setEvents(data || []);
        // If no eventId in query params, default to first event
        if (!selectedEventId && data && data.length > 0) {
          setSelectedEventId(String(data[0].id));
          setSearchParams({ eventId: String(data[0].id) });
        }
      } catch (err) {
        setNotification({
          type: "error",
          message: err.response?.data?.message || "Failed to load events.",
        });
      } finally {
        setLoadingEvents(false);
      }
    };
    fetchEvents();
  }, []);

  // Fetch registrations when selectedEventId or search changes
  const fetchParticipants = async (eventId, searchQuery) => {
    if (!eventId) return;
    try {
      setLoadingRegs(true);
      const data = await registrationService.getEventRegistrationsForAdmin(eventId, searchQuery);
      setRegistrations(data || []);
    } catch (err) {
      setNotification({
        type: "error",
        message: err.response?.data?.message || "Failed to load participants for this event.",
      });
    } finally {
      setLoadingRegs(false);
    }
  };

  useEffect(() => {
    if (selectedEventId) {
      fetchParticipants(selectedEventId, search);
    }
  }, [selectedEventId, search]);

  const handleEventChange = (e) => {
    const newId = e.target.value;
    setSelectedEventId(newId);
    setSearchParams(newId ? { eventId: newId } : {});
  };

  const handleToggleAttendance = async (reg, newStatus) => {
    try {
      setMarkingId(reg.id);
      await attendanceService.markAttendance(reg.id, newStatus);
      setNotification({
        type: "success",
        message: `Attendance marked as ${newStatus} for ${reg.userName}.`,
      });
      // Refresh participants
      await fetchParticipants(selectedEventId, search);
    } catch (err) {
      setNotification({
        type: "error",
        message: err.response?.data?.message || `Failed to update attendance status.`,
      });
    } finally {
      setMarkingId(null);
    }
  };

  const handleCopy = (code) => {
    navigator.clipboard.writeText(code);
    setCopiedCode(code);
    setTimeout(() => setCopiedCode(null), 2000);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "TBA";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const selectedEvent = events.find((e) => String(e.id) === String(selectedEventId));
  const totalCount = registrations.length;
  const presentCount = registrations.filter((r) => r.attendanceStatus === "PRESENT").length;
  const attendanceRate = totalCount > 0 ? Math.round((presentCount / totalCount) * 100) : 0;

  return (
    <div className="space-y-6">
      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-slate-200 pb-5">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-emerald-100 text-emerald-800 text-xs font-semibold mb-1">
            <UserCheck className="w-3.5 h-3.5" />
            <span>Check-in Desk</span>
          </div>
          <h1 className="text-2xl font-bold text-slate-900 tracking-tight">
            Attendance & Participant Management
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 mt-0.5">
            Search registered students by pass code or name and verify check-in attendance.
          </p>
        </div>
      </div>

      {/* Event Selector & Search Control Card (Inspired by Image 2 & 3) */}
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xs p-5 space-y-4">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* Event Dropdown */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
              Select Campus Event
            </label>
            <div className="relative">
              <select
                value={selectedEventId}
                onChange={handleEventChange}
                disabled={loadingEvents}
                className="w-full px-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-800 appearance-none focus:outline-hidden focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition cursor-pointer"
              >
                {events.map((ev) => (
                  <option key={ev.id} value={ev.id}>
                    {ev.title} ({ev.status}) — {ev.registeredCount || 0}/{ev.maxParticipants} registered
                  </option>
                ))}
              </select>
              <ChevronDown className="w-4 h-4 text-slate-400 absolute right-3.5 top-1/2 -translate-y-1/2 pointer-events-none" />
            </div>
          </div>

          {/* Search Bar */}
          <div>
            <label className="block text-xs font-bold uppercase tracking-wider text-slate-700 mb-1.5">
              Search Participants
            </label>
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search by student name, email, or pass code..."
                className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 placeholder-slate-400 focus:outline-hidden focus:ring-2 focus:ring-orange-500/20 focus:border-orange-500 transition"
              />
            </div>
          </div>
        </div>

        {/* Real-time Event Attendance Stats Bar */}
        {selectedEvent && (
          <div className="pt-3 border-t border-slate-100 flex flex-wrap items-center justify-between gap-4 text-xs">
            <div className="flex items-center gap-6">
              <div>
                <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                  Registered
                </span>
                <span className="font-extrabold text-slate-900 text-sm">{totalCount}</span>
              </div>
              <div className="border-l border-slate-200 pl-4">
                <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                  Present
                </span>
                <span className="font-extrabold text-emerald-600 text-sm">{presentCount}</span>
              </div>
              <div className="border-l border-slate-200 pl-4">
                <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                  Pending Check-in
                </span>
                <span className="font-extrabold text-slate-600 text-sm">
                  {Math.max(0, totalCount - presentCount)}
                </span>
              </div>
            </div>

            <div className="flex items-center gap-3 w-full sm:w-auto">
              <div className="w-36 bg-slate-100 rounded-full h-2 overflow-hidden">
                <div
                  className="bg-emerald-500 h-full rounded-full transition-all duration-500"
                  style={{ width: `${attendanceRate}%` }}
                ></div>
              </div>
              <span className="font-bold text-xs text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded-full border border-emerald-200">
                {attendanceRate}% checked in
              </span>
            </div>
          </div>
        )}
      </div>

      {/* Participants Table (Inspired by RegFox Image 3 & Canapii Image 2) */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        {loadingRegs ? (
          <div className="py-16">
            <LoadingSpinner text="Searching registered participants..." />
          </div>
        ) : registrations.length === 0 ? (
          <EmptyState
            icon={Users}
            title="No Participants Found"
            description={
              search
                ? `No participants matched "${search}".`
                : "No students have registered for this event yet."
            }
          />
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/80 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-200/70">
                  <th className="py-3.5 px-6">Participant</th>
                  <th className="py-3.5 px-6">Pass Code</th>
                  <th className="py-3.5 px-6">Registered On</th>
                  <th className="py-3.5 px-6">Status</th>
                  <th className="py-3.5 px-6">Attendance</th>
                  <th className="py-3.5 px-6 text-right">Check-in Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {registrations.map((reg) => {
                  const isPresent = reg.attendanceStatus === "PRESENT";
                  const isCancelled = reg.status === "CANCELLED";
                  const isMarking = markingId === reg.id;

                  // Initials for avatar like Image 2 Canapii
                  const initials = reg.userName
                    ? reg.userName
                        .split(" ")
                        .map((n) => n[0])
                        .slice(0, 2)
                        .join("")
                        .toUpperCase()
                    : "ST";

                  return (
                    <tr
                      key={reg.id}
                      className={`transition ${
                        isCancelled
                          ? "bg-slate-50/50 opacity-60"
                          : isPresent
                          ? "bg-emerald-50/20 hover:bg-emerald-50/30"
                          : "hover:bg-slate-50/50"
                      }`}
                    >
                      {/* Participant with Avatar (Inspired by Image 2) */}
                      <td className="py-4 px-6">
                        <div className="flex items-center gap-3">
                          <div
                            className={`w-8 h-8 rounded-full flex items-center justify-center font-bold text-xs shrink-0 ${
                              isPresent
                                ? "bg-emerald-100 text-emerald-800"
                                : "bg-orange-100 text-orange-800"
                            }`}
                          >
                            {initials}
                          </div>
                          <div>
                            <p className="font-bold text-slate-900 line-clamp-1">{reg.userName}</p>
                            <p className="text-[11px] text-slate-400 line-clamp-1">
                              {reg.userEmail}
                            </p>
                          </div>
                        </div>
                      </td>

                      {/* Pass Code with 1-click Copy */}
                      <td className="py-4 px-6 font-mono font-bold text-slate-700">
                        <div className="inline-flex items-center gap-1.5 bg-slate-100 px-2 py-1 rounded-lg">
                          <span>{reg.registrationCode}</span>
                          <button
                            onClick={() => handleCopy(reg.registrationCode)}
                            title="Copy code"
                            className="text-slate-400 hover:text-slate-700 cursor-pointer"
                          >
                            {copiedCode === reg.registrationCode ? (
                              <Check className="w-3.5 h-3.5 text-emerald-600" />
                            ) : (
                              <Copy className="w-3.5 h-3.5" />
                            )}
                          </button>
                        </div>
                      </td>

                      {/* Registered Date */}
                      <td className="py-4 px-6 text-slate-500">{formatDate(reg.registeredAt)}</td>

                      {/* Registration Status */}
                      <td className="py-4 px-6">
                        <span
                          className={`inline-flex px-2 py-0.5 rounded-full text-[10px] font-bold uppercase ${
                            reg.status === "REGISTERED"
                              ? "bg-blue-50 text-blue-700 border border-blue-200"
                              : "bg-rose-50 text-rose-700 border border-rose-200"
                          }`}
                        >
                          {reg.status}
                        </span>
                      </td>

                      {/* Attendance Status Badge */}
                      <td className="py-4 px-6">
                        {isPresent ? (
                          <div className="space-y-0.5">
                            <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-800 border border-emerald-300">
                              <CheckCircle2 className="w-3 h-3 text-emerald-600" />
                              PRESENT
                            </span>
                            {reg.attendanceTime && (
                              <p className="text-[10px] text-slate-400">
                                {formatDate(reg.attendanceTime)}
                              </p>
                            )}
                          </div>
                        ) : (
                          <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-[10px] font-medium bg-slate-100 text-slate-500">
                            <Clock className="w-3 h-3 text-slate-400" />
                            Not Checked In
                          </span>
                        )}
                      </td>

                      {/* Check-in Action Button */}
                      <td className="py-4 px-6 text-right">
                        {isCancelled ? (
                          <span className="text-[11px] text-slate-400 italic">
                            Cancelled Ticket
                          </span>
                        ) : isPresent ? (
                          <button
                            onClick={() => handleToggleAttendance(reg, "ABSENT")}
                            disabled={isMarking}
                            className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-semibold text-rose-600 hover:text-rose-700 hover:bg-rose-50 border border-rose-200 rounded-lg transition cursor-pointer disabled:opacity-50"
                            title="Unmark / Reset Attendance"
                          >
                            <XCircle className="w-3.5 h-3.5" />
                            <span>Mark Absent</span>
                          </button>
                        ) : (
                          <button
                            onClick={() => handleToggleAttendance(reg, "PRESENT")}
                            disabled={isMarking}
                            className="inline-flex items-center gap-1 px-3.5 py-1.5 text-xs font-semibold text-white bg-emerald-600 hover:bg-emerald-700 rounded-lg shadow-xs transition cursor-pointer disabled:opacity-50"
                          >
                            <CheckCircle2 className="w-3.5 h-3.5" />
                            <span>{isMarking ? "Marking..." : "Mark Present"}</span>
                          </button>
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
    </div>
  );
};