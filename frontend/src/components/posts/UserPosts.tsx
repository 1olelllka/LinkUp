import { Card, CardContent } from "@/components/ui/card";
import { useUserPosts } from "@/hooks/useUserPosts";
import { PostModal } from "./PostModal";

export const UserPosts = ({ userId } : {userId : string | undefined}) => {
  const posts = useUserPosts(userId);

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
                <p className="text-xs text-gray-400 text-right mt-auto">
                  {post.created_at}
                </p>
              </CardContent>
            </Card>
          } />
        ))}
      </div>
    </div>
  );
};