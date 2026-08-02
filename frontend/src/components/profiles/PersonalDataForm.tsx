import { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { z } from "zod";
import { Label } from "@/components/ui/label";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";
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
} from "@/components/ui/dropdown-menu";
import type { Profile } from "@/types/Profile";
import {
  deleteProfile,
  patchPersonalProfileInfo,
} from "@/services/profileServices";
import type { AxiosError } from "axios";
import { toast } from "sonner";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import { useProfileStore } from "@/store/useProfileStore";
import { useNavigate } from "react-router";
import { Pin } from "lucide-react";

const formSchema = z.object({
  username: z.string().min(1, "Username must not be empty."),
  name: z.string().min(1, "Name must not be empty."),
  aboutMe: z.string().optional(),
  gender: z.enum(["MALE", "FEMALE", "UNDEFINED"]),
  dateOfBirth: z
    .string()
    .refine(
      (val) => !isNaN(Date.parse(val)),
      "Date of birth must be a valid date",
    ),
});

type PersonalDataFormProps = {
  profile: Profile | undefined;
  setProfile: (param: Profile) => void;
};

export const PersonalDataForm = ({
  profile,
  setProfile,
}: PersonalDataFormProps) => {
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  const form = useForm<z.infer<typeof formSchema>>({
    resolver: zodResolver(formSchema),
    defaultValues: {
      username: "",
      name: "",
      aboutMe: "",
      gender: "UNDEFINED",
      dateOfBirth: "",
    },
  });

  const navigate = useNavigate();

  useEffect(() => {
    if (profile) {
      form.reset({
        username: profile.username,
        name: profile.name || "",
        aboutMe: profile.aboutMe || "",
        gender: profile.gender || "UNDEFINED",
        dateOfBirth: profile.dateOfBirth || "",
      });
    }
  }, [profile, form]);

  const currentGender = form.watch("gender");

  const onSubmit = async (data: z.infer<typeof formSchema>) => {
    if (!profile?.id) return;

    const changedFields = (
      Object.keys(data) as Array<keyof typeof data>
    ).reduce(
      (acc, key) => {
        if (data[key] !== profile[key]) {
          acc[key] = data[key] as never;
        }

        return acc;
      },
      {} as Partial<typeof data>,
    );

    if (Object.keys(changedFields).length === 0) {
      toast.info("No changes to save.");
      return;
    }

    try {
      await patchPersonalProfileInfo(profile.id, changedFields);

      setProfile({
        ...profile,
        ...changedFields,
      });

      toast.success("Successfully updated personal data!");
    } catch (err) {
      const error = err as AxiosError<{ message: string }>;

      if (error.status === 400 || error.status == 409) {
        toast.error(
          "Validation error: " + (error.response?.data?.message ?? error.message),
        );
      } else if (error.status === 404 || error.status === 401) {
        toast.error(
          "Client Error: " + (error.response?.data?.message ?? error.message),
        );
      } else {
        toast.error(error.message);
      }
    }
  };

  const handleDelete = () => {
    const profileId = useProfileStore.getState().profile?.id;

    if (!profileId) return;

    deleteProfile(profileId)
      .then((status) => {
        if (status === 204) {
          navigate("/");
        } else {
          toast.warning(
            "Unknown error occurred. Please try again later. " +
              "The request failed with status " +
              status,
          );
        }
      })
      .catch((err) => {
        toast.error((err as AxiosError).message);
      });
  };

  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button
          className="
            rounded-sm
            bg-[#B23A2E]
            text-[#F3EBD9]
            font-display
            hover:bg-[#9C3226]
            shadow-md
          "
        >
          Edit profile
        </Button>
      </DialogTrigger>
      <DialogContent
        className="
          max-h-[90vh]
          overflow-y-auto
          sm:max-w-[620px]
          p-3
          sm:p-5
          rounded-md
          bg-[#E8DFC8]
          text-[#241F1A]
          border-[#C9A063]
          border-2
          shadow-2xl
        "
      >
        {/* Paper sheet */}
        <div
          className="
            relative
          "
          style={{
            backgroundImage:
              "radial-gradient(rgba(107,74,50,0.12) 1px, transparent 1px)",
            backgroundSize: "14px 14px",
          }}
        >
          {/* Pin */}
          <Pin
            className="
              absolute
              -top-4
              left-1/2
              -translate-x-1/2
              w-7
              h-7
              rotate-[-8deg]
              drop-shadow-md
              z-10
            "
            style={{ color: "#D9A441" }}
            fill="#D9A441"
          />

          <DialogHeader className="text-left mb-7">
            <span className="font-hand text-2xl text-[#B23A2E]">
              update your card
            </span>

            <DialogTitle
              className="
                font-display
                text-2xl
                sm:text-3xl
                font-bold
                text-[#241F1A]
              "
            >
              Edit Profile
            </DialogTitle>

            <DialogDescription
              className="
                text-[#4A4136]
                leading-relaxed
                text-sm
              "
            >
              Keep your personal details up to date. Changes will appear
              across your LinkUp profile.
            </DialogDescription>
          </DialogHeader>

          <Form {...form}>
            <form
              onSubmit={form.handleSubmit(onSubmit)}
              className="space-y-6"
            >
              {/* Email */}
              <div
                className="
                  relative
                  bg-[#F3EBD9]
                  border
                  border-[#C9A063]
                  p-4
                  shadow-sm
                  rotate-[-0.5deg]
                "
              >
                <Label
                  className="
                    font-display
                    text-xs
                    uppercase
                    tracking-wider
                    text-[#4A4136]
                  "
                >
                  Email
                </Label>

                <Input
                  value={profile?.email || ""}
                  disabled
                  className="
                    mt-2
                    border-[#C9A063]
                    bg-[#E8DFC8]
                    text-[#6A5E4C]
                    rounded-sm
                    disabled:opacity-100
                    font-medium
                  "
                />
              </div>

              {/* Username */}
              <FormField
                control={form.control}
                name="username"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel
                      className="
                        font-display
                        text-sm
                        text-[#241F1A]
                      "
                    >
                      Username
                    </FormLabel>

                    <FormControl>
                      <Input
                        {...field}
                        className="
                          rounded-sm
                          border-[#C9A063]
                          bg-[#F3EBD9]
                          text-[#241F1A]
                          placeholder:text-[#8A7F6C]
                          focus-visible:ring-[#D9A441]
                          focus-visible:border-[#D9A441]
                        "
                      />
                    </FormControl>

                    <FormMessage className="text-[#B23A2E]" />
                  </FormItem>
                )}
              />

              {/* Name + Gender */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <FormField
                  control={form.control}
                  name="name"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel
                        className="
                          font-display
                          text-sm
                          text-[#241F1A]
                        "
                      >
                        Name
                      </FormLabel>

                      <FormControl>
                        <Input
                          {...field}
                          className="
                            rounded-sm
                            border-[#C9A063]
                            bg-[#F3EBD9]
                            text-[#241F1A]
                            focus-visible:ring-[#D9A441]
                            focus-visible:border-[#D9A441]
                          "
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
                    <FormItem>
                      <FormLabel
                        className="
                          font-display
                          text-sm
                          text-[#241F1A]
                        "
                      >
                        Gender
                      </FormLabel>

                      <FormControl>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button
                              variant="outline"
                              className="
                                w-full
                                justify-between
                                rounded-sm
                                border-[#C9A063]
                                bg-[#F3EBD9]
                                text-[#241F1A]
                                hover:bg-[#DDD0B0]
                                hover:text-[#241F1A]
                                font-medium
                              "
                            >
                              {currentGender}
                            </Button>
                          </DropdownMenuTrigger>

                          <DropdownMenuContent
                            align="start"
                            className="
                              w-56
                              rounded-sm
                              border-[#C9A063]
                              bg-[#F3EBD9]
                              text-[#241F1A]
                            "
                          >
                            <DropdownMenuLabel
                              className="
                                font-display
                                text-xs
                                uppercase
                                tracking-wider
                                text-[#4A4136]
                              "
                            >
                              Choose your gender
                            </DropdownMenuLabel>

                            <DropdownMenuSeparator className="bg-[#C9A063]" />

                            <DropdownMenuItem
                              onSelect={() => field.onChange("MALE")}
                              className="cursor-pointer focus:bg-[#DDD0B0]"
                            >
                              MALE
                            </DropdownMenuItem>

                            <DropdownMenuItem
                              onSelect={() => field.onChange("FEMALE")}
                              className="cursor-pointer focus:bg-[#DDD0B0]"
                            >
                              FEMALE
                            </DropdownMenuItem>

                            <DropdownMenuItem
                              onSelect={() => field.onChange("UNDEFINED")}
                              className="cursor-pointer focus:bg-[#DDD0B0]"
                            >
                              UNDEFINED
                            </DropdownMenuItem>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </FormControl>

                      <FormMessage className="text-[#B23A2E]" />
                    </FormItem>
                  )}
                />
              </div>

              {/* Date of birth */}
              <FormField
                control={form.control}
                name="dateOfBirth"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel
                      className="
                        font-display
                        text-sm
                        text-[#241F1A]
                      "
                    >
                      Date of Birth
                    </FormLabel>

                    <FormControl>
                      <Input
                        type="date"
                        {...field}
                        max={new Date().toISOString().split("T")[0]}
                        className="
                          rounded-sm
                          border-[#C9A063]
                          bg-[#F3EBD9]
                          text-[#241F1A]
                          focus-visible:ring-[#D9A441]
                          focus-visible:border-[#D9A441]
                        "
                      />
                    </FormControl>

                    <FormMessage className="text-[#B23A2E]" />
                  </FormItem>
                )}
              />

              {/* About me */}
              <FormField
                control={form.control}
                name="aboutMe"
                render={({ field }) => (
                  <FormItem>
                    <div className="flex items-center justify-between">
                      <FormLabel
                        className="
                          font-display
                          text-sm
                          text-[#241F1A]
                        "
                      >
                        About Me
                      </FormLabel>

                      <span className="font-hand text-lg text-[#8A7F6C]">
                        tell your people something
                      </span>
                    </div>

                    <FormControl>
                      <Textarea
                        {...field}
                        maxLength={300}
                        className="
                          min-h-[120px]
                          resize-none
                          rounded-sm
                          border-[#C9A063]
                          bg-[#F3EBD9]
                          text-[#241F1A]
                          placeholder:text-[#8A7F6C]
                          focus-visible:ring-[#D9A441]
                          focus-visible:border-[#D9A441]
                        "
                      />
                    </FormControl>

                    <FormMessage className="text-[#B23A2E]" />
                  </FormItem>
                )}
              />

              {/* Footer */}
              <DialogFooter
                className="
                  pt-5
                  border-t
                  border-[#C9A063]
                  sm:justify-between
                  gap-3
                "
              >
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => setDeleteConfirmOpen(true)}
                  className="
                    rounded-sm
                    border-[#B23A2E]
                    bg-transparent
                    text-[#B23A2E]
                    font-display
                    hover:bg-[#B23A2E]
                    hover:text-[#F3EBD9]
                  "
                >
                  Delete
                </Button>

                <div className="flex gap-2">
                  <DialogClose asChild>
                    <Button
                      type="button"
                      variant="outline"
                      className="
                        rounded-sm
                        border-[#8A7F6C]
                        bg-transparent
                        text-[#4A4136]
                        font-display
                        hover:bg-[#DDD0B0]
                        hover:text-[#241F1A]
                      "
                    >
                      Close
                    </Button>
                  </DialogClose>

                  <Button
                    type="submit"
                    className="
                      rounded-sm
                      bg-[#B23A2E]
                      text-[#F3EBD9]
                      font-display
                      hover:bg-[#9C3226]
                      shadow-sm
                    "
                  >
                    Save changes
                  </Button>
                </div>
              </DialogFooter>
            </form>
          </Form>

          {/* Delete-account confirmation — separate nested dialog, only
              handleDelete actually deletes anything */}
          <Dialog open={deleteConfirmOpen} onOpenChange={setDeleteConfirmOpen}>
            <DialogContent className="sm:max-w-[425px] bg-[#E8DFC8] text-[#241F1A] border-[#C9A063] rounded-sm shadow-2xl">
              <DialogHeader>
                <span className="font-hand text-xl text-[#D9A441]">
                  take this card down
                </span>
                <DialogTitle className="font-display text-2xl font-bold text-[#241F1A]">
                  Delete your account?
                </DialogTitle>
                <DialogDescription className="text-[#4A4136]">
                  This permanently deletes your profile, posts, and everything
                  else — it can't be undone.
                </DialogDescription>
              </DialogHeader>
              <DialogFooter className="gap-2 sm:gap-2">
                <DialogClose asChild>
                  <Button
                    variant="outline"
                    className="rounded-sm border-[#8A7F6C] bg-transparent text-[#4A4136] hover:bg-[#DDD0B0]"
                  >
                    Cancel
                  </Button>
                </DialogClose>
                <Button
                  className="rounded-sm bg-[#B23A2E] text-[#F3EBD9] hover:bg-[#9c3226]"
                  onClick={handleDelete}
                >
                  Delete my account
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        </div>
      </DialogContent>
    </Dialog>
  );
};