import { useState } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";

import { register } from "@/services/authServices";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Form, FormControl, FormField, FormItem, FormLabel, FormMessage,
} from "@/components/ui/form";
import { GenderSelect } from "./GenderSelect";
import { NavLink, useNavigate } from "react-router";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { SubmitLoader } from "../load/SubmitLoader";

const formSchema = z.object({
  alias: z.string().min(8, "Alias must be at least 8 characters and contain only letters, digits, or underscores."),
  email: z.email("Please write a valid email"),
  password: z.string().regex(/^(?=(?:.*\d){2,})(?=.*[A-Z])(?=.*[a-z]).{8,}$/, "Password must be at least 8 characters, contain at least two numbers, one capital letter, and one small letter."),
  name: z.string().min(1, "Name must not be blank"),
  gender: z.enum(["MALE", "FEMALE", "UNDEFINED"], "Gender must not be null."),
  dateOfBirth: z.string().refine(
        (val) => !isNaN(Date.parse(val)),
        "Date of birth must be a valid date"
    )
});
export const SignUpForm = () => {
    const [gender, setGender] = useState<"MALE" | "FEMALE" | "UNDEFINED">("UNDEFINED");
    const navigate = useNavigate();
    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: {
        email: "",
        password: "",
        name: "",
        alias: "",
        gender: gender,
        dateOfBirth: ""
        },
    });
    const [loading, setLoading] = useState(false);

  const onSubmit = async (values: z.infer<typeof formSchema>) => {
    setLoading(true);
    try {
      const data = await register(values);
      console.log("Registered:", data);
      toast.success(`Successfully registered, ${values.alias}! Proceeding to login page...`);
      navigate("/login");
    } catch (err) {
        const error = err as AxiosError<{ message?: string }>;
        if (error.response) {
          const status = error.response.status;
          const backendMsg = error.response.data?.message;

          if (status === 429) {
              toast.error("Too many requests. Please wait a little bit.");
          } else if (status === 500) {
              toast.error("Server error. Please try again later.");
          } else if (status === 409) {
              toast.error(backendMsg);
          } else if (status == 400) {
            toast.error(backendMsg);
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
  };

  return (
    <div className="w-full max-w-md bg-white p-8 rounded-2xl shadow-md border border-gray-200">
      {loading && <SubmitLoader />}
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
          <FormField
            control={form.control}
            name="email"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Email</FormLabel>
                <FormControl><Input {...field} /></FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="alias"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Alias</FormLabel>
                <FormControl><Input {...field} /></FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="name"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Name</FormLabel>
                <FormControl><Input {...field} /></FormControl>
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
                <FormControl><Input type="password" {...field} /></FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="dateOfBirth"
            render={({ field }) => (
              <FormItem>
                <FormLabel>Date of Birth</FormLabel>
                <FormControl>
                  <Input type="date" max={new Date().toISOString().split("T")[0]} {...field} />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />

          <FormField
            control={form.control}
            name="gender"
            render={({ field }) => (
              <GenderSelect value={gender} onSelect={(val) => {
                setGender(val);
                field.onChange(val);
              }} />
            )}
          />

          <Button type="submit" className="w-full">Submit</Button>
        </form>
      </Form>

      <div className="text-center mt-4 text-sm text-gray-600">
        Already have an account?{" "}
        <NavLink to="/login">
          <span className="text-blue-600 hover:underline cursor-pointer">
            Log in
          </span>
        </NavLink>
      </div>
    </div>
  );
};
