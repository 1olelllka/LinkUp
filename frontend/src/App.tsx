import './App.css'
import {
  RouterProvider,
} from "react-router";
import { router } from './routes/routes';
import { Toaster } from 'sonner';
import { useHealthStore } from './store/useHealthStore';
import { ServerErrorPage } from './pages/ServerErrorPage';
import { useEffect } from 'react';
import { checkGatewayHealthStatus } from './services/gateway';

function App() {

  const {down, setDown} = useHealthStore();

  useEffect(() => {
    const check = async () => {
      try {
        const res = await checkGatewayHealthStatus();
        if (res.data.status != 'UP') {
          setDown(true);
        }
      } catch (err) {
        console.log(err);
        setDown(true);
      }
    }
    check();
    const interval = setInterval(check, 10000);
    return () => clearInterval(interval);
  }, [setDown])

  return (
      <>
        {down 
          ? <ServerErrorPage />
          : <RouterProvider router={router} />
        }
        <Toaster />
      </>
  )
}

export default App
