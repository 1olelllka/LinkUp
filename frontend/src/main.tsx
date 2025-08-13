import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { NotificationWSProvider } from './layouts/NotificationWSProvider.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <NotificationWSProvider>
      <App />
    </NotificationWSProvider>
  </StrictMode>,
)
