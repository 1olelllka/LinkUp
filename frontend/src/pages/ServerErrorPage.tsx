import { Separator } from "@/components/ui/separator"
import { Button } from "@/components/ui/button"
import { useHealthStore } from "@/store/useHealthStore"
import { checkGatewayHealthStatus } from "@/services/gateway"
import { toast } from "sonner"
import { checkAuthHealth } from "@/services/authServices"
import { checkProfileHealth } from "@/services/profileServices"

export const ServerErrorPage = () => {
  const { setDown } = useHealthStore()

  const handleRetry = async () => {
    try {
        const gateway = await checkGatewayHealthStatus();
        const auth = await checkAuthHealth();
        const profile = await checkProfileHealth();
        if (gateway.data.status != 'UP' || auth.data.status != "UP" || profile.data.status != 'UP') {
          setDown(true);
        } else if (gateway.data.status == 'UP' && auth.data.status == 'UP' && profile.data.status == 'UP') {
          setDown(false);
          toast.success("The issues were resolved!")
      }
    } catch {
      toast.error("The error persists")
    }
  }

  return (
    <div className="flex flex-col justify-center items-center min-h-screen px-4 text-center">
      <h1 className="text-8xl font-extrabold tracking-tight">500</h1>
      <Separator className="my-4 w-32" />
      <h2 className="text-2xl font-semibold">Internal Server Error</h2>
      <p className="text-slate-500 mt-2 max-w-md">
        This happened due to an error on one of the critical services. 
        We&apos;re working on the quickest solution for this issue.
      </p>

      <Button className="mt-6" onClick={handleRetry}>
        Retry
      </Button>
    </div>
  )
}
