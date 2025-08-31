import type { AxiosError } from "axios"
import { Card, CardContent } from "@/components/ui/card"
import { Separator } from "@/components/ui/separator"

type ServiceErrorProps = {
  err: AxiosError
}

export const ServiceError = ({ err }: ServiceErrorProps) => {
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

  return (
    <div className="flex flex-col justify-center items-center px-4 text-center">
      <Card className="max-w-lg w-full shadow-lg">
        <CardContent className="p-6">
          <h1 className="text-3xl font-bold text-red-600 mb-2">
            {status ? `${status} ${statusText}` : "Request Failed"}
          </h1>
          <Separator className="my-3" />
          <p className="text-slate-600 mb-2">{message}</p>
          {responseData && (
            <pre className="bg-slate-100 text-sm p-2 rounded-md overflow-x-auto text-left">
              {responseData}
            </pre>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
