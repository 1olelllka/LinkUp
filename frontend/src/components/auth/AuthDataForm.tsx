import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DialogFooter,
  DialogClose
} from "@/components/ui/dialog"
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form"
import { Input } from "@/components/ui/input"
import { z } from "zod"
import { zodResolver } from "@hookform/resolvers/zod"
import { useForm } from "react-hook-form"
import { useEffect } from "react";
import { patchMe } from "@/services/authServices";

const formSchema = z.object({
    email: z.email("Invalid Email."),
    alias: z.string().regex(/^\w{8,}$/, "Alias must be at least 8 characters and contain only letters, digits, or underscores."),
})

type AuthDataFormProps = {
    email: string | undefined,
    alias: string | undefined,
    authProvier: string | undefined
}

export const AuthDataForm = (data : AuthDataFormProps) => {

    const form = useForm<z.infer<typeof formSchema>>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            email: data.email,
            alias: data.alias,
        },
    })

    useEffect(() => {
        form.reset({
            email: data.email,
            alias: data.alias
        })
    }, [form, data])

    async function onSubmit(values: z.infer<typeof formSchema>) {
        try {
            patchMe(values).then((response) => {

                form.reset({
                    email: response.email,
                    alias: response.alias
                })
            })
        } catch (err) {
            console.log(err);
        }
    }

    return (
        <>
            <Dialog>
                    <div className="text-right mb-0">
                        <DialogTrigger asChild>
                                <Button type="button">Edit Auth Data</Button>
                        </DialogTrigger>
                    </div>
                    <DialogContent className="sm:max-w-[425px]">
                    <DialogHeader>
                        <DialogTitle>Edit Auth Data</DialogTitle>
                        <DialogDescription>
                        Make changes to your authorization data here. Click save when you&apos;re
                        done.
                        </DialogDescription>
                    </DialogHeader>
                    <Form {...form}>
                        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
                        <div className="grid gap-4">
                            <FormField
                                control={form.control}
                                name="email"
                                render={({ field }) => (
                                    <FormItem>
                                    <FormLabel>Email</FormLabel>
                                    <FormControl>
                                        <Input {...field} disabled={data.authProvier == "GOOGLE" ? true : false}/>
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
                                        <Input {...field} />
                                    </FormControl>
                                    <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </div>
                        <DialogFooter>
                            <DialogClose asChild>
                            <Button variant="outline">Close</Button>
                            </DialogClose>
                            <Button type="submit">Save changes</Button>
                        </DialogFooter>
                        </form>
                    </Form>
                    </DialogContent>
            </Dialog>
        </>
    );
}