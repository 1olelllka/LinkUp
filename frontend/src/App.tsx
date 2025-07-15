import './App.css'
import { FeedPage } from './pages/FeedPage';
import { LoginPage } from './pages/LoginPage'
import { SignUpPage } from './pages/SignUpPage'
import {
  createBrowserRouter,
  RouterProvider,
} from "react-router";
import { ChatsPage } from './pages/ChatsPage';
import { ProfileSearchPage } from './pages/ProfileSearchPage';

const router = createBrowserRouter([
  {
    path: "/",
    element: <div>Hello World</div>,
  },
  {
    path: "/login",
    element: <LoginPage />
  },
  {
    path: "/signup",
    element: <SignUpPage />
  },
  {
    path: "/feeds",
    element: <FeedPage />
  },
  {
    path: "/chats",
    element: <ChatsPage />
  },
  {
    path: "/search",
    element: <ProfileSearchPage />
  }
]);


function App() {

  return (
    <>
      <RouterProvider router={router} />
    </>
  )
}

export default App
