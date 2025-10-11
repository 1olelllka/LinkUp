import type { AxiosError } from "axios"
import { Card, CardContent } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"
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
          "w-full shadow-lg",
          isCompact ? "max-w-md" : "max-w-lg"
        )}
      >
        <CardContent className={clsx(isCompact ? "p-4" : "p-6")}>
          <h1
            className={clsx(
              "font-bold text-red-600 leading-tight mb-2",
              isCompact ? "text-base" : "text-3xl"
            )}
          >
            {status ? `${status} ${statusText}` : "Request Failed"}
          </h1>
          <Separator className="my-3" />
          <p
            className={clsx(
              "text-slate-600 mb-2",
              isCompact ? "text-xs leading-tight" : "text-base"
            )}
          >
            {message}
          </p>
          {responseData && (
            <pre
              className={clsx(
                "bg-slate-100 rounded-md overflow-x-auto text-left",
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
