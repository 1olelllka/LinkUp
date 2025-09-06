
import {useState, useEffect } from "react"
import { useDebounce } from "./useDebounce";
import { searchProfile } from "@/services/profileServices";
import type { ProfilePage } from "@/types/Profile";
import { useSearchParams } from "react-router";
import { AxiosError } from "axios";

export const useSearch = (searchTerm: string) => {
    const debouncedTerm = useDebounce(searchTerm, 500);
    const [searchResult, setSearchResult] = useState<ProfilePage>({
        content: [],
        first: true,
        last: true,
        totalPages: 1,
        totalElements: 0,
        pageable: {
            pageNumber: 0
        }
    });
    const searchParams = useSearchParams();
    const pageNumber = searchParams[0].get('page');
    const [error, setError] = useState<AxiosError>();
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (debouncedTerm.trim() == "") {
            setSearchResult({ content: [], first: true, last: true, totalPages: 1, totalElements: 0, pageable: {pageNumber: 0} });
            return;
        }
        setLoading(true);
        searchProfile(debouncedTerm, pageNumber)
        .then(setSearchResult)
        .catch(err => setError(err as AxiosError))
        .finally(() => setLoading(false));
    }, [debouncedTerm, pageNumber]);

    return {searchResult, error, loading};
}