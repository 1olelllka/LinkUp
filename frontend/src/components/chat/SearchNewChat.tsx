import { Input } from "@/components/ui/input";
import { useSearch } from "@/hooks/useSearch";
import { useProfileStore } from "@/store/useProfileStore";
import { useSearchParams } from "react-router";

type SelectedChat = {
  id: string;
  selectedReceiverName: string;
  receiverId: string | undefined;
};

export const SearchNewChat = ({
  selectedChat,
  setSelectedChat,
  searchTerm,
  setSearchTerm,
}: {
  selectedChat: SelectedChat | null;
  setSelectedChat: (chat: SelectedChat) => void;
  searchTerm: string;
  setSearchTerm: (searching: string) => void;
}) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const initialSearch = searchParams.get("search") || "";

  if (searchTerm !== initialSearch) {
    setSearchTerm(initialSearch);
  }

  const {searchResult} = useSearch(searchTerm);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    setSearchTerm(value);

    if (value) {
      setSearchParams({ search: value });
    } else {
      searchParams.delete("search");
      setSearchParams(searchParams);
    }
  };

  return (
    <>
      <div className="m-2">
        <Input
          placeholder="Find user..."
          onChange={handleChange}
          value={searchTerm}
        />
      </div>

      <div className="space-y-3">
        {searchResult.content.map((res) => {
          if (res.id === useProfileStore.getState().profile?.userId) return null;

          return (
            <div
              key={res.id}
              onClick={() =>
                setSelectedChat({
                  id: res.id,
                  selectedReceiverName: res.name,
                  receiverId: res.id,
                })
              }
              className={`p-4 rounded-xl cursor-pointer transition flex justify-between items-center ${
                selectedChat?.id === res.id
                  ? "bg-gray-200"
                  : "hover:bg-gray-100"
              }`}
            >
              <div>
                <h4 className="font-semibold">
                  {res.name} (@{res.username})
                </h4>
                <p className="text-sm text-gray-500 truncate w-40">
                  Type new message
                </p>
              </div>
              <div>
                <span className="text-xs text-gray-400">11:53</span>
              </div>
            </div>
          );
        })}
      </div>
    </>
  );
};
