import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Link } from "react-router";

export const ForbiddenPage = () => {
  return (
    <div className="flex flex-col justify-center items-center min-h-screen px-4">
      <h1 className="text-8xl font-extrabold tracking-tight">403</h1>
      <Separator className="my-4 max-w-100" />
      <h2 className="text-xl font-semibold text-center">
        You don't have permission to access this page
      </h2>
      <p className="mt-2 text-center text-gray-500">
        Please log in or create an account to continue.
      </p>

      <div className="flex gap-4 mt-6">
        <Link to="/login">
          <Button variant="default" className="cursor-pointer">Log In</Button>
        </Link>
        <Link to="/signup">
          <Button variant="outline" className="cursor-pointer">Sign Up</Button>
        </Link>
        <Link to="/">
          <Button variant="ghost" className="cursor-pointer">Back Home</Button>
        </Link>
      </div>
    </div>
  );
};
