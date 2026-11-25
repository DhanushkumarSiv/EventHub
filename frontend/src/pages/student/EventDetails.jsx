import React, { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import {
  Calendar,
  MapPin,
  Clock,
  Users,
  ArrowLeft,
  CheckCircle2,
  AlertCircle,
  Ticket,
  Sparkles,
  ShieldAlert,
} from "lucide-react";
import { eventService } from "../../services/eventService";
import { registrationService } from "../../services/registrationService";
import { useAuth } from "../../context/AuthContext";
import { LoadingSpinner } from "../../components/common/LoadingSpinner";
import { Notification } from "../../components/common/Notification";

export const EventDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated, isStudent, isAdmin } = useAuth();

  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(true);
  const [registering, setRegistering] = useState(false);
  const [error, setError] = useState("");
  const [successCode, setSuccessCode] = useState(null);
  const [notification, setNotification] = useState({ message: "", type: "success" });

  useEffect(() => {
    fetchEventDetails();
  }, [id]);

  const fetchEventDetails = async () => {
    setLoading(true);
    try {
      const data = await eventService.getPublishedEventById(id);
      setEvent(data);
    } catch (err) {
      setError("Event not found or is no longer published.");
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async () => {
    if (!isAuthenticated) {
      navigate("/login", { state: { from: { pathname: `/events/${id}` } } });
      return;
    }

    if (!isStudent) {
      setNotification({
        type: "error",
        message: "Only student accounts can register for campus events.",
      });
      return;
    }

    setRegistering(true);
    try {
      const result = await registrationService.registerForEvent(id);
      setSuccessCode(result.registrationCode);
      setNotification({
        type: "success",
        message: "Registration confirmed! Your registration code is " + result.registrationCode,
      });
      fetchEventDetails(); // Refresh capacity
    } catch (err) {
      const msg = err.response?.data?.message || "Failed to register for this event.";
      setNotification({ type: "error", message: msg });
    } finally {
      setRegistering(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading event details..." />;
  }

  if (error || !event) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-6 text-center">
        <div className="w-16 h-16 rounded-2xl bg-rose-50 text-rose-500 flex items-center justify-center mb-4">
          <AlertCircle className="w-8 h-8" />
        </div>
        <h2 className="text-xl font-bold text-slate-800 mb-2">Event Not Available</h2>
        <p className="text-sm text-slate-500 mb-6">{error || "Unable to display this event."}</p>
        <Link
          to="/events"
          className="px-5 py-2.5 bg-orange-500 text-white font-semibold text-sm rounded-xl hover:bg-orange-600 transition"
        >
          Back to Events
        </Link>
      </div>
    );
  }

  const eventDate = new Date(event.eventDate).toLocaleDateString("en-US", {
    weekday: "long",
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

  const deadline = new Date(event.registrationDeadline).toLocaleDateString("en-US", {
    weekday: "short",
    year: "numeric",
    month: "short",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

  const percentFilled = Math.min(
    100,
    Math.round(((event.registeredCount || 0) / (event.maxParticipants || 1)) * 100)
  );

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4 sm:px-6 lg:px-8">
      <Notification
        type={notification.type}
        message={notification.message}
        onClose={() => setNotification({ message: "", type: "success" })}
      />

      <div className="max-w-4xl mx-auto">
        {/* Back navigation */}
        <Link
          to="/events"
          className="inline-flex items-center gap-2 text-xs font-semibold text-slate-500 hover:text-slate-900 mb-6 transition"
        >
          <ArrowLeft className="w-4 h-4" />
          Back to Events
        </Link>

        {/* Hero Header Card */}
        <div className="bg-white rounded-3xl border border-slate-200/80 shadow-xs overflow-hidden mb-8">
          <div className="h-48 bg-gradient-to-r from-orange-500 via-amber-500 to-orange-600 p-8 flex flex-col justify-between text-white relative">
            <span className="self-start px-3.5 py-1 bg-white/20 backdrop-blur-md rounded-full text-xs font-bold tracking-wider uppercase">
              Campus Event
            </span>
            <div>
              <h1 className="text-3xl sm:text-4xl font-black tracking-tight">{event.title}</h1>
              <p className="text-sm text-orange-100 flex items-center gap-2 mt-2">
                <MapPin className="w-4 h-4 text-orange-200" />
                {event.venue}
              </p>
            </div>
          </div>

          {/* Details Content */}
          <div className="p-8">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
              {/* Left description */}
              <div className="md:col-span-2 space-y-6">
                <div>
                  <h3 className="text-sm font-bold uppercase tracking-wider text-slate-400 mb-2">
                    About this event
                  </h3>
                  <p className="text-slate-700 text-sm leading-relaxed whitespace-pre-line">
                    {event.description || "No full description provided for this event."}
                  </p>
                </div>

                <div className="pt-6 border-t border-slate-100 grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100">
                    <span className="text-xs font-semibold text-slate-400 block mb-1">Event Date & Time</span>
                    <p className="text-xs font-bold text-slate-800 flex items-center gap-2">
                      <Calendar className="w-4 h-4 text-orange-500" />
                      {eventDate}
                    </p>
                  </div>

                  <div className="p-4 bg-slate-50 rounded-2xl border border-slate-100">
                    <span className="text-xs font-semibold text-slate-400 block mb-1">Registration Closes</span>
                    <p className="text-xs font-bold text-slate-800 flex items-center gap-2">
                      <Clock className="w-4 h-4 text-orange-500" />
                      {deadline}
                    </p>
                  </div>
                </div>
              </div>

              {/* Right CTA Card */}
              <div className="bg-slate-50 rounded-2xl border border-slate-200/80 p-6 flex flex-col justify-between">
                <div>
                  <span className="text-xs font-bold text-slate-400 uppercase tracking-wider block mb-2">
                    Registration Status
                  </span>

                  {event.registrationClosed ? (
                    <span className="inline-flex px-3 py-1 rounded-full text-xs font-bold bg-slate-200 text-slate-600">
                      Closed
                    </span>
                  ) : event.full ? (
                    <span className="inline-flex px-3 py-1 rounded-full text-xs font-bold bg-rose-100 text-rose-700">
                      Capacity Full
                    </span>
                  ) : (
                    <span className="inline-flex px-3 py-1 rounded-full text-xs font-bold bg-emerald-100 text-emerald-700">
                      Open for Registration
                    </span>
                  )}

                  {/* Capacity Bar */}
                  <div className="mt-5">
                    <div className="flex items-center justify-between text-xs font-semibold text-slate-600 mb-2">
                      <span>Seats Registered</span>
                      <span>
                        {event.registeredCount || 0} / {event.maxParticipants}
                      </span>
                    </div>
                    <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full ${
                          percentFilled >= 100
                            ? "bg-rose-500"
                            : percentFilled >= 75
                            ? "bg-amber-500"
                            : "bg-emerald-500"
                        }`}
                        style={{ width: `${percentFilled}%` }}
                      ></div>
                    </div>
                    <p className="text-[11px] text-slate-400 mt-2">
                      {event.maxParticipants - (event.registeredCount || 0)} spot(s) remaining
                    </p>
                  </div>
                </div>

                {/* Registration Action */}
                <div className="mt-8 pt-6 border-t border-slate-200/60">
                  {successCode ? (
                    <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-2xl text-center">
                      <CheckCircle2 className="w-6 h-6 text-emerald-600 mx-auto mb-2" />
                      <p className="text-xs font-bold text-emerald-900">You are Registered!</p>
                      <p className="text-[11px] text-emerald-700 font-mono mt-1 font-bold">{successCode}</p>
                      <Link
                        to="/registrations/my"
                        className="mt-3 block text-xs font-semibold text-orange-600 hover:text-orange-700"
                      >
                        View My Tickets →
                      </Link>
                    </div>
                  ) : isAdmin ? (
                    <div className="p-3 bg-purple-50 rounded-xl text-center text-xs text-purple-700 font-medium">
                      You are logged in as Administrator. Event management is available in the Admin Console.
                    </div>
                  ) : (
                    <button
                      onClick={handleRegister}
                      disabled={event.full || event.registrationClosed || registering}
                      className="w-full py-3 bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 disabled:opacity-50 text-white font-bold text-sm rounded-xl shadow-md shadow-orange-500/20 transition flex items-center justify-center gap-2 cursor-pointer"
                    >
                      {registering ? (
                        <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                      ) : (
                        <Ticket className="w-4 h-4" />
                      )}
                      {event.full ? "Event Full" : event.registrationClosed ? "Registration Closed" : "Register Now"}
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
