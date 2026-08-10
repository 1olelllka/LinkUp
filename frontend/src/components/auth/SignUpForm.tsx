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
import { Pin } from "lucide-react";

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
      await register(values);
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
    <div className="relative w-full max-w-md mx-auto bg-[#E8DFC8] p-8 pt-10 rounded-sm border border-[#C9A063] shadow-2xl">
      {loading && <SubmitLoader />}

      <Pin
        className="absolute top-0 left-1/2 -translate-x-1/2 -translate-y-1/2 w-5 h-5 z-10 drop-shadow rotate-[-8deg]"
        style={{ color: "#D9A441" }}
        fill="#D9A441"
      />

      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
          <FormField
            control={form.control}
            name="email"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="font-display text-xs uppercase tracking-wide text-[#4A4136]">Email</FormLabel>
                <FormControl>
                  <Input
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
            name="alias"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="font-display text-xs uppercase tracking-wide text-[#4A4136]">Alias</FormLabel>
                <FormControl>
                  <Input
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
            name="name"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="font-display text-xs uppercase tracking-wide text-[#4A4136]">Name</FormLabel>
                <FormControl>
                  <Input
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
                    type="password"
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
            name="dateOfBirth"
            render={({ field }) => (
              <FormItem>
                <FormLabel className="font-display text-xs uppercase tracking-wide text-[#4A4136]">Date of Birth</FormLabel>
                <FormControl>
                  <Input
                    type="date"
                    max={new Date().toISOString().split("T")[0]}
                    className="bg-[#F3EBD9] border-[#C9A063] text-[#241F1A] rounded-sm focus-visible:ring-[#D9A441] focus-visible:border-[#B23A2E]"
                    {...field}
                  />
                </FormControl>
                <FormMessage className="text-[#B23A2E]" />
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

          <Button type="submit" className="w-full bg-[#B23A2E] hover:bg-[#9c3226] text-[#F3EBD9] rounded-sm font-medium">
            Submit
          </Button>
        </form>
      </Form>

      <div className="text-center mt-4 text-sm text-[#4A4136]">
        Already have an account?{" "}
        <NavLink to="/login">
          <span className="text-[#B23A2E] hover:underline cursor-pointer font-medium">
            Log in
          </span>
        </NavLink>
      </div>
    </div>
  );
};