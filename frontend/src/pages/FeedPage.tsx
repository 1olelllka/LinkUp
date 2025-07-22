import { Feed } from "@/components/feed/Feed";
import { FeedLayout } from "@/layouts/FeedLayout";

export const FeedPage = () => (
  <FeedLayout>
    <h2 className="text-2xl font-bold mb-4">Feeds</h2>
    <Feed />
  </FeedLayout>
);