import React, { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import {
  Calendar,
  MapPin,
  Users,
  Search,
  Sparkles,
  Clock,
  ArrowRight,
  CheckCircle2,
  AlertTriangle,
} from "lucide-react";
import { eventService } from "../../services/eventService";
import { LoadingSpinner } from "../../components/common/LoadingSpinner";
import { EmptyState } from "../../components/common/EmptyState";

export const Events = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedCategory, setSelectedCategory] = useState("All");

  const categories = ["All", "Tech", "Workshops", "Seminars", "Cultural"];

  const gradientThemes = [
    "from-blue-600 to-indigo-600",
    "from-orange-500 to-amber-500",
    "from-pink-500 to-rose-600",
    "from-emerald-500 to-teal-600",
    "from-indigo-600 to-purple-600",
  ];

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    setLoading(true);
    try {
      const data = await eventService.getPublishedEvents();
      setEvents(data || []);
    } catch (err) {
      setError("Unable to load events. Please check the backend connection.");
    } finally {
      setLoading(false);
    }
  };

  const filteredEvents = events.filter((event) => {
    const matchesSearch =
      event.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      event.venue.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (event.description && event.description.toLowerCase().includes(searchQuery.toLowerCase()));

    if (selectedCategory === "All") return matchesSearch;
    return (
      matchesSearch &&
      (event.title.toLowerCase().includes(selectedCategory.toLowerCase()) ||
        (event.description && event.description.toLowerCase().includes(selectedCategory.toLowerCase())))
    );
  });

  const formatDate = (dateString) => {
    if (!dateString) return { day: "TBD", month: "TBD", full: "Date TBD" };
    const date = new Date(dateString);
    return {
      day: date.getDate(),
      month: date.toLocaleString("en-US", { month: "short" }).toUpperCase(),
      full: date.toLocaleDateString("en-US", {
        weekday: "short",
        month: "short",
        day: "numeric",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      }),
    };
  };

  return (
    <div className="min-h-screen bg-slate-50 pb-16">
      {/* Hero Section - Inspired by Go Campus & Mohandas (Image 1 & 4) */}
      <section className="bg-gradient-to-b from-white via-orange-50/20 to-slate-50 border-b border-slate-200/80 pt-10 pb-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col lg:flex-row items-center justify-between gap-8">
            <div className="max-w-2xl text-left">
              <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-orange-100 text-orange-700 text-xs font-semibold mb-4">
                <Sparkles className="w-3.5 h-3.5 text-orange-600" />
                <span>Your campus, all in one place</span>
              </div>
              <h1 className="text-4xl sm:text-5xl font-black text-slate-900 tracking-tight leading-tight">
                Discover & register for <span className="text-orange-500">campus events.</span>
              </h1>
              <p className="mt-4 text-slate-600 text-base leading-relaxed">
                From hackathons to technical workshops — find what's happening on campus, grab your ticket, and register in seconds.
              </p>

              {/* Search Bar - Pill Shape */}
              <div className="mt-6 flex items-center bg-white rounded-full p-1.5 shadow-lg shadow-slate-200/80 border border-slate-200 max-w-lg">
                <Search className="w-5 h-5 text-slate-400 ml-3.5 shrink-0" />
                <input
                  type="text"
                  placeholder="Search events, workshops, fests..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full px-3 py-2 text-sm text-slate-800 placeholder-slate-400 outline-hidden bg-transparent"
                />
                <button
                  type="button"
                  className="px-6 py-2.5 bg-orange-500 hover:bg-orange-600 text-white font-semibold text-sm rounded-full transition shadow-xs cursor-pointer shrink-0"
                >
                  Search
                </button>
              </div>

              {/* Stats Counters */}
              <div className="mt-8 flex items-center gap-8 text-left">
                <div>
                  <span className="text-2xl font-black text-slate-900">{events.length}+</span>
                  <p className="text-xs font-medium text-slate-500">Events published</p>
                </div>
                <div className="w-px h-8 bg-slate-200"></div>
                <div>
                  <span className="text-2xl font-black text-slate-900">
                    {events.reduce((acc, curr) => acc + (curr.registeredCount || 0), 0)}
                  </span>
                  <p className="text-xs font-medium text-slate-500">Student registrations</p>
                </div>
                <div className="w-px h-8 bg-slate-200"></div>
                <div>
                  <span className="text-2xl font-black text-slate-900">100%</span>
                  <p className="text-xs font-medium text-slate-500">Free campus access</p>
                </div>
              </div>
            </div>

            {/* Featured Event Card on Right - Inspired by Image 1 */}
            {events.length > 0 && (
              <div className="w-full lg:w-96 bg-gradient-to-br from-orange-500 via-amber-500 to-orange-600 rounded-3xl p-6 text-white shadow-xl shadow-orange-500/25 relative overflow-hidden">
                <div className="absolute -right-10 -bottom-10 w-40 h-40 bg-white/10 rounded-full blur-2xl"></div>
                <span className="px-3 py-1 bg-white/20 backdrop-blur-md rounded-full text-[11px] font-bold tracking-wider uppercase">
                  Featured Event
                </span>
                <h3 className="text-2xl font-black mt-4 mb-2">{events[0].title}</h3>
                <p className="text-xs text-orange-100 line-clamp-2 mb-4">
                  {events[0].description || "Join this exciting campus event!"}
                </p>
                <div className="space-y-1.5 text-xs text-white/90 mb-6">
                  <div className="flex items-center gap-2">
                    <Calendar className="w-3.5 h-3.5" />
                    <span>{formatDate(events[0].eventDate).full}</span>
                  </div>
                  <div className="flex items-center gap-2">
                    <MapPin className="w-3.5 h-3.5" />
                    <span>{events[0].venue}</span>
                  </div>
                </div>
                <Link
                  to={`/events/${events[0].id}`}
                  className="block w-full py-2.5 bg-white text-orange-600 hover:bg-orange-50 font-bold text-center text-sm rounded-xl transition shadow-xs"
                >
                  Register Now
                </Link>
              </div>
            )}
          </div>
        </div>
      </section>

      {/* Main Content & Event Grid */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-10">
        {/* Category Filter Pills - Inspired by Image 1 */}
        <div className="flex items-center justify-between mb-8 flex-wrap gap-4">
          <h2 className="text-2xl font-bold text-slate-900 tracking-tight">Upcoming events</h2>
          <div className="flex items-center gap-2 flex-wrap">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-4 py-1.5 rounded-full text-xs font-semibold transition cursor-pointer ${
                  selectedCategory === cat
                    ? "bg-slate-900 text-white"
                    : "bg-white text-slate-600 hover:bg-slate-100 border border-slate-200"
                }`}
              >
                {cat}
              </button>
            ))}
          </div>
        </div>

        {/* Loading / Error States */}
        {loading ? (
          <LoadingSpinner message="Fetching campus events..." />
        ) : error ? (
          <div className="p-6 bg-rose-50 border border-rose-200 rounded-2xl text-rose-700 text-sm font-medium flex items-center gap-3">
            <AlertTriangle className="w-5 h-5 shrink-0" />
            <span>{error}</span>
          </div>
        ) : filteredEvents.length === 0 ? (
          <EmptyState
            title="No events match your criteria"
            description="Try adjusting your search query or category filter."
            actionText="Clear filter"
            onAction={() => {
              setSearchQuery("");
              setSelectedCategory("All");
            }}
          />
        ) : (
          /* Event Grid - Inspired by Image 1 & Image 4 */
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredEvents.map((event, idx) => {
              const dateInfo = formatDate(event.eventDate);
              const gradient = gradientThemes[idx % gradientThemes.length];
              const percentFilled = Math.min(
                100,
                Math.round(((event.registeredCount || 0) / (event.maxParticipants || 1)) * 100)
              );

              return (
                <div
                  key={event.id}
                  className="bg-white rounded-3xl border border-slate-200/80 shadow-xs hover:shadow-xl hover:-translate-y-1 transition duration-200 overflow-hidden flex flex-col justify-between group"
                >
                  <div>
                    {/* Top Gradient Banner with Date Badge */}
                    <div className={`h-36 bg-gradient-to-r ${gradient} p-5 relative flex flex-col justify-between`}>
                      <span className="self-start px-3 py-1 bg-white/20 backdrop-blur-md rounded-full text-[11px] font-bold text-white tracking-wider uppercase">
                        Campus Event
                      </span>

                      {/* Floating Date Badge - Inspired by Image 1 */}
                      <div className="absolute top-4 right-4 bg-white/95 backdrop-blur-md rounded-2xl p-2.5 shadow-md flex flex-col items-center justify-center min-w-[52px]">
                        <span className="text-lg font-black text-slate-900 leading-none">{dateInfo.day}</span>
                        <span className="text-[10px] font-bold tracking-wider text-orange-600 uppercase mt-0.5">
                          {dateInfo.month}
                        </span>
                      </div>
                    </div>

                    {/* Card Body */}
                    <div className="p-5">
                      <h3 className="text-lg font-bold text-slate-900 group-hover:text-orange-600 transition line-clamp-1 mb-2">
                        {event.title}
                      </h3>
                      <p className="text-xs text-slate-500 line-clamp-2 leading-relaxed mb-4">
                        {event.description || "No description provided."}
                      </p>

                      {/* Metadata */}
                      <div className="space-y-2 text-xs text-slate-600 pt-2 border-t border-slate-100 mb-4">
                        <div className="flex items-center gap-2">
                          <MapPin className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                          <span className="truncate">{event.venue}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <Clock className="w-3.5 h-3.5 text-slate-400 shrink-0" />
                          <span>Deadline: {formatDate(event.registrationDeadline).full}</span>
                        </div>
                      </div>

                      {/* Capacity Progress Bar */}
                      <div>
                        <div className="flex items-center justify-between text-[11px] font-medium text-slate-500 mb-1.5">
                          <span className="flex items-center gap-1.5">
                            <Users className="w-3.5 h-3.5 text-slate-400" />
                            Capacity: {event.registeredCount || 0} / {event.maxParticipants}
                          </span>
                          <span className={percentFilled >= 100 ? "text-rose-600 font-bold" : "text-slate-600"}>
                            {percentFilled}% full
                          </span>
                        </div>
                        <div className="w-full h-1.5 bg-slate-100 rounded-full overflow-hidden">
                          <div
                            className={`h-full rounded-full transition-all ${
                              percentFilled >= 100
                                ? "bg-rose-500"
                                : percentFilled >= 75
                                ? "bg-amber-500"
                                : "bg-emerald-500"
                            }`}
                            style={{ width: `${percentFilled}%` }}
                          ></div>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* Card Footer Action */}
                  <div className="p-5 pt-0">
                    <Link
                      to={`/events/${event.id}`}
                      className="w-full py-2.5 px-4 bg-slate-50 hover:bg-orange-50 hover:text-orange-600 text-slate-700 font-semibold text-xs rounded-xl border border-slate-200 transition flex items-center justify-center gap-2 group-hover:border-orange-200"
                    >
                      <span>View & Register</span>
                      <ArrowRight className="w-3.5 h-3.5 text-slate-400 group-hover:text-orange-500 transition" />
                    </Link>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
