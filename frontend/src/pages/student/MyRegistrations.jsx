import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Ticket,
  Calendar,
  MapPin,
  Clock,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Copy,
  Check,
  Trash2,
  ArrowRight,
  ShieldCheck,
} from "lucide-react";
import { registrationService } from "../../services/registrationService";
import { LoadingSpinner } from "../../components/common/LoadingSpinner";
import { EmptyState } from "../../components/common/EmptyState";
import { ConfirmModal } from "../../components/common/ConfirmModal";
import { Notification } from "../../components/common/Notification";

export const MyRegistrations = () => {
  const [registrations, setRegistrations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [notification, setNotification] = useState(null);
  const [filter, setFilter] = useState("ALL"); // ALL, ACTIVE, CANCELLED
  const [cancellingReg, setCancellingReg] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [copiedCode, setCopiedCode] = useState(null);

  const fetchRegistrations = async () => {
    try {
      setLoading(true);
      const data = await registrationService.getMyRegistrations();
      setRegistrations(data || []);
    } catch (err) {
      setNotification({
        type: "error",
        message: err.response?.data?.message || "Failed to load your event registrations.",
      });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchRegistrations();
  }, []);

  const handleCopyCode = (code) => {
    navigator.clipboard.writeText(code);
    setCopiedCode(code);
    setTimeout(() => setCopiedCode(null), 2000);
  };

  const handleConfirmCancel = async () => {
    if (!cancellingReg) return;
    try {
      setIsSubmitting(true);
      await registrationService.cancelRegistration(cancellingReg.id);
      setNotification({
        type: "success",
        message: `Your registration for "${cancellingReg.eventTitle}" was cancelled successfully.`,
      });
      setCancellingReg(null);
      await fetchRegistrations();
    } catch (err) {
      setNotification({
        type: "error",
        message: err.response?.data?.message || "Failed to cancel registration.",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return "TBA";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-US", {
      weekday: "short",
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  const filteredRegistrations = registrations.filter((reg) => {
    if (filter === "ACTIVE") return reg.status === "REGISTERED";
    if (filter === "CANCELLED") return reg.status === "CANCELLED";
    return true;
  });

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto space-y-8">
        {notification && (
          <Notification
            type={notification.type}
            message={notification.message}
            onClose={() => setNotification(null)}
          />
        )}

        {/* Page Header */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 border-b border-slate-200 pb-6">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-orange-100/80 text-orange-700 text-xs font-semibold mb-2">
              <Ticket className="w-3.5 h-3.5" />
              <span>Student Passbook</span>
            </div>
            <h1 className="text-3xl font-extrabold text-slate-900 tracking-tight">
              My Registrations
            </h1>
            <p className="mt-1 text-sm text-slate-500">
              Manage your event passes, check attendance status, or cancel bookings.
            </p>
          </div>

          <Link
            to="/events"
            className="inline-flex items-center gap-2 px-4 py-2.5 bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 text-white font-medium text-sm rounded-xl shadow-xs shadow-orange-500/20 transition self-start sm:self-auto"
          >
            <span>Explore More Events</span>
            <ArrowRight className="w-4 h-4" />
          </Link>
        </div>

        {/* Filter Pills */}
        <div className="flex items-center gap-2">
          {[
            { label: "All Tickets", value: "ALL", count: registrations.length },
            {
              label: "Active",
              value: "ACTIVE",
              count: registrations.filter((r) => r.status === "REGISTERED").length,
            },
            {
              label: "Cancelled",
              value: "CANCELLED",
              count: registrations.filter((r) => r.status === "CANCELLED").length,
            },
          ].map((item) => (
            <button
              key={item.value}
              onClick={() => setFilter(item.value)}
              className={`px-4 py-1.5 rounded-full text-xs font-semibold transition cursor-pointer flex items-center gap-1.5 ${
                filter === item.value
                  ? "bg-slate-900 text-white shadow-xs"
                  : "bg-white text-slate-600 border border-slate-200 hover:bg-slate-100"
              }`}
            >
              <span>{item.label}</span>
              <span
                className={`px-1.5 py-0.2 rounded-full text-[10px] ${
                  filter === item.value
                    ? "bg-slate-700 text-white"
                    : "bg-slate-100 text-slate-600"
                }`}
              >
                {item.count}
              </span>
            </button>
          ))}
        </div>

        {/* Content Section */}
        {loading ? (
          <LoadingSpinner text="Retrieving your registered passes..." />
        ) : filteredRegistrations.length === 0 ? (
          <EmptyState
            icon={Ticket}
            title={
              filter === "ALL"
                ? "No Event Registrations Yet"
                : `No ${filter.toLowerCase()} registrations`
            }
            description={
              filter === "ALL"
                ? "You haven't registered for any campus events yet. Explore upcoming hackathons, tech talks, and cultural fests!"
                : `You don't have any tickets in the ${filter.toLowerCase()} category.`
            }
            actionLabel={filter === "ALL" ? "Browse Upcoming Events" : undefined}
            onAction={filter === "ALL" ? () => window.location.assign("/events") : undefined}
          />
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {filteredRegistrations.map((reg) => {
              const isRegistered = reg.status === "REGISTERED";
              const isPresent = reg.attendanceStatus === "PRESENT";

              return (
                <div
                  key={reg.id}
                  className={`bg-white rounded-2xl border transition-all duration-200 hover:shadow-lg overflow-hidden flex flex-col justify-between ${
                    isRegistered
                      ? "border-slate-200 hover:border-orange-300"
                      : "border-slate-200/60 opacity-70 bg-slate-50/50"
                  }`}
                >
                  {/* Ticket Header Banner */}
                  <div>
                    <div
                      className={`p-4 flex items-center justify-between text-white ${
                        isRegistered
                          ? "bg-gradient-to-r from-orange-500 via-amber-500 to-amber-600"
                          : "bg-slate-600"
                      }`}
                    >
                      <div className="flex items-center gap-2">
                        <Ticket className="w-4 h-4" />
                        <span className="text-xs font-bold tracking-wider uppercase">
                          Event Pass
                        </span>
                      </div>
                      <span
                        className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold tracking-wide uppercase ${
                          isRegistered
                            ? "bg-white/20 text-white backdrop-blur-xs"
                            : "bg-rose-500/80 text-white"
                        }`}
                      >
                        {reg.status}
                      </span>
                    </div>

                    <div className="p-5 space-y-4">
                      {/* Event Title */}
                      <div>
                        <Link
                          to={`/events/${reg.eventId}`}
                          className="font-bold text-lg text-slate-900 hover:text-orange-600 transition line-clamp-1"
                        >
                          {reg.eventTitle}
                        </Link>
                        <p className="text-xs text-slate-500 mt-1 flex items-center gap-1.5">
                          <span>Registered on:</span>
                          <span className="font-medium text-slate-700">
                            {formatDate(reg.registeredAt)}
                          </span>
                        </p>
                      </div>

                      {/* Event Details Grid */}
                      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs bg-slate-50 p-3 rounded-xl border border-slate-100">
                        <div className="flex items-center gap-2 text-slate-600">
                          <Calendar className="w-4 h-4 text-orange-500 shrink-0" />
                          <span className="truncate">{formatDate(reg.eventDate)}</span>
                        </div>
                        <div className="flex items-center gap-2 text-slate-600">
                          <MapPin className="w-4 h-4 text-orange-500 shrink-0" />
                          <span className="truncate">{reg.eventVenue || "Campus Venue"}</span>
                        </div>
                      </div>

                      {/* Registration Code Voucher Bar */}
                      <div className="bg-slate-900 text-white rounded-xl p-3.5 flex items-center justify-between border border-slate-800">
                        <div>
                          <p className="text-[10px] uppercase font-bold text-slate-400 tracking-wider">
                            Pass / Verification Code
                          </p>
                          <p className="font-mono font-bold text-sm text-amber-400 tracking-wider mt-0.5">
                            {reg.registrationCode}
                          </p>
                        </div>
                        <button
                          onClick={() => handleCopyCode(reg.registrationCode)}
                          className="p-2 bg-slate-800 hover:bg-slate-700 text-slate-300 hover:text-white rounded-lg transition cursor-pointer"
                          title="Copy Pass Code"
                        >
                          {copiedCode === reg.registrationCode ? (
                            <Check className="w-4 h-4 text-emerald-400" />
                          ) : (
                            <Copy className="w-4 h-4" />
                          )}
                        </button>
                      </div>

                      {/* Attendance Badge */}
                      <div className="flex items-center justify-between pt-1">
                        <span className="text-xs text-slate-500 font-medium">
                          Check-in Status:
                        </span>
                        {isPresent ? (
                          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                            <CheckCircle2 className="w-3.5 h-3.5 text-emerald-600" />
                            Checked In (PRESENT)
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-600 border border-slate-200">
                            <Clock className="w-3.5 h-3.5 text-slate-400" />
                            Not Checked In Yet
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Actions Footer */}
                  {isRegistered && (
                    <div className="p-4 bg-slate-50 border-t border-slate-100 flex items-center justify-between">
                      <Link
                        to={`/events/${reg.eventId}`}
                        className="text-xs font-semibold text-orange-600 hover:text-orange-700 transition"
                      >
                        View Event Page →
                      </Link>

                      <button
                        onClick={() => setCancellingReg(reg)}
                        className="inline-flex items-center gap-1 text-xs font-medium text-rose-600 hover:text-rose-700 hover:bg-rose-50 px-2.5 py-1.5 rounded-lg transition cursor-pointer"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                        Cancel Registration
                      </button>
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        )}

        {/* Cancellation Confirmation Dialog */}
        <ConfirmModal
          isOpen={!!cancellingReg}
          title="Cancel Event Registration?"
          message={`Are you sure you want to cancel your pass for "${cancellingReg?.eventTitle}"? Your registration code ${cancellingReg?.registrationCode} will be invalidated and your slot will be released back to the event capacity.`}
          confirmLabel={isSubmitting ? "Cancelling..." : "Yes, Cancel Registration"}
          cancelLabel="Keep My Registration"
          confirmVariant="danger"
          onConfirm={handleConfirmCancel}
          onCancel={() => setCancellingReg(null)}
        />
      </div>
    </div>
  );
};