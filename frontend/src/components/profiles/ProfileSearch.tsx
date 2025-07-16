import { useEffect, useState } from "react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import axios from "axios";
import { useDebounce } from "@/lib/useDebounce";

type User = {
    id: string,
    name: string,
    username: string
}

type SearchResult = {
    content: User[];
    last: boolean;
    first: boolean;
    totalPages: number;
}

export const ProfileSearch = () => {
  const [searchTerm, setSearchTerm] = useState("");
  const debouncedTerm = useDebounce(searchTerm, 500);
  const [searchResult, setSearchResult] = useState<SearchResult>({
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
    axios.get(`http://localhost:8080/api/profiles?search=${debouncedTerm}`).then((response) => {
        setSearchResult(response.data);
    }).catch(err => console.log(err));
  }, [debouncedTerm])

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
                {/* TODO: elither this or profile pic */}
                <div className="w-12 h-12 bg-gray-300 rounded-full flex items-center justify-center text-white font-bold">
                  {profile.name.charAt(0).toUpperCase()}
                </div>
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
