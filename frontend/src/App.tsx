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
import { checkAuthHealth } from './services/authServices';
import { checkProfileHealth } from './services/profileServices';

function App() {

  const {down, setDown} = useHealthStore();

  useEffect(() => {
    const check = async () => {
      try {
        const gateway = await checkGatewayHealthStatus();
        const auth = await checkAuthHealth();
        const profile = await checkProfileHealth();
        if (gateway.data.status != 'UP' || auth.data.status != "UP" || profile.data.status != 'UP') {
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
