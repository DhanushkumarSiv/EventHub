import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
  Calendar,
  Users,
  CheckCircle2,
  TrendingUp,
  PlusCircle,
  ArrowUpRight,
  Clock,
  MapPin,
  RefreshCw,
  Award,
  Layers,
  ChevronRight,
} from "lucide-react";
import { dashboardService } from "../../services/dashboardService";
import { LoadingSpinner } from "../../components/common/LoadingSpinner";
import { Notification } from "../../components/common/Notification";

export const Dashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [notification, setNotification] = useState(null);

  const fetchStats = async (isManualRefresh = false) => {
    try {
      if (isManualRefresh) setRefreshing(true);
      else setLoading(true);

      const data = await dashboardService.getStats();
      setStats(data);
    } catch (err) {
      setNotification({
        type: "error",
        message: err.response?.data?.message || "Failed to load dashboard metrics.",
      });
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

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

  if (loading) {
    return (
      <div className="py-20">
        <LoadingSpinner text="Computing real-time analytics from PostgreSQL..." />
      </div>
    );
  }

  const totalRegs = stats?.totalRegistrations || 0;
  const totalAtt = stats?.totalAttendance || 0;
  const attendanceRate = totalRegs > 0 ? Math.round((totalAtt / totalRegs) * 100) : 0;

  return (
    <div className="space-y-8">
      {notification && (
        <Notification
          type={notification.type}
          message={notification.message}
          onClose={() => setNotification(null)}
        />
      )}

      {/* Header Area */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-orange-100 text-orange-700 text-xs font-semibold mb-1">
            <TrendingUp className="w-3.5 h-3.5" />
            <span>Real-time Operational Metrics</span>
          </div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
            Admin Overview & Analytics
          </h1>
          <p className="text-sm text-slate-500 mt-0.5">
            Monitor campus event engagement, registration pacing, and check-in attendance.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={() => fetchStats(true)}
            disabled={refreshing}
            className="inline-flex items-center gap-2 px-3.5 py-2 text-xs font-semibold text-slate-700 bg-white border border-slate-200 hover:bg-slate-50 rounded-xl transition shadow-xs cursor-pointer disabled:opacity-50"
            title="Refresh metrics"
          >
            <RefreshCw className={`w-3.5 h-3.5 ${refreshing ? "animate-spin text-orange-500" : ""}`} />
            <span>Refresh</span>
          </button>

          <Link
            to="/admin/events/new"
            className="inline-flex items-center gap-2 px-4 py-2 bg-gradient-to-r from-orange-500 to-amber-500 hover:from-orange-600 hover:to-amber-600 text-white font-medium text-xs rounded-xl shadow-xs shadow-orange-500/20 transition cursor-pointer"
          >
            <PlusCircle className="w-4 h-4" />
            <span>Create New Event</span>
          </Link>
        </div>
      </div>

      {/* 4 Main KPI Cards (Inspired by Images 2, 5, 6) */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Total Events */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs hover:shadow-md transition">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-bold uppercase tracking-wider">Total Events</span>
            <div className="w-8 h-8 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center">
              <Calendar className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-3xl font-extrabold text-slate-900 tracking-tight">
              {stats?.totalEvents ?? 0}
            </span>
          </div>
          <div className="mt-2.5 flex items-center gap-2 text-xs text-slate-500">
            <span className="inline-flex items-center gap-1 font-semibold text-emerald-600">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500"></span>
              {stats?.publishedEvents ?? 0} Published
            </span>
            <span>•</span>
            <span className="text-slate-400">{stats?.draftEvents ?? 0} Drafts</span>
          </div>
        </div>

        {/* Published Events */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs hover:shadow-md transition">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-bold uppercase tracking-wider">Active Published</span>
            <div className="w-8 h-8 rounded-xl bg-orange-50 text-orange-600 flex items-center justify-center">
              <Layers className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-3xl font-extrabold text-slate-900 tracking-tight">
              {stats?.publishedEvents ?? 0}
            </span>
          </div>
          <p className="mt-2.5 text-xs text-slate-500">
            Live on the public student portal
          </p>
        </div>

        {/* Total Registrations */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs hover:shadow-md transition">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-bold uppercase tracking-wider">Total Registrations</span>
            <div className="w-8 h-8 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center">
              <Users className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3">
            <span className="text-3xl font-extrabold text-slate-900 tracking-tight">
              {stats?.totalRegistrations ?? 0}
            </span>
          </div>
          <p className="mt-2.5 text-xs text-slate-500">
            Unique passes issued across events
          </p>
        </div>

        {/* Check-ins / Attendance */}
        <div className="bg-white rounded-2xl p-5 border border-slate-200/80 shadow-xs hover:shadow-md transition">
          <div className="flex items-center justify-between text-slate-500">
            <span className="text-xs font-bold uppercase tracking-wider">Total Check-ins</span>
            <div className="w-8 h-8 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center">
              <CheckCircle2 className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-extrabold text-slate-900 tracking-tight">
              {stats?.totalAttendance ?? 0}
            </span>
            <span className="text-xs font-bold text-emerald-600 bg-emerald-50 px-2 py-0.5 rounded-full">
              {attendanceRate}% rate
            </span>
          </div>
          <p className="mt-2.5 text-xs text-slate-500">
            Verified participants present
          </p>
        </div>
      </div>

      {/* Middle Row: Visual Engagement Meters (Inspired by Image 2, 5, 6) */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Attendance Donut / Gauge Card */}
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <h2 className="text-sm font-bold text-slate-900 uppercase tracking-wider">
                Attendance Conversion
              </h2>
              <Award className="w-4 h-4 text-orange-500" />
            </div>
            <p className="text-xs text-slate-500 mt-1">
              Checked-in vs total registered participants
            </p>

            {/* Visual Circular Gauge */}
            <div className="mt-6 flex flex-col items-center justify-center">
              <div className="relative w-36 h-36 flex items-center justify-center">
                <svg className="w-full h-full transform -rotate-90" viewBox="0 0 100 100">
                  <circle
                    cx="50"
                    cy="50"
                    r="40"
                    stroke="#F1F5F9"
                    strokeWidth="12"
                    fill="transparent"
                  />
                  <circle
                    cx="50"
                    cy="50"
                    r="40"
                    stroke="#F97316"
                    strokeWidth="12"
                    strokeDasharray={2 * Math.PI * 40}
                    strokeDashoffset={
                      2 * Math.PI * 40 * (1 - (attendanceRate > 100 ? 1 : attendanceRate / 100))
                    }
                    strokeLinecap="round"
                    fill="transparent"
                    className="transition-all duration-1000 ease-out"
                  />
                </svg>
                <div className="absolute flex flex-col items-center">
                  <span className="text-2xl font-black text-slate-900 tracking-tight">
                    {attendanceRate}%
                  </span>
                  <span className="text-[10px] uppercase font-bold text-slate-400">
                    Checked In
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-slate-100 grid grid-cols-2 gap-2 text-center text-xs">
            <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                Present
              </span>
              <span className="font-bold text-slate-800 text-sm">{totalAtt}</span>
            </div>
            <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">
              <span className="text-slate-400 block text-[10px] uppercase font-semibold">
                Pending Check-in
              </span>
              <span className="font-bold text-slate-800 text-sm">
                {Math.max(0, totalRegs - totalAtt)}
              </span>
            </div>
          </div>
        </div>

        {/* Capacity Utilization by Event (Inspired by Image 2 & 6) */}
        <div className="bg-white rounded-2xl p-6 border border-slate-200/80 shadow-xs lg:col-span-2 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-sm font-bold text-slate-900 uppercase tracking-wider">
                  Registration Capacity by Event
                </h2>
                <p className="text-xs text-slate-500 mt-0.5">
                  Real-time seat reservation pacing across all events
                </p>
              </div>
              <Link
                to="/admin/events"
                className="text-xs font-semibold text-orange-600 hover:text-orange-700 transition flex items-center gap-1"
              >
                <span>View All</span>
                <ChevronRight className="w-3.5 h-3.5" />
              </Link>
            </div>

            <div className="mt-5 space-y-4">
              {stats?.registrationsByEvent && stats.registrationsByEvent.length > 0 ? (
                stats.registrationsByEvent.slice(0, 4).map((stat) => {
                  const percent = Math.min(
                    100,
                    Math.round((stat.registeredCount / Math.max(1, stat.maxParticipants)) * 100)
                  );
                  return (
                    <div key={stat.eventId} className="space-y-1.5">
                      <div className="flex items-center justify-between text-xs">
                        <span className="font-semibold text-slate-800 truncate max-w-xs sm:max-w-md">
                          {stat.eventTitle}
                        </span>
                        <div className="flex items-center gap-2 shrink-0">
                          <span className="text-slate-500">
                            <strong className="text-slate-900">{stat.registeredCount}</strong> /{" "}
                            {stat.maxParticipants} seats
                          </span>
                          <span className="font-bold text-slate-700 w-10 text-right">
                            {percent}%
                          </span>
                        </div>
                      </div>
                      <div className="w-full bg-slate-100 rounded-full h-2.5 overflow-hidden">
                        <div
                          className={`h-full rounded-full transition-all duration-500 ${
                            percent >= 100
                              ? "bg-rose-500"
                              : percent >= 80
                              ? "bg-amber-500"
                              : "bg-orange-500"
                          }`}
                          style={{ width: `${percent}%` }}
                        ></div>
                      </div>
                    </div>
                  );
                })
              ) : (
                <div className="text-center py-8 text-xs text-slate-400">
                  No active registration records found.
                </div>
              )}
            </div>
          </div>

          <div className="mt-5 pt-4 border-t border-slate-100 flex items-center justify-between text-xs text-slate-500">
            <span>Overall Campus Capacity tracked</span>
            <Link
              to="/admin/attendance"
              className="text-xs font-semibold text-orange-600 hover:text-orange-700 flex items-center gap-1"
            >
              Open Attendance Marker →
            </Link>
          </div>
        </div>
      </div>

      {/* Bottom Section: Upcoming Events Overview (Inspired by Image 3 & 4) */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex items-center justify-between">
          <div>
            <h2 className="text-base font-bold text-slate-900">Upcoming Campus Events</h2>
            <p className="text-xs text-slate-500 mt-0.5">
              Scheduled events awaiting check-in and participant arrival
            </p>
          </div>
          <Link
            to="/admin/events"
            className="text-xs font-semibold text-orange-600 hover:text-orange-700 transition"
          >
            Manage All Events →
          </Link>
        </div>

        {stats?.upcomingEvents && stats.upcomingEvents.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500 border-b border-slate-200/60">
                  <th className="py-3 px-6">Event Title</th>
                  <th className="py-3 px-6">Event Date & Venue</th>
                  <th className="py-3 px-6">Capacity</th>
                  <th className="py-3 px-6">Status</th>
                  <th className="py-3 px-6 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {stats.upcomingEvents.map((event) => (
                  <tr key={event.id} className="hover:bg-slate-50/60 transition">
                    <td className="py-4 px-6">
                      <p className="font-bold text-slate-900 line-clamp-1">{event.title}</p>
                      <p className="text-slate-400 text-[11px] line-clamp-1">{event.description}</p>
                    </td>
                    <td className="py-4 px-6">
                      <div className="flex items-center gap-1.5 text-slate-700 font-medium">
                        <Clock className="w-3.5 h-3.5 text-orange-500 shrink-0" />
                        <span>{formatDate(event.eventDate)}</span>
                      </div>
                      <div className="flex items-center gap-1.5 text-slate-400 text-[11px] mt-0.5">
                        <MapPin className="w-3.5 h-3.5 shrink-0" />
                        <span>{event.venue}</span>
                      </div>
                    </td>
                    <td className="py-4 px-6 font-semibold text-slate-700">
                      <span>{event.registeredCount ?? 0}</span> / {event.maxParticipants}
                    </td>
                    <td className="py-4 px-6">
                      <span
                        className={`inline-flex px-2.5 py-0.5 rounded-full text-[11px] font-bold uppercase ${
                          event.status === "PUBLISHED"
                            ? "bg-emerald-50 text-emerald-700 border border-emerald-200"
                            : event.status === "DRAFT"
                            ? "bg-slate-100 text-slate-600 border border-slate-200"
                            : "bg-rose-50 text-rose-700 border border-rose-200"
                        }`}
                      >
                        {event.status}
                      </span>
                    </td>
                    <td className="py-4 px-6 text-right space-x-2">
                      <Link
                        to={`/admin/events/edit/${event.id}`}
                        className="text-xs font-semibold text-slate-600 hover:text-slate-900 px-2 py-1 rounded-md hover:bg-slate-100 transition"
                      >
                        Edit
                      </Link>
                      <Link
                        to="/admin/attendance"
                        className="text-xs font-semibold text-orange-600 hover:text-orange-700 px-2 py-1 rounded-md hover:bg-orange-50 transition"
                      >
                        Attendance
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="p-8 text-center text-xs text-slate-400">
            No upcoming events scheduled right now.
          </div>
        )}
      </div>
    </div>
  );
};