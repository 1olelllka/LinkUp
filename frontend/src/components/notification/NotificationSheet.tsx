import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
} from "@/components/ui/sheet";
import { useNotifications } from "@/hooks/useNotifications";
import { useProfileStore } from "@/store/useProfileStore";
import { useCallback, useRef, useState } from "react";
import { formatDistanceToNow } from "date-fns";
import { Pin, Trash } from "lucide-react";
import { Button } from "../ui/button";
import {
  deleteAllNotificationsForUser,
  deleteSpecificNotification,
} from "@/services/notificationService";
import { ServiceError } from "../errors/ServiceUnavailable";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { PageLoader } from "../load/PageLoader";

const NOTE_ROTATIONS = ["-rotate-1", "rotate-1", "rotate-0", "-rotate-2"];

export const NotificationSheet = ({
  trigger,
}: {
  trigger: React.ReactNode;
}) => {
  const [open, setOpen] = useState(false);

  const currentUser = useProfileStore(
    (state) => state.profile?.id,
  );

  const {
    notifications,
    notificationPage,
    loading,
    loadNewNotifications,
    setNotifications,
    setNotificationPage,
    markAsRead,
    error,
  } = useNotifications(
    open && currentUser ? currentUser : "",
  );

  const handleNewNotifications = useCallback(async () => {
    if (loading || !currentUser) return;

    await loadNewNotifications();
  }, [currentUser, loadNewNotifications, loading]);

  const observer = useRef<IntersectionObserver | null>(null);

  const observe = (
    el: HTMLDivElement | null,
    read: boolean,
  ) => {
    if (!el || read) return;

    if (!observer.current) {
      observer.current = new IntersectionObserver(
        (entries) => {
          entries.forEach((entry) => {
            if (entry.isIntersecting) {
              const id = entry.target.getAttribute("data-id");

              if (id) {
                markAsRead(id);
              }

              observer.current?.unobserve(entry.target);
            }
          });
        },
        { threshold: 0.5 },
      );
    }

    observer.current.observe(el);
  };

  const handleDeleteAll = () => {
    if (!currentUser) return;

    deleteAllNotificationsForUser(currentUser)
      .then((response) => {
        if (response.status === 204) {
          setNotifications([]);
          setNotificationPage(null);

          toast.success(
            "Successfully cleared your notifications!",
          );
        } else {
          toast.error(
            "Unexpected server response while deleting notifications.",
          );
        }
      })
      .catch((err) =>
        toast.error(
          "Error while deleting notifications. " +
            (err as AxiosError).message,
        ),
      );
  };

  const handleDelete = (id: string) => {
    deleteSpecificNotification(id)
      .then((response) => {
        if (response.status === 204) {
          setNotifications((prev) =>
            prev.filter((notification) => notification.id !== id),
          );

          toast.success("Notification removed.");
        }
      })
      .catch((err) =>
        toast.error(
          "Error while deleting the notification. " +
            (err as AxiosError).message,
        ),
      );
  };

  return (
    <Sheet open={open} onOpenChange={setOpen}>
      <SheetTrigger asChild>
        {trigger}
      </SheetTrigger>

      <SheetContent
        side="right"
        className="
          w-full
          sm:max-w-md
          border-l-2
          border-[#C9A063]
          bg-[#E8DFC8]
          p-3
          sm:p-4
          shadow-2xl
        "
      >
        {/* Paper board */}
        <div
          className="
            flex
            h-full
            flex-col
            rounded-sm
            bg-[#E8DFC8]
            text-[#241F1A]
            overflow-visible
          "
          style={{
            backgroundImage:
              "radial-gradient(rgba(107,74,50,0.12) 1px, transparent 1px)",
            backgroundSize: "14px 14px",
          }}
        >
        <SheetHeader
        className="
            relative
            shrink-0
            overflow-visible
            border-b
            border-[#C9A063]
            px-5
            py-5
        "
        >
        <Pin
            className="
            absolute
            -top-3
            left-1/2
            -translate-x-1/2
            w-6
            h-6
            rotate-[-10deg]
            drop-shadow-md
            z-20
            "
            style={{ color: "#D9A441" }}
            fill="#D9A441"
        />

        <div className="flex items-center justify-between gap-3 pt-1">
            <div>
            <span className="font-hand text-xl text-[#B23A2E]">
                little notes from your people
            </span>

            <SheetTitle
                className="
                font-display
                text-2xl
                font-bold
                text-[#241F1A]
                "
            >
                Notifications
            </SheetTitle>
            </div>

            {notifications.length > 0 && (
            <Button
                variant="outline"
                onClick={handleDeleteAll}
                disabled={loading}
                className="
                shrink-0
                rounded-sm
                border-[#B23A2E]
                bg-transparent
                text-[#B23A2E]
                font-display
                text-xs
                hover:bg-[#B23A2E]
                hover:text-[#F3EBD9]
                "
            >
                Clear all
            </Button>
            )}
        </div>
        </SheetHeader>

          {/* Error */}
          {error ? (
            <div className="flex-1 overflow-y-auto px-4 py-5">
              <ServiceError err={error} />
            </div>
          ) : (
            <>
              {/* Notifications */}
              {notifications.length > 0 ? (
                <div className="flex-1 overflow-y-auto px-4 py-5">
                  <div className="space-y-4">
                    {notifications.map((item, index) => (
                      <div
                        key={item.id}
                        data-id={item.id}
                        ref={(el) => observe(el, item.read)}
                        className={`
                          relative
                          flex
                          items-start
                          gap-3
                          border
                          border-[#C9A063]
                          p-4
                          shadow-md
                          transition-all
                          hover:-translate-y-0.5
                          hover:shadow-lg
                          ${NOTE_ROTATIONS[index % NOTE_ROTATIONS.length]}
                          ${
                            item.read
                              ? "bg-[#F3EBD9]"
                              : "bg-[#DDD0B0]"
                          }
                        `}
                      >
                        {/* Pin */}
                        <Pin
                          className="
                            absolute
                            -top-3
                            left-1/2
                            -translate-x-1/2
                            w-5
                            h-5
                            rotate-[-8deg]
                            drop-shadow
                          "
                          style={{ color: "#D9A441" }}
                          fill="#D9A441"
                        />

                        {/* Unread marker */}
                        {!item.read && (
                          <span
                            className="
                              mt-1.5
                              w-2
                              h-2
                              shrink-0
                              rounded-full
                              bg-[#B23A2E]
                            "
                          />
                        )}

                        <div className="flex min-w-0 flex-1 flex-col">
                          <p
                            className={`
                              text-sm
                              leading-relaxed
                              ${
                                item.read
                                  ? "text-[#4A4136]"
                                  : "font-medium text-[#241F1A]"
                              }
                            `}
                          >
                            {item.text}
                          </p>

                          <div
                            className="
                              mt-3
                              flex
                              items-center
                              justify-between
                              gap-3
                              border-t
                              border-[#C9A063]/60
                              pt-2
                            "
                          >
                            <span className="font-hand text-base text-[#8A7F6C]">
                              {formatDistanceToNow(
                                new Date(item.createdAt),
                                {
                                  addSuffix: true,
                                },
                              )}
                            </span>

                            <button
                              type="button"
                              onClick={(e) => {
                                e.preventDefault();
                                e.stopPropagation();
                                handleDelete(item.id);
                              }}
                              className="
                                rounded-sm
                                p-1
                                text-[#8A7F6C]
                                transition-colors
                                hover:bg-[#B23A2E]
                                hover:text-[#F3EBD9]
                              "
                              aria-label="Delete notification"
                            >
                              <Trash className="h-4 w-4" />
                            </button>
                          </div>
                        </div>
                      </div>
                    ))}

                    {/* Load more */}
                    {notificationPage &&
                      !notificationPage.last && (
                        <div className="pt-1 pb-2 text-center">
                          {!loading && (
                            <button
                              type="button"
                              onClick={handleNewNotifications}
                              className="
                                font-hand
                                text-xl
                                text-[#B23A2E]
                                transition-colors
                                hover:text-[#9C3226]
                                hover:underline
                              "
                            >
                              pin more notes →
                            </button>
                          )}
                        </div>
                      )}
                  </div>
                </div>
              ) : (
                !loading && (
                  <div className="flex flex-1 items-center justify-center px-6">
                    <div className="relative w-full max-w-xs">
                      <div
                        className="
                          bg-[#F3EBD9]
                          border
                          border-[#C9A063]
                          p-6
                          text-center
                          shadow-lg
                          rotate-[-2deg]
                        "
                      >
                        <Pin
                          className="
                            absolute
                            -top-3
                            left-1/2
                            -translate-x-1/2
                            w-5
                            h-5
                            rotate-[-10deg]
                          "
                          style={{ color: "#D9A441" }}
                          fill="#D9A441"
                        />

                        <span className="font-hand text-2xl text-[#8A7F6C]">
                          💤 peace and quiet
                        </span>

                        <p className="mt-2 text-sm text-[#4A4136]">
                          No new notifications.
                        </p>
                      </div>
                    </div>
                  </div>
                )
              )}

              {/* Loading */}
              {loading && (
                <div className="shrink-0 border-t border-[#C9A063] py-3">
                  <PageLoader />
                </div>
              )}
            </>
          )}
        </div>
      </SheetContent>
    </Sheet>
  );
};