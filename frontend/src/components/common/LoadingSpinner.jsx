import React from "react";

export const LoadingSpinner = ({ message = "Loading..." }) => {
  return (
    <div className="flex flex-col items-center justify-center py-16 gap-3">
      <div className="w-9 h-9 border-3 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
      <p className="text-sm font-medium text-slate-500">{message}</p>
    </div>
  );
};
