import type { UserSearchResult } from "@/types/UserSearchResult";
import {useState, useEffect } from "react"
import { useDebounce } from "./useDebounce";
import { searchProfile } from "@/services/profileServices";

export const useSearch = (searchTerm: string) => {
    const debouncedTerm = useDebounce(searchTerm, 500);
    const [searchResult, setSearchResult] = useState<UserSearchResult>({
        content: [],
        first: true,
        last: true,
        totalPages: 1,
    });

    useEffect(() => {
        if (debouncedTerm.trim() == "") {
            setSearchResult({ content: [], first: true, last: true, totalPages: 1 });
            return;
        }
        searchProfile(debouncedTerm)
        .then(setSearchResult)
        .catch(err => console.log(err));
    }, [debouncedTerm]);

    return searchResult;
}