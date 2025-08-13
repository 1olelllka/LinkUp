import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet"
import { useNotifications } from "@/hooks/useNotifications";
import { useProfileStore } from "@/store/useProfileStore";
import { useCallback, useRef, useState } from "react"
import { formatDistanceToNow } from "date-fns"
import { Trash } from "lucide-react";
import { Button } from "../ui/button";
import { deleteAllNotificationsForUser, deleteSpecificNotification } from "@/services/notificationService";

export const NotificationSheet = ({trigger} : {trigger: React.ReactNode}) => {
    const [open, setOpen] = useState(false);
    const currentUser = useProfileStore(state => state.profile?.userId);
    const {notifications, notificationPage, loading, loadNewNotifications, setNotifications, setNotificationPage, markAsRead} =
        useNotifications(open && currentUser ? currentUser : "");

    const handleNewNotitications = useCallback(async () => {
        if (loading || !currentUser) return;
        await loadNewNotifications();
    }, [currentUser, loadNewNotifications, loading]);

    const observer = useRef<IntersectionObserver | null>(null);

    const observe = (el: HTMLDivElement | null, read: boolean) => {
        if (!el || read) return;
        if (!observer.current) {
            observer.current = new IntersectionObserver(entries => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    markAsRead(entry.target.getAttribute("data-id")!);
                    observer.current?.unobserve(entry.target);
                }
            });
            }, { threshold: 0.5 });
        }
        observer.current.observe(el);
    };


    return (
        <Sheet open={open} onOpenChange={setOpen}>
        <SheetTrigger asChild>{trigger}</SheetTrigger>
        <SheetContent>
            <SheetHeader>
                <div className="flex flex-row space-x-2">
                    <SheetTitle className="text-3xl">Notifications</SheetTitle>
                    <Button
                        variant={"outline"}
                        className="text-red-500 w-20 hover:text-red-600"
                        onClick={(e) => {
                            e.preventDefault();
                            if (currentUser) {
                                deleteAllNotificationsForUser(currentUser)
                                .then(response => {
                                    console.log(response)
                                    if (response.status == 204) {
                                        setNotifications([]);
                                        setNotificationPage(null);
                                    } else {
                                        console.log("Unexpected status code - " + response);
                                    }
                                })
                                .catch(err => console.log(err));
                            }
                        }}
                    >
                        Delete All
                    </Button>
                </div>
            </SheetHeader>
            {notifications.length > 0 ? (
                <div className="my-2 space-y-3 mx-3 overflow-y-auto">
                    {notifications.map(item => (
                    <div
                        key={item.id}
                        data-id={item.id}
                        className={`flex items-start gap-3 p-3 rounded-lg border transition-colors
                        ${item.read ? "bg-white hover:bg-slate-50" : "bg-blue-50 hover:bg-blue-100"}`}
                        ref={el => observe(el, item.read)}
                    >
                        {!item.read && <span className="mt-2 w-2 h-2 rounded-full bg-blue-500" />}

                        <div className="flex flex-col flex-1">
                        <p className="text-sm text-slate-800">{item.text}</p>
                        <div className="flex flex-row justify-between">
                            <span className="text-xs text-slate-500">
                                {formatDistanceToNow(new Date(item.createdAt), { addSuffix: true })}
                            </span>
                            <Trash 
                            color={"red"} size={15} 
                            className="self-center"
                            onClick={(e) => {
                                e.preventDefault();
                                deleteSpecificNotification(item.id)
                                .then(response => {
                                    if (response.status == 204) {
                                        setNotifications((prev) => prev.filter(a => a.id != item.id));
                                    } else {
                                        console.log("Unexpected response status - " + response);
                                    }
                                }).catch(err => console.log(err));
                            }}/>
                        </div>
                        </div>
                    </div>
                    ))}
                    {notificationPage && !notificationPage.last && 
                    <div className="mt-2">
                        {loading 
                        ? <p 
                        className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400">
                        🔄 Loading...</p>
                        : <p 
                        className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400"
                        onClick={handleNewNotitications}
                        >🚀 Load More</p>
                        }
                    </div>
                    }
                </div>
                ) : (
                <p className="text-md text-slate-500 text-center mt-5">
                    💤 Peace and quiet — no new notifications
                </p>
                )}
        </SheetContent>
        </Sheet>
    )
}