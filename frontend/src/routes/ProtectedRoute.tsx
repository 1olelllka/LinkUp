import { ForbiddenPage } from "@/pages/ForbiddenPage";
import { ensureAccessToken } from "@/utils/ensureAccessToken";


export const ProtectedRoute = ({children} : {children: React.ReactNode}) => {
    const isAuthenticated = ensureAccessToken();

    if (!isAuthenticated) {

        return <ForbiddenPage />
    }
    return <>{children}</>;
}