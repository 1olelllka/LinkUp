import { LoginForm } from "@/components/auth/LoginForm";

export const LoginPage = () => {

    return (
        <>
            <div>
                <div className="min-h-screen flex flex-col items-center justify-center space-y-6">
                    <h1 className="text-3xl font-semibold text-gray-800">Log in to your account</h1>
                    <LoginForm />
                </div>
            </div>
        </>
    )
}