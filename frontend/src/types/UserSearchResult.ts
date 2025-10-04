import type { UserList } from "./Profile";

export type UserSearchResult = {
    content: UserList[];
    last: boolean;
    first: boolean;
    totalPages: number;
}