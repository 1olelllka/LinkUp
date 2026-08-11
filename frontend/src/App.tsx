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
      <Toaster
        position="bottom-right"
        toastOptions={{
          unstyled: true,
          classNames: {
            toast:
              "flex items-start gap-3 bg-[#E8DFC8] border border-[#C9A063] text-[#241F1A] rounded-sm shadow-lg p-4",
            title: "font-display font-bold text-[#241F1A]",
            description: "text-[#4A4136] text-sm",
            actionButton: "bg-[#B23A2E] text-[#F3EBD9] rounded-sm px-2 py-1 text-xs",
            cancelButton: "bg-[#DDD0B0] text-[#241F1A] rounded-sm px-2 py-1 text-xs",
            closeButton: "bg-[#F3EBD9] border-[#C9A063] text-[#8A7F6C]",
            success: "border-l-4 border-l-[#6B7A5E]",
            error: "border-l-4 border-l-[#B23A2E]",
            warning: "border-l-4 border-l-[#D9A441]",
          },
        }}
      />
      </>
  )
}

export default App
