import { Separator } from "@/components/ui/separator"

export const TooManyRequestsPage = () => {
  return (
    <div className="flex flex-col justify-center items-center min-h-screen px-4 text-center">
      <h1 className="text-8xl font-extrabold tracking-tight">429</h1>
      <Separator className="my-4 w-32" />
      <h2 className="text-2xl font-semibold">Too Many Requests</h2>
      <p className="text-slate-500 mt-2 max-w-md">
        This happened due to excessive server calls from your client side. The error
        will be resolved shortly, please wait.
      </p>
    </div>
  )
}
