import { useState } from "react";
import { Input } from "@/components/ui/input";
import { useSearch } from "@/hooks/useSearch";
import { ProfileList } from "./ProfileList";


export const ProfileSearch = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const searchResult = useSearch(searchTerm);
  
  return (
    <div className="bg-slate-50 rounded-2xl shadow-lg p-6 min-h-[calc(100vh-48px)] transition-all w-[99%]">
      <div className="max-w-4xl">
        <h1 className="text-3xl font-semibold mb-6">Search Profiles</h1>

        <Input
          placeholder="Search by name or username..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="mb-6 bg-white border border-gray-300 w-[75%]"
        />

        <ProfileList profileList={searchResult} />
      </div>
    </div>
  );
};
