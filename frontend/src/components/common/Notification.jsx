import React, { useEffect } from "react";
import { CheckCircle2, AlertCircle, X } from "lucide-react";

export const Notification = ({ type = "success", message, onClose, duration = 4000 }) => {
  useEffect(() => {
    if (!message) return;
    const timer = setTimeout(() => {
      onClose();
    }, duration);
    return () => clearTimeout(timer);
  }, [message, duration, onClose]);

  if (!message) return null;

  const isSuccess = type === "success";

  return (
    <div className="fixed bottom-6 right-6 z-50 flex items-center gap-3 px-4 py-3 bg-slate-900 text-white rounded-xl shadow-xl border border-slate-800 animate-slide-up">
      {isSuccess ? (
        <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
      ) : (
        <AlertCircle className="w-5 h-5 text-rose-400 shrink-0" />
      )}
      <p className="text-sm font-medium pr-2">{message}</p>
      <button
        onClick={onClose}
        className="text-slate-400 hover:text-white transition p-1 rounded-md"
      >
        <X className="w-4 h-4" />
      </button>
    </div>
  );
};
