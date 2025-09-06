import { useState } from "react";
import { Input } from "@/components/ui/input";
import { useSearch } from "@/hooks/useSearch";
import { ProfileList } from "./ProfileList";
import { useSearchParams } from "react-router";
import { ProfilePagination } from "./ProfilePagination";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";

export const ProfileSearch = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialSearch = searchParams.get("query") || "";
  const [searchTerm, setSearchTerm] = useState(initialSearch);

  const {searchResult, error, loading} = useSearch(searchTerm);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);

    if (value) {
      setSearchParams({ query: value });
    } else {
      searchParams.delete("query");
      setSearchParams(searchParams);
    }
  };

  return (
    <div className="bg-slate-50 rounded-2xl shadow-lg p-6 min-h-[calc(100vh-48px)] transition-all w-[99%]">
      <div className="max-w-4xl">
        <h1 className="text-3xl font-semibold mb-6">Search Profiles</h1>
        <Input
          placeholder="Search by name or username..."
          value={searchTerm}
          onChange={handleChange}
          className="mb-6 bg-white border border-gray-300 w-[75%]"
        />
        {error
        ? <div className="mt-10">
          <ServiceError err={error} />
          </div>
        : 
        <>
          {loading
          ? <PageLoader />
          : 
          <>
            <ProfileList profileList={searchResult} />
            <ProfilePagination pageOptions={searchResult}/>
          </>
          }
        </>
        }
      </div>
    </div>
  );
};
