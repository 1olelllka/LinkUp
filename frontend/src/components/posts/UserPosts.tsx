import { Card, CardContent } from "@/components/ui/card";
import { useUserPosts } from "@/hooks/useUserPosts";
import { PostModal } from "./PostModal";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { MoreHorizontal, Pin } from "lucide-react";
import { useProfileStore } from "@/store/useProfileStore";
import { deletePostById } from "@/services/postServices";
import { useCallback, useState } from "react";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";
import { toast } from "sonner";
import type { AxiosError } from "axios";
import { CreatePost } from "./CreatePost";
import { UpdatePost } from "./UpdatePost";

const CARD_ROTATIONS = ["-rotate-1", "rotate-1", "rotate-2", "-rotate-2"];

export const UserPosts = ({ userId }: { userId: string | undefined }) => {
  const {
    posts,
    setPosts,
    postPage,
    loadMorePosts,
    loading,
    pageLoading,
    error,
  } = useUserPosts(userId);
  const currentUser = useProfileStore((state) => state.profile?.id);
  const [updatePostId, setUpdatePostId] = useState<number | null>(null);
  const [updateDialogOpen, setUpdateDialogOpen] = useState(false);

  const handleLoadPosts = useCallback(async () => {
    await loadMorePosts();
  }, [loadMorePosts]);

  return (
    <div className="mt-5 bg-[#E8DFC8] border border-[#C9A063] p-6 rounded-sm shadow-lg transition-all w-[99%] flex-1 min-h-0 flex flex-col">
      {error ? (
        <>
          <h2 className="font-display text-2xl font-bold mb-4 text-[#241F1A]">
            Posts
          </h2>
          <ServiceError err={error} />
        </>
      ) : (
        <>
          <div className="flex flex-row justify-between items-center">
            <h2 className="font-display text-2xl font-bold mb-4 text-[#241F1A]">
              Posts
            </h2>

            {String(userId) === String(currentUser) && (
              <CreatePost
                onPostCreated={(res) => {
                  console.log(res)
                  setPosts(prev => [res, ...prev])
                }}
              />
            )}
          </div>
          {pageLoading ? (
            <PageLoader />
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 pt-2">
              {posts && posts.length > 0 ? (
                posts.map((post, index) => (
                  <PostModal
                    postId={post.id}
                    trigger={
                      <Card
                        key={post.id}
                        className={`
                    relative
                    flex
                    flex-col
                    border
                    border-[#C9A063]
                    bg-[#F3EBD9]
                    rounded-sm
                    shadow-md
                    hover:shadow-xl
                    transition
                    group
                    p-0
                    ${CARD_ROTATIONS[index % CARD_ROTATIONS.length]}
                    hover:rotate-0
                    hover:-translate-y-1
                    duration-300
                  `}
                      >
                        <Pin
                          className="
                      absolute
                      -top-2
                      left-1/2
                      -translate-x-1/2
                      w-5
                      h-5
                      z-20
                      drop-shadow-md
                      rotate-[-10deg]
                    "
                          style={{ color: "#D9A441" }}
                          fill="#D9A441"
                        />

                        {post.image ? (
                          <div className="relative h-60 w-full overflow-hidden">
                            <img
                              src={post.image}
                              className="
                                absolute
                                top-0
                                left-0
                                w-full
                                h-[120%]
                                object-cover
                                transition-transform
                                duration-700
                                ease-in-out
                                group-hover:translate-y-[-15%]
                              "
                            />
                          </div>
                        ) : (
                          <div className="w-full h-60 flex items-center justify-center text-[#8A7F6C] text-sm bg-[#DDD0B0]">
                            No image
                          </div>
                        )}
                        <CardContent className="p-4 flex flex-col h-full">
                          <div>
                            <p className="text-md text-[#241F1A]">
                              {post.desc.length > 100
                                ? post.desc.substring(0, 100) + "..."
                                : post.desc}
                            </p>
                          </div>

                          <div className="flex items-center justify-between mt-auto pt-4">
                            {post.user_id?.toString() ===
                            useProfileStore.getState().profile?.id ? (
                              <DropdownMenu>
                                <DropdownMenuTrigger asChild>
                                  <button
                                    className="text-[#8A7F6C] hover:text-[#B23A2E] transition"
                                    onClick={(e) => e.stopPropagation()}
                                  >
                                    <MoreHorizontal className="w-4 h-4" />
                                  </button>
                                </DropdownMenuTrigger>

                                    <DropdownMenuContent
                                      align="start"
                                      onClick={(e) => e.stopPropagation()}
                                      className="
                                        bg-[#F3EBD9]
                                        border-[#C9A063]
                                        rounded-sm
                                      "
                                    >
                                    <DropdownMenuItem
                                      onSelect={(e) => {
                                        e.preventDefault();
                                        e.stopPropagation();
                                        setUpdatePostId(post.id);
                                        setUpdateDialogOpen(true);
                                      }}
                                      className="
                                        text-[#241F1A]
                                        focus:bg-[#DDD0B0]
                                        focus:text-[#241F1A]
                                        rounded-sm
                                        cursor-pointer
                                      "
                                    >
                                      Update
                                    </DropdownMenuItem>

                                  <DropdownMenuItem
                                    onSelect={(e) => {
                                      e.preventDefault();
                                      e.stopPropagation();

                                      deletePostById(post.id)
                                        .then((response) => {
                                          if (response.status === 204) {
                                            setPosts((prev) =>
                                              prev?.filter(
                                                (p) => p.id !== post.id
                                              )
                                            );

                                            toast.success(
                                              "Successfully deleted post!"
                                            );
                                          } else {
                                            toast.warning(
                                              "Unexpected server response. Please try again"
                                            );
                                          }
                                        })
                                        .catch((err) =>
                                          toast.error(
                                            (err as AxiosError).message
                                          )
                                        );
                                    }}
                                    className="
                                    text-[#B23A2E]
                                    focus:bg-[#B23A2E]
                                    focus:text-[#F3EBD9]
                                    rounded-sm
                                    cursor-pointer
                                  "
                                  >
                                    Delete
                                  </DropdownMenuItem>
                                </DropdownMenuContent>
                              </DropdownMenu>
                            ) : (
                              <div />
                            )}
                            <p className="text-xs text-[#8A7F6C]">
                              {new Date(post.created_at).toDateString()}
                            </p>
                          </div>
                        </CardContent>
                      </Card>
                    }
                  />
                ))
              ) : (
                <div>
                  <p className="font-hand text-lg text-[#8A7F6C]">
                    📌 nothing pinned here yet
                  </p>
                </div>
              )}
            </div>
          )}
          <UpdatePost
            postId={updatePostId}
            open={updateDialogOpen}
            onOpenChange={(open) => {
              setUpdateDialogOpen(open);

              if (!open) {
                setUpdatePostId(null);
              }
            }}
          />
          {postPage && postPage.next != null && (
            <div className="mt-4">
              {loading ? (
                <p className="text-center font-semibold text-sm text-[#8A7F6C]">
                  🔄 Loading...
                </p>
              ) : (
                <p
                  className="text-center font-semibold text-sm hover:underline cursor-pointer text-[#B23A2E]"
                  onClick={handleLoadPosts}
                >
                  🚀 Load More
                </p>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
};
