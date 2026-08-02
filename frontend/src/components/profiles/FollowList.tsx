import { useParams } from "react-router";
import { useFollowList } from "@/hooks/useFollowList";
import { ProfileList } from "./ProfileList";
import { ProfilePagination } from "./ProfilePagination";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";
import { Pin } from "lucide-react";

interface FollowListProp {
  type: "follower" | "followee"
}

export function FollowList({type} : FollowListProp) {
  const { userId } = useParams();
  const {followListPage, error, loading} = useFollowList({
    userId: userId,
    type: type
  })

  const title = type === "followee" ? "Following" : "Followers";
  const subtitle = type === "followee" ? "people they follow" : "people who follow them";

  return (
    <div className="bg-[#E8DFC8] border border-[#C9A063] rounded-sm shadow-lg p-6 min-h-[calc(100vh-48px)] transition-all w-[99%]">
      <div className="max-w-4xl">
        <div className="flex items-start gap-3 mb-6">
          <Pin
            className="w-5 h-5 mt-1 rotate-[-12deg] drop-shadow"
            style={{ color: "#D9A441" }}
            fill="#D9A441"
          />
          <div>
            <h1 className="font-display text-2xl font-bold text-[#241F1A]">{title}</h1>
            <p className="font-hand text-lg text-[#8A7F6C] mt-0.5">{subtitle}</p>
          </div>
        </div>
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