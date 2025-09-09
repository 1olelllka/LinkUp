import { Input } from "@/components/ui/input";
import { useSearch } from "@/hooks/useSearch";
import { useProfileStore } from "@/store/useProfileStore";
import { useSearchParams } from "react-router";
import { PageLoader } from "../load/PageLoader";
import { getChatByTwoUsers } from "@/services/chatServices";
import type { AxiosError } from "axios";
import { toast } from "sonner";
import {
  Pagination,
  PaginationContent,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"


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

  const {searchResult, loading} = useSearch(searchTerm);

  const goToPage = (page: number) => {
      searchParams.set("page", String(page));
      setSearchParams(searchParams);
  };
  
  const currentPage = searchResult.pageable.pageNumber + 1;

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
        {loading && <PageLoader />}
        {searchResult.content.map((res) => {
          if (res.id === useProfileStore.getState().profile?.userId) return null;

          return (
            <div
              key={res.id}
              onClick={() =>
                getChatByTwoUsers(useProfileStore.getState().profile?.userId, res.id)
                .then(response => {
                  setSelectedChat({
                    id: response.id,
                    selectedReceiverName: res.name,
                    receiverId: res.id
                  })
                }).catch(err => {
                  const error = err as AxiosError;
                  if (error.status == 404) {
                    setSelectedChat({
                      id: res.id,
                      selectedReceiverName: res.name,
                      receiverId: res.id,
                    })
                  } else {
                    toast.error(error.message)
                  }
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
              </div>
            </div>
          );
        })}
        <Pagination>
          <PaginationContent>
            {!searchResult.first && 
            <PaginationPrevious 
            className="cursor-pointer"
            onClick={(e) => {
              e.preventDefault();
              goToPage(currentPage - 1)
            }}/>}
            {!searchResult.last && 
            <PaginationNext 
            className="cursor-pointer"
            onClick={(e) => {
              e.preventDefault();
              goToPage(currentPage + 1)
            }}/>}
          </PaginationContent>
        </Pagination>
      </div>
    </>
  );
};
