import { ForbiddenPage } from "@/pages/ForbiddenPage";
import { ensureAccessToken } from "@/utils/ensureAccessToken";
import { useEffect, useState } from "react";


export const ProtectedRoute = ({children} : {children: React.ReactNode}) => {
    const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);

    useEffect(() => {
        let mounted = true;
        (async () => {
        try {
            const token = await ensureAccessToken();
            if (mounted) {
            setIsAuthenticated(!!token);
            }
        } catch {
            if (mounted) {
            setIsAuthenticated(false);
            }
        }
        })();
        return () => {
        mounted = false;
        };
    })

    if (!isAuthenticated) {
        return <ForbiddenPage />
    }
    return <>{children}</>;
}