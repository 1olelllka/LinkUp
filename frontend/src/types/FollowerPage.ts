import type { UserList } from "./Profile"

type Pageable = {
    pageNumber: number,
}

export type FollowerPage = {
    content: UserList[],
    first: boolean,
    last: boolean,
    totalPages: number,
    totalElements: number,
    pageable: Pageable
}