import { Feed } from "@/components/feed/Feed";
import { FeedLayout } from "@/layouts/FeedLayout";
import { useProfileStore } from "@/store/useProfileStore";

export const FeedPage = () => (
  <FeedLayout>
    <Feed userId={useProfileStore.getState().profile?.userId || ""}/>
  </FeedLayout>
);