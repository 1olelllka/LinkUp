import { useParams } from "react-router";
import { useFollowList } from "@/hooks/useFollowList";
import { ProfileList } from "./ProfileList";
import { ProfilePagination } from "./ProfilePagination";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";

interface FollowListProp {
  type: "follower" | "followee"
}

export function FollowList({type} : FollowListProp) {
  const { userId } = useParams();
  const {followListPage, error, loading} = useFollowList({
    userId: userId,
    type: type
  })

  return (
    <div className="bg-slate-50 rounded-2xl shadow-lg p-6 min-h-[calc(100vh-48px)] transition-all w-[99%]">
      <div className="max-w-4xl">
        <h1 className="text-3xl font-bold mb-6">{type === "followee" ? "Followees" : "Followers"}</h1>
        {error
        ? <ServiceError err={error} /> 
        : 
        <>
          {loading
          ? <PageLoader />
          : 
          <>
            {followListPage && 
            <>
              <ProfileList profileList={followListPage} />
              <ProfilePagination pageOptions={followListPage} />
            </>
            }
          </>
          }
        </>
        }
      </div>
    </div>
  );
}
