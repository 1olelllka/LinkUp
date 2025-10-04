import { createBrowserRouter } from "react-router";
import { FeedPage } from "@/pages/FeedPage";
import { LoginPage } from "@/pages/LoginPage";
import { SignUpPage } from "@/pages/SignUpPage";
import { ChatsPage } from "@/pages/ChatsPage";
import { ProfileSearchPage } from "@/pages/ProfileSearchPage";
import { ProfilePage } from "@/pages/ProfilePage";
import { FollowersPage } from "@/pages/FollowersPage";
import { ProfileDetailPage } from "@/pages/ProfileDetailPage";
import { StoryArchivePage } from "@/pages/StoryArchivePage";
import { CreatePostPage } from "@/pages/CreatePostPage";
import { UpdatePostPage } from "@/pages/UpdatePostPage";
import { NotFoundPage } from "@/pages/NotFoundPage";
import { ProtectedRoute } from "./ProtectedRoute";
import { LinkUpLanding } from "@/pages/LinkUpLanding";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <LinkUpLanding />
  },
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/signup",
    element: <SignUpPage />,
  },
  {
    path: "/feeds",
    element: 
    <ProtectedRoute children={
      <FeedPage />
    } />,
  },
  {
    path: "/chats",
    element: <ProtectedRoute children={<ChatsPage />} />
  },
  {
    path: "/search",
    element: <ProtectedRoute children={<ProfileSearchPage />} />,
  },
  {
    path: "/profile",
    element:<ProtectedRoute children={<ProfilePage />} />,
  },
  {
    path: "/profile/:userId",
    element: <ProtectedRoute children={<ProfileDetailPage />} />
  },
  {
    path: "/profile/:userId/followers",
    element: <ProtectedRoute children={<FollowersPage type="follower"/>} />
  },
  {
    path: "/profile/:userId/followees",
    element: <ProtectedRoute children={<FollowersPage type="followee"/>} />
  },
  {
    path: "/archive",
    element: <ProtectedRoute children={<StoryArchivePage />} />
  },
  {
    path: "/create-post",
    element: <ProtectedRoute children={<CreatePostPage />} />
  },
  {
    path: "/update-post/:postId",
    element: <ProtectedRoute children={<UpdatePostPage />} />
  },
  {
    path: "*",
    element: <NotFoundPage />
  }
]);
