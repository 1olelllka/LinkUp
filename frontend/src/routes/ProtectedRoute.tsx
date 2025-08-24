import { ForbiddenPage } from "@/pages/ForbiddenPage";
import { useAuthStore } from "@/store/useAuthStore"


export const ProtectedRoute = ({children} : {children: React.ReactNode}) => {
    const isAuthenticated = useAuthStore.getState().token != null;

    if (!isAuthenticated) {
        return <ForbiddenPage />
    }
    return <>{children}</>;
}