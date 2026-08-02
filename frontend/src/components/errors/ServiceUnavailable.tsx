import type { AxiosError } from "axios"
import { Card, CardContent } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
import { AlertTriangle } from "lucide-react"
import clsx from "clsx"

type ServiceErrorProps = {
  err: AxiosError
  variant?: "default" | "compact"
}

export const ServiceError = ({ err, variant = "default" }: ServiceErrorProps) => {
  const status = err.response?.status
  const statusText = err.response?.statusText || "Unknown Error"
  const message = err.message || "Something went wrong"

  let responseData: string | null = null
  if (err.response?.data) {
    if (typeof err.response.data === "string") {
      responseData = err.response.data
    } else {
      try {
        responseData = JSON.stringify(err.response.data, null, 2)
      } catch {
        responseData = String(err.response.data)
      }
    }
  }

  const isCompact = variant === "compact"

  return (
    <div className="flex flex-col justify-center items-center px-4 text-center">
      <Card
        className={clsx(
          "shadow-lg bg-[#F3EBD9] border border-[#B23A2E]/40 rounded-sm",
          isCompact ? "max-w-md" : "max-w-lg"
        )}
      >
        <CardContent className={clsx(isCompact ? "p-4" : "p-6")}>
          <div className="flex items-center justify-center gap-2 mb-2">
            <AlertTriangle
              className={isCompact ? "w-4 h-4" : "w-6 h-6"}
              style={{ color: "#B23A2E" }}
            />
            <h1
              className={clsx(
                "font-display font-bold text-[#B23A2E] leading-tight",
                isCompact ? "text-base" : "text-2xl"
              )}
            >
              {status ? `${status} ${statusText}` : "Request Failed"}
            </h1>
          </div>
          <Separator className="my-3 bg-[#C9A063]" />
          <p
            className={clsx(
              "text-[#4A4136] mb-2",
              isCompact ? "text-xs leading-tight" : "text-base"
            )}
          >
            {message}
          </p>
          {responseData && (
            <pre
              className={clsx(
                "bg-[#E8DFC8] border border-[#C9A063] text-[#241F1A] rounded-sm overflow-x-auto text-left",
                isCompact ? "text-xs p-2 leading-tight" : "text-sm p-2"
              )}
            >
              {responseData}
            </pre>
          )}
        </CardContent>
      </Card>
    </div>
  )
}