import { SignUpForm } from "@/components/auth/SignUpForm";



export const SignUpPage = () => {
  return (
    <div className="min-h-screen bg-[#1E1A16] flex flex-col items-center justify-center px-4 py-12">
        <span className="font-hand text-2xl text-[#D9A441] mb-1">welcome to the board</span>
        <h1 className="font-display text-3xl md:text-4xl font-bold text-[#F3EBD9] mb-8 text-center">Sign Up</h1>
      <SignUpForm />
    </div>
  );
};
