import { Card, CardContent } from "@/components/ui/card";
import { useUserPosts } from "@/hooks/useUserPosts";
import { PostModal } from "./PostModal";
import {
  DropdownMenu,
  DropdownMenuTrigger,
  DropdownMenuContent,
  DropdownMenuItem,
} from "@/components/ui/dropdown-menu";
import { MoreHorizontal } from "lucide-react";
import { useProfileStore } from "@/store/useProfileStore";
import { deletePostById } from "@/services/postServices";

export const UserPosts = ({ userId } : {userId : string | undefined}) => {
  const {posts, setPosts} = useUserPosts(userId);

  return (
    <div>
      <h2 className="text-xl font-bold mb-4">Posts</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {posts?.map((post) => (
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
                    alt={post.title}
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
                  <p className="text-md text-gray-800">{post.desc}</p>
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
                        onClick={async () => {
                          deletePostById(post.id).then(response => {
                            if (response.status == 204) {
                              setPosts((prev) => prev?.filter(p => p.id != post.id));
                            } else {
                              console.log("Unexpected status code --> " + response);
                            }
                          }).catch(err => console.log(err));
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
                <p className="text-xs text-gray-400">{post.created_at}</p>
                </div>
              </CardContent>
            </Card>
          } />
        ))}
      </div>
    </div>
  );
};