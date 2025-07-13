import './App.css'
import { FeedPage } from './pages/FeedPage';
import { LoginPage } from './pages/LoginPage'
import { SignUpPage } from './pages/SignUpPage'
import {
  createBrowserRouter,
  RouterProvider,
} from "react-router";

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
