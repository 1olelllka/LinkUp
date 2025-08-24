import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Link } from "react-router";

export const NotFoundPage = () => {
  return (
    <div className="flex flex-col justify-center items-center min-h-screen px-4">
      <h1 className="text-8xl font-extrabold tracking-tight">404</h1>
      <Separator className="my-4 max-w-100" />
      <h2 className="text-xl font-semibold text-center">
        Opps... The page was not found
      </h2>

      <div className="flex gap-4 mt-6">
        <Link to="/">
          <Button variant="ghost" className="cursor-pointer">Back Home</Button>
        </Link>
      </div>
    </div>
  );
};
