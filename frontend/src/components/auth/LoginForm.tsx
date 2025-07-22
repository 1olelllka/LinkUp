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
import { NavLink } from "react-router";
import { login } from "@/services/authServices";
import { API_BASE } from "@/constants/routes";


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

    async function onSubmit(values: z.infer<typeof formSchema>) {
        try {
            const res = await login(values);
            console.log(res);
        } catch (err) {
            console.log(err);
        }
    }

    return (
        <div className="w-1/2 mx-auto h-96 bg-white flex relative border border-gray-300 rounded-2xl shadow-md overflow-hidden">
            <div className="w-1/2 bg-blue-100 flex items-center justify-center p-6">
            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-6 w-full">
                <FormField
                    control={form.control}
                    name="email"
                    render={({ field }) => (
                    <FormItem>
                        <FormLabel>Email</FormLabel>
                        <FormControl>
                        <Input placeholder="example@mail.com" {...field} />
                        </FormControl>
                        <FormMessage />
                    </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="password"
                    render={({ field }) => (
                    <FormItem>
                        <FormLabel>Password</FormLabel>
                        <FormControl>
                        <Input placeholder="my_secret_password" type="password" {...field} />
                        </FormControl>
                        <FormMessage />
                    </FormItem>
                    )}
                />
                <Button type="submit" className="w-full">Submit</Button>
                <div className="text-center mt-1 text-sm text-gray-600">
                    Don't have an account?{" "}
                    <NavLink to="/signup" end>
                        <span className="text-blue-600 hover:underline cursor-pointer">
                        Sign Up
                        </span>
                    </NavLink>
                </div>
                </form>
            </Form>
            </div>

            <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 h-4/5 w-px bg-gray-300">
                <span className="absolute -left-4 top-1/2 -translate-y-1/2 bg-white px-2 text-sm font-light text-gray-600 rounded-md">
                    OR
                </span>
            </div>

            <div className="w-1/2 bg-green-100 flex flex-col items-center justify-center p-6">
            <h1 className="mb-4 text-lg font-semibold">Log in with Google</h1>
            <Button type="button" variant="outline" asChild>
                <a href={`${API_BASE}/auth/oauth2/authorization/google`}>
                    Continue with Google
                </a>
            </Button>
            </div>

        </div>
    )
}