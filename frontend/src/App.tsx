import './App.css'
import {
  RouterProvider,
} from "react-router";
import { router } from './routes/routes';
import { Toaster } from 'sonner';
import { useHealthStore } from './store/useHealthStore';
import { ServerErrorPage } from './pages/ServerErrorPage';
import { useEffect, useState } from 'react';
import { checkGatewayHealthStatus } from './services/gateway';
import { checkAuthHealth } from './services/authServices';
import { checkProfileHealth } from './services/profileServices';
import { TooManyRequestsPage } from './pages/TooManyRequestsPage';
import type { AxiosError } from 'axios';

function App() {

  const {down, setDown} = useHealthStore();
  const [tooMany, setTooMany] = useState(false);

  useEffect(() => {
    const check = async () => {
      try {
        const gateway = await checkGatewayHealthStatus();
        const auth = await checkAuthHealth();
        const profile = await checkProfileHealth();
        if (gateway.data.status != 'UP' || auth.data.status != "UP" || profile.data.status != 'UP') {
          setDown(true);
          setTooMany(false);
        } else if (gateway.data.status == 'UP' && auth.data.status == 'UP' && profile.data.status == 'UP') {
          setDown(false);
        }
      } catch (err) {
        const error = err as AxiosError;
        if (error.status == 429) {
          setTooMany(true);
        } else {
          setTooMany(false)
        }
        setDown(true);
      }
    }
    check();
    const interval = setInterval(check, 20000);
    return () => clearInterval(interval);
  }, [setDown])

  return (
      <>
        {down 
          ? (tooMany ? <TooManyRequestsPage /> : <ServerErrorPage />)
          : <RouterProvider router={router} />
        }
        <Toaster />
      </>
  )
}

export default App
