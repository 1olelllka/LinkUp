import { Button } from "@/components/ui/button";
import { useHealthStore } from "@/store/useHealthStore";
import { checkGatewayHealthStatus } from "@/services/gateway";
import { toast } from "sonner";
import { checkAuthHealth } from "@/services/authServices";
import { checkProfileHealth } from "@/services/profileServices";
import { Pin } from "lucide-react";

export const ServerErrorPage = () => {
  const { setDown } = useHealthStore();

  const handleRetry = async () => {
    try {
      const gateway = await checkGatewayHealthStatus();
      const auth = await checkAuthHealth();
      const profile = await checkProfileHealth();

      if (
        gateway.data.status !== "UP" ||
        auth.data.status !== "UP" ||
        profile.data.status !== "UP"
      ) {
        setDown(true);
      } else {
        setDown(false);
        toast.success("The issues were resolved!");
      }
    } catch {
      toast.error("The error persists.");
    }
  };

  return (
    <div className="min-h-screen bg-[#1E1A16] text-[#F3EBD9] flex items-center justify-center px-4">
      <div className="relative w-full max-w-lg">
        <Pin
          className="absolute -top-4 left-1/2 -translate-x-1/2 w-7 h-7 z-20 rotate-[5deg] drop-shadow-lg"
          style={{ color: "#D9A441" }}
          fill="#D9A441"
        />

        <div className="bg-[#E8DFC8] text-[#241F1A] border border-[#C9A063] rounded-sm shadow-2xl p-8 sm:p-12 text-center rotate-[1deg]">
          <span className="font-hand text-2xl text-[#B23A2E]">
            something went wrong
          </span>

          <h1 className="font-display text-8xl sm:text-9xl font-bold tracking-tight mt-1">
            500
          </h1>

          <div className="h-px bg-[#C9A063] max-w-xs mx-auto my-5" />

          <h2 className="font-display text-xl sm:text-2xl font-bold">
            Internal Server Error
          </h2>

          <p className="text-[#4A4136] mt-3 leading-relaxed max-w-md mx-auto">
            One of the critical services isn't feeling well.
            We're working on getting everything back up.
          </p>

          <Button
            className="
              mt-7
              rounded-sm
              bg-[#B23A2E]
              hover:bg-[#9C3226]
              text-[#F3EBD9]
              cursor-pointer
            "
            onClick={handleRetry}
          >
            Retry
          </Button>
        </div>
      </div>
    </div>
  );
};