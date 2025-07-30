import { useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useSearch } from "@/hooks/useSearch";
import { CustomAvatar } from "./CustomAvatar";


export const ProfileSearch = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const searchResult = useSearch(searchTerm);
  return (
    <div className="max-w-4xl mx-auto">
      <h1 className="text-3xl font-semibold mb-6">Search Profiles</h1>

      <Input
        placeholder="Search by name or username..."
        value={searchTerm}
        onChange={(e) => setSearchTerm(e.target.value)}
        className="mb-6 bg-white border border-gray-300"
      />

      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
        {searchResult?.content.length > 0 ? (
          searchResult?.content.map((profile) => (
            <div
              key={profile.id}
              className="bg-white p-4 rounded-xl shadow hover:shadow-md transition border border-gray-200"
            >
              <div className="flex items-center space-x-4 mb-4">
                <CustomAvatar name={profile.name} photo={profile.photo} size={50} />
                <div>
                  <h3 className="font-semibold text-lg">{profile.name}</h3>
                  <p className="text-sm text-gray-500">@{profile.username}</p>
                </div>
              </div>
              <Button className="w-full mt-2" variant="outline">
                View Profile
              </Button>
            </div>
          ))
        ) : (
          <p className="text-gray-500">No profiles found.</p>
        )}
      </div>
    </div>
  );
};
