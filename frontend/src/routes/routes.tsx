import { createBrowserRouter } from "react-router";
import { FeedPage } from "@/pages/FeedPage";
import { LoginPage } from "@/pages/LoginPage";
import { SignUpPage } from "@/pages/SignUpPage";
import { ChatsPage } from "@/pages/ChatsPage";
import { ProfileSearchPage } from "@/pages/ProfileSearchPage";
import { ProfilePage } from "@/pages/ProfilePage";
import { FollowersPage } from "@/pages/FollowersPage";
import { ProfileDetailPage } from "@/pages/ProfileDetailPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <div>Hello World</div>,
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
    element: <FeedPage />,
  },
  {
    path: "/chats",
    element: <ChatsPage />,
  },
  {
    path: "/search",
    element: <ProfileSearchPage />,
  },
  {
    path: "/profile",
    element: <ProfilePage />,
  },
  {
    path: "/profile/:userId",
    element: <ProfileDetailPage />
  },
  {
    path: "/profile/:userId/followers",
    element: <FollowersPage type="follower"/>
  },
  {
    path: "/profile/:userId/followees",
    element: <FollowersPage type="followee"/>
  }
]);
