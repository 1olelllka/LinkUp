import { useState, useEffect } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import {z} from "zod";
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
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
import type { Profile } from "@/types/Profile";
import { patchPersonalProfileInfo } from "@/services/profileServices";

const formSchema = z.object({
  name: z.string().min(1, "Name must not be empty."),
  aboutMe: z.string().optional(),
  gender: z.enum(["MALE", "FEMALE", "UNDEFINED"]),
  dateOfBirth: z.string().refine(
    (val) => !isNaN(Date.parse(val)),
    "Date of birth must be a valid date"
  )
});

type PersonalDataFormProps = {
  profile: Profile | undefined;
  setProfile: (param: Profile) => void;
};

export const PersonalDataForm = ({profile, setProfile} : PersonalDataFormProps) => {
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
        if (profile) {
          form.reset({
              name: profile.name || "",
              aboutMe: profile.aboutMe || "",
              gender: profile.gender || "UNDEFINED",
              dateOfBirth: profile.dateOfBirth || ""
          });
        }
    }, [profile, form]);

    const currentGender = form.watch("gender");

    const handleSave = async (data: z.infer<typeof formSchema>) => {
        try {
          if (!profile?.id) return;
          await patchPersonalProfileInfo(profile?.id, data)
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

    return (
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
    );
}