import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination"
import type { ProfilePage } from "@/types/Profile";
import { useSearchParams } from "react-router";

export const ProfilePagination = (data: {pageOptions: ProfilePage}) => {

  const [searchParams, setSearchParams] = useSearchParams();

  const goToPage = (page: number) => {
    searchParams.set("page", String(page));
    setSearchParams(searchParams);
  };

  const currentPage = data.pageOptions.pageable.pageNumber + 1;

  return (
    data.pageOptions.totalElements > 0 && (
      <Pagination>
        <PaginationContent>
          {!data.pageOptions.first && 
            <PaginationItem>
              <PaginationPrevious
                className="border border-[#C9A063] text-[#241F1A] hover:bg-[#DDD0B0] rounded-sm"
                onClick={(e) => {
                  e.preventDefault()
                  goToPage(currentPage - 1)
                }} />
            </PaginationItem>
          }
          {currentPage > 1 && 
            <PaginationItem>
              <PaginationLink
                className="border border-[#C9A063] text-[#241F1A] hover:bg-[#DDD0B0] rounded-sm"
                onClick={(e) => {
                  e.preventDefault()
                  goToPage(currentPage - 1)
                }}>{currentPage - 1}</PaginationLink>
            </PaginationItem>
          }
          <PaginationItem>
            <PaginationLink 
              className="bg-[#B23A2E] text-[#F3EBD9] border border-[#B23A2E] hover:bg-[#9c3226] rounded-sm"
              onClick={(e) => {
                e.preventDefault()
                goToPage(currentPage)
              }}
            >{currentPage}</PaginationLink>
          </PaginationItem>
          {currentPage < data.pageOptions.totalPages &&
            <PaginationItem>
              <PaginationLink
                className="border border-[#C9A063] text-[#241F1A] hover:bg-[#DDD0B0] rounded-sm"
                onClick={(e) => {
                  e.preventDefault()
                  goToPage(currentPage + 1)
                }}>{currentPage + 1}</PaginationLink>
            </PaginationItem>
          }
          {!data.pageOptions.last && 
            <PaginationItem>
              <PaginationNext
                className="border border-[#C9A063] text-[#241F1A] hover:bg-[#DDD0B0] rounded-sm"
                onClick={(e) => {
                  e.preventDefault()
                  goToPage(currentPage + 1)
                }} />
            </PaginationItem>
          }
        </PaginationContent>
      </Pagination>
    )
  );
}