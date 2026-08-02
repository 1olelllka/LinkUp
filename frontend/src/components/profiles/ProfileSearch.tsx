import { useState } from "react";
import { Input } from "@/components/ui/input";
import { useSearch } from "@/hooks/useSearch";
import { ProfileList } from "./ProfileList";
import { useSearchParams } from "react-router";
import { ProfilePagination } from "./ProfilePagination";
import { ServiceError } from "../errors/ServiceUnavailable";
import { PageLoader } from "../load/PageLoader";
import { Pin } from "lucide-react";

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
    <div className="bg-[#E8DFC8] border border-[#C9A063] rounded-sm shadow-lg p-6 min-h-[calc(100vh-48px)] transition-all w-[99%]">
      <div className="max-w-4xl">
        <div className="flex items-start gap-3 mb-6">
          <Pin
            className="w-5 h-5 mt-1 rotate-[-12deg] drop-shadow"
            style={{ color: "#D9A441" }}
            fill="#D9A441"
          />
          <div>
            <h1 className="font-display text-2xl font-bold text-[#241F1A]">Search Profiles</h1>
            <p className="font-hand text-lg text-[#8A7F6C] mt-0.5">find someone in your circle</p>
          </div>
        </div>
        <Input
          placeholder="Search by name or username..."
          value={searchTerm}
          onChange={handleChange}
          className="mb-6 bg-[#F3EBD9] border-[#C9A063] text-[#241F1A] placeholder:text-[#8A7F6C] rounded-sm focus-visible:ring-[#D9A441] focus-visible:border-[#B23A2E] w-full sm:w-[75%]"
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