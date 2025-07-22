import { SignUpForm } from "@/components/auth/SignUpForm";



export const SignUpPage = () => {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 px-4">
      <h1 className="text-4xl font-bold text-gray-800 mb-6">Sign Up</h1>
      <SignUpForm />
    </div>
  );
};
