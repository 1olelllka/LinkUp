import {z} from "zod";
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import { Button } from "@/components/ui/button"
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { NavLink, useNavigate } from "react-router";
import { getMe, login } from "@/services/authServices";
import { API_BASE } from "@/constants/routes";
import { useAuthStore } from "@/store/useAuthStore";
import { useProfileStore } from "@/store/useProfileStore";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { useState } from "react";
import { SubmitLoader } from "../load/SubmitLoader";
import { getSpecificProfileInfo } from "@/services/profileServices";
import { Pin } from "lucide-react";


const formSchema = z.object({
  email: z.email("Please write valid email"),
  password: z.string().min(1, "Password must contain at least 1 character")
})

export const LoginForm = () => {

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            email: "",
            password: ""
        }
    })
    const [loading, setLoading] = useState(false);

    const navigate = useNavigate();

    async function onSubmit(values: z.infer<typeof formSchema>) {
        setLoading(true);
        try {
            const res = await login(values);
            useAuthStore.getState().setToken(res.accessToken);
            const authData = await getMe();
            console.log(authData)
            const profileData = await getSpecificProfileInfo(authData.userId);
            useProfileStore.getState().setProfile(profileData);
            toast.success(`Welcome back ${authData.alias}!`);
            navigate("/profile")
        } catch (err) {
            const error = err as AxiosError<{ message?: string }>;
            if (error.response) {
                const status = error.response.status;
                const backendMsg = error.response.data?.message;
                if (status === 429) {
                    toast.error("Too many requests. Please wait a little bit.");
                } else if (status === 500) {
                    toast.error("Server error. Please try again later.");
                } else if (status === 403) {
                    toast.error("Invalid username or password.");
                } else if (status === 404) {
                    toast.error("Not found.");
                } else if (status === 400) {
                    toast.warning(backendMsg);
                } else {
                    toast.error(backendMsg || `Unexpected error: ${status}`);
                }
            } else if (error.request) {
                toast.error("No response from server. Check your connection.");
            } else {
                toast.error(error.message);
            }
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="relative w-full max-w-3xl mx-auto min-h-[24rem] bg-[#E8DFC8] flex flex-col md:flex-row border border-[#C9A063] rounded-sm shadow-2xl overflow-hidden">
            {loading && <SubmitLoader />}

            <Pin
                className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 w-5 h-5 z-10 drop-shadow rotate-[-8deg]"
                style={{ color: "#D9A441" }}
                fill="#D9A441"
            />

            <div className="w-full md:w-1/2 flex items-center justify-center p-8">
            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6 w-full">
                <FormField
                    control={form.control}
                    name="email"
                    render={({ field }) => (
                    <FormItem>
                        <FormLabel className="font-display text-xs uppercase tracking-wide text-[#4A4136]">Email</FormLabel>
                        <FormControl>
                        <Input
                            placeholder="example@mail.com"
                            className="bg-[#F3EBD9] border-[#C9A063] text-[#241F1A] placeholder:text-[#8A7F6C] rounded-sm focus-visible:ring-[#D9A441] focus-visible:border-[#B23A2E]"
                            {...field}
                        />
                        </FormControl>
                        <FormMessage className="text-[#B23A2E]" />
                    </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="password"
                    render={({ field }) => (
                    <FormItem>
                        <FormLabel className="font-display text-xs uppercase tracking-wide text-[#4A4136]">Password</FormLabel>
                        <FormControl>
                        <Input
                            placeholder="my_secret_password"
                            type="password"
                            className="bg-[#F3EBD9] border-[#C9A063] text-[#241F1A] placeholder:text-[#8A7F6C] rounded-sm focus-visible:ring-[#D9A441] focus-visible:border-[#B23A2E]"
                            {...field}
                        />
                        </FormControl>
                        <FormMessage className="text-[#B23A2E]" />
                    </FormItem>
                    )}
                />
                <Button type="submit" className="w-full bg-[#B23A2E] hover:bg-[#9c3226] text-[#F3EBD9] rounded-sm font-medium">
                    Submit
                </Button>
                <div className="text-center mt-1 text-sm text-[#4A4136]">
                    Don't have an account?{" "}
                    <NavLink to="/signup" end>
                        <span className="text-[#B23A2E] hover:underline cursor-pointer font-medium">
                        Sign Up
                        </span>
                    </NavLink>
                </div>
                </form>
            </Form>
            </div>

            {/* divider — desktop */}
            <div className="hidden md:block absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 h-4/5 w-px bg-[#B23A2E]/50">
                <span className="absolute -left-4 top-1/2 -translate-y-1/2 bg-[#E8DFC8] border border-[#C9A063] px-2 font-hand text-lg text-[#241F1A] rounded-sm">
                    or
                </span>
            </div>

            {/* divider — mobile */}
            <div className="md:hidden flex items-center gap-3 px-8">
                <span className="flex-1 h-px bg-[#C9A063]" />
                <span className="font-hand text-lg text-[#241F1A]">or</span>
                <span className="flex-1 h-px bg-[#C9A063]" />
            </div>

            <div className="w-full md:w-1/2 bg-[#DDD0B0] flex flex-col items-center justify-center p-8 gap-4">
            <h1 className="font-display font-bold text-[#241F1A]">Log in with Google</h1>
            <Button
                type="button"
                variant="outline"
                asChild
                className="border-[#6B4A32] text-[#241F1A] bg-transparent hover:bg-[#B23A2E] hover:text-[#F3EBD9] hover:border-[#B23A2E] transition-colors rounded-sm"
            >
                <a href={`${API_BASE}/auth/oauth2/authorization/google`}>
                    Continue with Google
                </a>
            </Button>
            </div>

        </div>
    )
}