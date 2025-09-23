import { Card, CardContent } from "@/components/ui/card";
import { useUserPosts } from "@/hooks/useUserPosts";
import { PostModal } from "./PostModal";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { MoreHorizontal, Plus } from "lucide-react";
import { useProfileStore } from "@/store/useProfileStore";
import { deletePostById } from "@/services/postServices";
import { useCallback } from "react";
import { Button } from "../ui/button";
import { useNavigate } from "react-router";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";
import { toast } from "sonner";
import type { AxiosError } from "axios";

export const UserPosts = ({ userId } : {userId : string | undefined}) => {
  const {posts, setPosts, postPage, loadMorePosts, loading, setLoading, pageLoading, error} = useUserPosts(userId);
  const currentUser = useProfileStore.getState().profile?.userId;
  const navigate = useNavigate();

  const handleLoadPosts = useCallback(async () => {
    await loadMorePosts();
  }, [loadMorePosts])

  return (
    <div className="mt-5 bg-slate-50 p-6 rounded-xl shadow-lg transition-all w-[99%]">
      {error
      ? 
      <>
        <h2 className="text-2xl font-bold mb-4">Posts</h2>
        <ServiceError err={error} />
      </>
      : 
      <>
        <div className="flex flex-row justify-between">
          <h2 className="text-2xl font-bold mb-4">Posts</h2>
          {userId == currentUser && 
            <Button 
            variant={"outline"} 
            size={"icon"} 
            className="cursor-pointer"
            onClick={() => navigate('/create-post')}>
              <Plus />
            </Button>
          }
        </div>
          {pageLoading 
            ? <PageLoader />
            : 
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {posts && posts.length > 0 
            ? posts.map((post) => (
              <PostModal postId={post.id} trigger={
                <Card
                  key={post.id}
                  className="flex flex-col border border-gray-200 rounded-xl shadow hover:shadow-lg transition overflow-hidden group p-0"
                >
                  {post.image 
                  ? (
                    <div className="relative h-60 w-full overflow-hidden rounded-t-xl">
                      <img
                        src={post.image}
                        className="absolute top-0 left-0 w-full h-[120%] object-cover transition-transform duration-700 ease-in-out group-hover:translate-y-[-15%] rounded-t-xl"
                      />
                    </div>
                  ) : (
                  <div className="w-full h-60 flex items-center justify-center text-gray-400 text-sm bg-gray-100">
                    No image
                  </div>
                )}

                  <CardContent className="p-4 flex flex-col h-full">
                  <div>
                    <p className="text-md text-gray-800">
                      {post.desc.length > 100 
                        ? post.desc.substring(0, 100) + "..." 
                        : post.desc}
                    </p>
                  </div>

                    <div className="flex items-center justify-between mt-auto pt-4">
                    {post.user_id?.toString() === useProfileStore.getState().profile?.userId ? (
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <button className="text-gray-500 hover:text-red-600 transition">
                            <MoreHorizontal className="w-4 h-4" />
                          </button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="start">
                          <DropdownMenuItem
                            onClick={() => navigate(`/update-post/${post.id}`)}>
                            Update
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            onClick={async (e) => {
                              e.stopPropagation();
                              e.preventDefault();
                              deletePostById(post.id).then(response => {
                                if (response.status == 204) {
                                  setLoading(true);
                                  setPosts((prev) => prev?.filter(p => p.id != post.id));
                                  setLoading(false);
                                  toast.success("Successfully deleted post!");
                                } else {
                                  // console.log("Unexpected status code --> " + response);
                                  toast.warning("Unexpected server response. Please try again");
                                }
                              }).catch(err => toast.error((err as AxiosError).message));
                            }}
                            className="text-red-600 focus:bg-red-50 focus:text-red-700"
                          >
                            Delete
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    ) : (
                      <div />
                    )}
                    <p className="text-xs text-gray-400">{new Date(post.created_at).toDateString()}</p>
                    </div>
                  </CardContent>
                </Card>
              } />
            ))
            : <div>
                <p className="font-semibold text-slate-400">📭 Nothing to see here</p>
              </div>}
              </div>
          }
        {postPage && postPage.next != null && 
          <div className="mt-2">
            {loading 
            ? <p 
            className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400">
              🔄 Loading...</p>
            : <p 
            className="text-center font-semibold text-sm hover:underline cursor-pointer text-slate-400"
            onClick={handleLoadPosts}
            >🚀 Load More</p>
            }
          </div>
        }
      </>
      }
    </div>
  );
};