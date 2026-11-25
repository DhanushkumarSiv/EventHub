import React from "react";
import { CalendarX } from "lucide-react";

export const EmptyState = ({
  icon: Icon = CalendarX,
  title = "No items found",
  description = "There are no records to display at this time.",
  actionText,
  onAction,
}) => {
  return (
    <div className="flex flex-col items-center justify-center p-12 text-center bg-white rounded-2xl border border-slate-200/80 shadow-xs max-w-md mx-auto my-6">
      <div className="w-14 h-14 rounded-2xl bg-orange-50 text-orange-500 flex items-center justify-center mb-4">
        <Icon className="w-7 h-7" />
      </div>
      <h3 className="text-lg font-semibold text-slate-800 mb-1">{title}</h3>
      <p className="text-sm text-slate-500 mb-6">{description}</p>
      {actionText && onAction && (
        <button
          onClick={onAction}
          className="px-5 py-2.5 bg-orange-500 hover:bg-orange-600 text-white font-medium text-sm rounded-xl transition shadow-xs cursor-pointer"
        >
          {actionText}
        </button>
      )}
    </div>
  );
};
