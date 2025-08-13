import './App.css'
import {
  RouterProvider,
} from "react-router";
import { router } from './routes/routes';
import { Toaster } from 'sonner';

function App() {

  return (
    <>
      <RouterProvider router={router} />
      <Toaster />
    </>
  )
}

export default App
