import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
import axios from "axios";

import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import {
  DropdownMenu,
  DropdownMenuItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Input } from "@/components/ui/input";
import { useState } from "react";
import { NavLink } from "react-router";


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

export const SignUpPage = () => {
  const [gender, setGender] = useState<"MALE" | "FEMALE" | "UNDEFINED">("UNDEFINED");
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

  function onSubmit(values: z.infer<typeof formSchema>) {
    axios
      .post("http://localhost:8080/api/auth/register", values)
      .then((response) => {
        console.log(response.data);
      })
      .catch((error) => {
        console.log(error);
      });
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-50 px-4">
      <h1 className="text-4xl font-bold text-gray-800 mb-6">Sign Up</h1>

      <div className="w-full max-w-md bg-white p-8 rounded-2xl shadow-md border border-gray-200">
        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
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
              name="alias"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Alias</FormLabel>
                  <FormControl>
                    <Input placeholder="username1234" {...field} />
                  </FormControl>
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
                  <FormControl>
                    <Input placeholder="My Name" {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
                control={form.control}
                name="gender"
                render={({ field }) => (
                    <FormItem>
                    <FormLabel>Gender</FormLabel>
                    <FormControl>
                        <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button variant="outline">{gender}</Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent className="w-56">
                            <DropdownMenuLabel>Choose your gender</DropdownMenuLabel>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                            onSelect={() => {
                                field.onChange("MALE")
                                setGender("MALE")
                            }}
                            >
                            MALE
                            </DropdownMenuItem>
                            <DropdownMenuItem
                            onSelect={() => {
                                field.onChange("FEMALE")
                                setGender("FEMALE")
                            }}
                            >
                            FEMALE
                            </DropdownMenuItem>
                            <DropdownMenuItem
                            onSelect={() => {
                                field.onChange("UNDEFINED")
                                setGender("UNDEFINED")
                            }}
                            >
                            UNDEFINED
                            </DropdownMenuItem>
                        </DropdownMenuContent>
                        </DropdownMenu>
                    </FormControl>
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
                        <Input
                        type="date"
                        {...field}
                        max={new Date().toISOString().split("T")[0]}
                        />
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
                    <Input
                      placeholder="my_secret_password"
                      type="password"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <Button type="submit" className="w-full">
              Submit
            </Button>
          </form>
        </Form>

        <div className="text-center mt-4 text-sm text-gray-600">
          Already have an account?{" "}
          <NavLink to="/login" end>
            <span className="text-blue-600 hover:underline cursor-pointer">
              Log in
            </span>
          </NavLink>
        </div>
      </div>
    </div>
  );
};
