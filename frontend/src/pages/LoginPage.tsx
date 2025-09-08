import { LoginForm } from "@/components/auth/LoginForm";

export const LoginPage = () => {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-[#FFF BDE] px-4">
      <h1 className="text-3xl font-bold text-gray-800 mb-8">
        Log in to your account
      </h1>
      <LoginForm />
    </div>
  );
};
