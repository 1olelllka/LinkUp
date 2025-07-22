import { type UserList } from "./UserList";

export type UserSearchResult = {
    content: UserList[];
    last: boolean;
    first: boolean;
    totalPages: number;
}