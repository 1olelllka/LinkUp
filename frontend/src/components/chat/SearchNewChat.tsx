import { useEffect } from "react";
import { Input } from "@/components/ui/input";
import { useSearch } from "@/hooks/useSearch";
import { useProfileStore } from "@/store/useProfileStore";
import { useSearchParams } from "react-router";
import { PageLoader } from "../load/PageLoader";
import { getChatByTwoUsers } from "@/services/chatServices";
import type { AxiosError } from "axios";
import { toast } from "sonner";
import { CustomAvatar } from "../profiles/CustomAvatar";
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

  useEffect(() => {
    if (searchTerm !== initialSearch) {
      setSearchTerm(initialSearch);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [initialSearch]);

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
      <div className="mb-3">
        <Input
          placeholder="Find user..."
          onChange={handleChange}
          value={searchTerm}
          className="bg-[#F3EBD9] border-[#C9A063] text-[#241F1A] placeholder:text-[#8A7F6C] rounded-sm focus-visible:ring-[#D9A441] focus-visible:border-[#B23A2E]"
        />
      </div>

      {searchTerm.length > 0 && (
        <div className="space-y-2">
          {loading && <PageLoader />}
          {searchResult.content.map((res) => {
            if (res.id === useProfileStore.getState().profile?.id) return null;

            const isActive = selectedChat?.id === res.id;

            return (
              <div
                key={res.id}
                onClick={() =>
                  getChatByTwoUsers(useProfileStore.getState().profile?.id, res.id)
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
                className={`p-3 rounded-sm cursor-pointer transition flex items-center gap-3 border ${
                  isActive
                    ? "bg-[#DDD0B0] border-[#B23A2E]"
                    : "bg-[#F3EBD9] border-[#C9A063] hover:bg-[#DDD0B0]"
                }`}
              >
                <CustomAvatar name={res.name} photo={res.photo} size={36} />
                <h4 className="font-display font-semibold text-sm text-[#241F1A]">
                  {res.name} <span className="font-normal text-[#8A7F6C]">@{res.username}</span>
                </h4>
              </div>
            );
          })}

          {searchResult.content.length === 0 && !loading && (
            <p className="font-hand text-lg text-[#8A7F6C] text-center py-4">no one matches that</p>
          )}

          {(!searchResult.first || !searchResult.last) && (
            <Pagination>
              <PaginationContent>
                {!searchResult.first &&
                  <PaginationPrevious
                    className="cursor-pointer border border-[#C9A063] text-[#241F1A] hover:bg-[#DDD0B0] rounded-sm"
                    onClick={(e) => {
                      e.preventDefault();
                      goToPage(currentPage - 1)
                    }}/>}
                {!searchResult.last &&
                  <PaginationNext
                    className="cursor-pointer border border-[#C9A063] text-[#241F1A] hover:bg-[#DDD0B0] rounded-sm"
                    onClick={(e) => {
                      e.preventDefault();
                      goToPage(currentPage + 1)
                    }}/>}
              </PaginationContent>
            </Pagination>
          )}
        </div>
      )}
    </>
  );
};