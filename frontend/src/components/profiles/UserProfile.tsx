import { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import {z} from "zod";
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import axios from "axios";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import {
  DropdownMenu,
  DropdownMenuItem,
  DropdownMenuContent,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { AuthDataForm } from "./AuthDataForm";


type Profile = {
  id: string,
  alias: string,
  email: string,
  name: string,
  authProvider: string,
  photo: string,
  aboutMe: string,
  gender: "MALE" | "FEMALE" | "UNDEFINED",
  dateOfBirth: string,
  createdAt: string
}

const formSchema = z.object({
  name: z.string().min(1, "Name must not be empty."),
  aboutMe: z.string().optional(),
  gender: z.enum(["MALE", "FEMALE", "UNDEFINED"]),
  dateOfBirth: z.string().refine(
    (val) => !isNaN(Date.parse(val)),
    "Date of birth must be a valid date"
  )
});

export const UserProfile = () => {
  const [profile, setProfile] = useState<Profile>();
  const [editing, setEditing] = useState(false);

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      name: "",
      aboutMe: "",
      gender: "UNDEFINED",
      dateOfBirth: ""
    },
  });

  useEffect(() => {
    const fetchData = async () => {
      try {
        const authResponse = await axios.get("http://localhost:8080/api/auth/me", {
          headers: {
            Authorization: "Bearer eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJMaW5rVXAiLCJzdWIiOiIyZTRmMDI4ZS1mZTlhLTQxM2ItOGY3MS01YzFmZjY0OTdhYzgiLCJpYXQiOjE3NTI2NjI1OTEsImV4cCI6MTc1MjY2NjE5MX0.fYIZbN5avZV6CIywCNp5y2CLgZVZDZT9VpHZZVoJsamZ9-FPoGz8mAez1zaWB-B3"
          }
        });

        const user = authResponse.data;
        setProfile(user);
        const profileResponse = await axios.get(`http://localhost:8080/api/profiles/${user.userId}`);
        const combinedProfile = {
          ...user,
          ...profileResponse.data,
        };
        setProfile(combinedProfile);
      } catch (error) {
        console.error(error);
      }
    };

    fetchData();
  }, []);

  useEffect(() => {
    if (profile) {
      form.reset({
        name: profile.name || "",
        aboutMe: profile.aboutMe || "",
        gender: profile.gender || "UNDEFINED",
        dateOfBirth: profile.dateOfBirth || ""
      });
    }
  }, [profile, form]);

  const handleSave = async (data: z.infer<typeof formSchema>) => {
    try {
      await axios.patch(`http://localhost:8080/api/profiles/${profile?.id}`, data, {
        headers: {
            Authorization: "eyJhbGciOiJIUzM4NCJ9.eyJpc3MiOiJMaW5rVXAiLCJzdWIiOiIyZTRmMDI4ZS1mZTlhLTQxM2ItOGY3MS01YzFmZjY0OTdhYzgiLCJpYXQiOjE3NTI2NjI1OTEsImV4cCI6MTc1MjY2NjE5MX0.fYIZbN5avZV6CIywCNp5y2CLgZVZDZT9VpHZZVoJsamZ9-FPoGz8mAez1zaWB-B3"
        }
      });
      setEditing(false);
      if (profile) {
        setProfile({
          ...profile,
          ...data
        });
      }
      console.log("Updated profile:", data);
    } catch (err) {
      console.log(err);
    }
  };

  const handleEdit = () => {
    setEditing(true);
    if (profile) {
      form.reset({
        name: profile.name || "",
        aboutMe: profile.aboutMe || "",
        gender: profile.gender || "UNDEFINED",
        dateOfBirth: profile.dateOfBirth || ""
      });
    }
  };

  const currentGender = form.watch("gender");

  return (
    <div className="bg-white p-6 rounded-xl shadow border border-gray-200">
      <div className="flex items-center space-x-6 mb-6">
         <div className="w-24 h-24 rounded-full border flex items-center justify-center bg-gray-200">
          {profile?.photo ? (
            <img
              src={profile.photo}
              alt="Profile"
              className="w-full h-full rounded-full object-cover"
            />
          ) : (
            <span className="text-3xl font-bold text-gray-600">
              {profile?.name?.charAt(0).toUpperCase() || profile?.alias?.charAt(0).toUpperCase() || '?'}
            </span>
          )}
        </div>
        <div>
          <h2 className="text-2xl font-bold">{profile?.alias}</h2>
          <p className="text-sm text-gray-500">Joined on {profile?.createdAt}</p>
        </div>
      </div>

      {/* TODO: create followers/followees functionality */}
      
      <div className="space-y-4">
        <div>
          <Label>Email</Label>
          <Input value={profile?.email || ""} disabled className="mt-1" />
        </div>

        <div>
          <Label>Authentication Provider</Label>
          <Input value={profile?.authProvider || ""} disabled className="mt-1" />
        </div>
        
        <AuthDataForm email={profile?.email} alias={profile?.alias} authProvier={profile?.authProvider}/>

        <Form {...form}>
          <div className="space-y-8">
            <FormField
              control={form.control}
              name="aboutMe"
              render={({ field }) => (
                <FormItem className="mb-4">
                  <FormLabel>About Me</FormLabel>
                  <FormControl>
                    <Textarea
                      disabled={!editing}
                      className="mt-1"
                      {...field}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="name"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>Name</FormLabel>
                    <FormControl>
                      <Input
                        disabled={!editing}
                        className="mt-1"
                        {...field}
                      />
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
                        <DropdownMenuTrigger asChild disabled={!editing}>
                          <Button variant="outline" className="mt-1 w-full justify-start">
                            {currentGender}
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent className="w-56">
                          <DropdownMenuLabel>Choose your gender</DropdownMenuLabel>
                          <DropdownMenuSeparator />
                          <DropdownMenuItem
                            onSelect={() => field.onChange("MALE")}
                          >
                            MALE
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            onSelect={() => field.onChange("FEMALE")}
                          >
                            FEMALE
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            onSelect={() => field.onChange("UNDEFINED")}
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
                        className="mt-1"
                        disabled={!editing}
                        type="date"
                        {...field}
                        max={new Date().toISOString().split("T")[0]}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>
            <div className="text-right">
              {editing ? (
                <div className="space-x-2">
                  <Button 
                    type="button" 
                    variant="outline" 
                    onClick={() => setEditing(false)}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" onClick={() => form.handleSubmit(handleSave)()}>Save</Button>
                </div>
              ) : (
                <Button variant="outline" type="button" onClick={handleEdit}>
                  Edit Personal Data
                </Button>
              )}
            </div>
          </div>
        </Form>
      </div>
    </div>
  );
};