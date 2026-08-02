import { Pin } from "lucide-react";

export const TooManyRequestsPage = () => {
  return (
    <div className="min-h-screen bg-[#1E1A16] text-[#F3EBD9] flex items-center justify-center px-4">
      <div className="relative w-full max-w-lg">
        <Pin
          className="absolute -top-4 left-1/2 -translate-x-1/2 w-7 h-7 z-20 rotate-[8deg] drop-shadow-lg"
          style={{ color: "#D9A441" }}
          fill="#D9A441"
        />

        <div className="bg-[#E8DFC8] text-[#241F1A] border border-[#C9A063] rounded-sm shadow-2xl p-8 sm:p-12 text-center rotate-[1deg]">
          <span className="font-hand text-2xl text-[#D9A441]">
            slow down a little
          </span>

          <h1 className="font-display text-8xl sm:text-9xl font-bold tracking-tight mt-1">
            429
          </h1>

          <div className="h-px bg-[#C9A063] max-w-xs mx-auto my-5" />

          <h2 className="font-display text-xl sm:text-2xl font-bold">
            Too Many Requests
          </h2>

          <p className="text-[#4A4136] mt-3 leading-relaxed max-w-md mx-auto">
            You've sent too many requests in a short amount of time.
            The issue should resolve shortly — give the server a
            moment to catch its breath.
          </p>
        </div>
      </div>
    </div>
  );
};