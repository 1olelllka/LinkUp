import { Card } from "@/components/ui/card";
import { NavLink, useParams } from "react-router";
import { Button } from "@/components/ui/button";
import { CustomAvatar } from "./CustomAvatar";
import { useFollowList } from "@/hooks/useFollowList";

interface FollowListProp {
  type: "follower" | "followee"
}

export function FollowList({type} : FollowListProp) {
  const { userId } = useParams();
  const followerPage = useFollowList({
    userId: userId,
    pageNumber: 0,
    type: type
  })

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <h1 className="text-3xl font-bold mb-6">{type === "followee" ? "Followees" : "Followers"}</h1>

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
        {followerPage?.content.map((f) => (
          <Card
            key={f.id}
            className="p-4 flex flex-col items-center gap-3 text-center shadow-sm"
          >
            <CustomAvatar name={f?.name} photo={f?.photo} size={80} />
            <div>
              <p className="font-semibold text-lg">{f.username}</p>
              <p className="text-sm text-muted-foreground">{f.name}</p>
            </div>
            <NavLink to={`/profile/${f.id}`}>
              <Button variant="secondary" size="sm">
                View
              </Button>
            </NavLink>
          </Card>
        ))}
      </div>
    </div>
  );
}
