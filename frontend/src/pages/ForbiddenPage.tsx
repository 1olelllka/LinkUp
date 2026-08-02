import { Button } from "@/components/ui/button";
import { Link } from "react-router";
import { Pin } from "lucide-react";

export const ForbiddenPage = () => {
  return (
    <div className="min-h-screen bg-[#1E1A16] text-[#F3EBD9] flex items-center justify-center px-4">
      <div className="relative w-full max-w-lg">
        <Pin
          className="absolute -top-4 left-1/2 -translate-x-1/2 w-7 h-7 z-20 rotate-[-6deg] drop-shadow-lg"
          style={{ color: "#D9A441" }}
          fill="#D9A441"
        />

        <div className="bg-[#E8DFC8] text-[#241F1A] border border-[#C9A063] rounded-sm shadow-2xl p-8 sm:p-12 text-center rotate-[-1deg]">
          <span className="font-hand text-2xl text-[#B23A2E]">
            that's not your corner
          </span>

          <h1 className="font-display text-8xl sm:text-9xl font-bold tracking-tight mt-1">
            403
          </h1>

          <div className="h-px bg-[#C9A063] max-w-xs mx-auto my-5" />

          <h2 className="font-display text-xl sm:text-2xl font-bold">
            Access denied
          </h2>

          <p className="text-[#4A4136] mt-3 leading-relaxed">
            You don't have permission to access this page.
            Log in or create an account to continue.
          </p>

          <div className="flex flex-wrap justify-center gap-3 mt-7">
            <Link to="/login">
              <Button
                className="
                  rounded-sm
                  bg-[#B23A2E]
                  hover:bg-[#9C3226]
                  text-[#F3EBD9]
                  cursor-pointer
                "
              >
                Log In
              </Button>
            </Link>

            <Link to="/signup">
              <Button
                variant="outline"
                className="
                  rounded-sm
                  border-[#6B4A32]
                  text-[#241F1A]
                  hover:bg-[#DDD0B0]
                  cursor-pointer
                "
              >
                Sign Up
              </Button>
            </Link>

            <Link to="/">
              <Button
                variant="ghost"
                className="
                  rounded-sm
                  text-[#4A4136]
                  hover:bg-[#DDD0B0]
                  cursor-pointer
                "
              >
                Back Home
              </Button>
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};