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
                            <PaginationPrevious onClick={(e) => {
                                e.preventDefault()
                                goToPage(currentPage - 1)
                            }} />
                        </PaginationItem>
                    }
                    {currentPage > 1 && 
                        <PaginationItem>
                            <PaginationLink onClick={(e) => {
                                e.preventDefault()
                                goToPage(currentPage - 1)
                            }}>{currentPage - 1}</PaginationLink>
                        </PaginationItem>
                    }
                    <PaginationItem>
                        <PaginationLink 
                        className="bg-gray-100"
                        onClick={(e) => {
                            e.preventDefault()
                            goToPage(currentPage)
                        }}
                        >{currentPage}</PaginationLink>
                    </PaginationItem>
                    {currentPage < data.pageOptions.totalPages &&
                        <PaginationItem>
                            <PaginationLink onClick={(e) => {
                                e.preventDefault()
                                goToPage(currentPage + 1)
                            }}>{currentPage + 1}</PaginationLink>
                        </PaginationItem>
                    }
                    {!data.pageOptions.last && 
                        <PaginationItem>
                            <PaginationNext onClick={(e) => {
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