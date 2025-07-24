import { Feed } from "@/components/feed/Feed";
import { FeedLayout } from "@/layouts/FeedLayout";
import { useProfileStore } from "@/store/useProfileStore";

export const FeedPage = () => (
  <FeedLayout>
    <h2 className="text-2xl font-bold mb-4">Feeds</h2>
    <Feed userId={useProfileStore.getState().profile?.userId || ""}/>
  </FeedLayout>
);