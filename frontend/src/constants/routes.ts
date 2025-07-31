
export const API_BASE = "http://localhost:8080/api";

export const API_ROUTES = {
    auth: {
        login: `${API_BASE}/auth/login`,
        signup: `${API_BASE}/auth/register`,
        me: `/auth/me`
    },
    profile: {
        search: `${API_BASE}/profiles?search=`,
        profileDetail: `/profiles/`,
        profileFollowers: (userId: string, page: number = 0) => `${API_BASE}/profiles/${userId}/followers?page=${page}`,
        profileFollowees: (userId: string, page: number = 0) => `${API_BASE}/profiles/${userId}/followees?page=${page}`,
        followStatus: (from: string, to: string) => `${API_BASE}/profiles/follow-status?from=${from}&to=${to}`,
        follow: `/profiles/follow`,
        unfollow: `/profiles/unfollow`
    },
    chats: {
        list: "/chats/users/"
    },
    messages: {
        list: "/chats/"
    },
    feed: {
        list: "/feeds"
    },
    posts: {
        list: `${API_BASE}/posts/users/`,
        detail: (id: number) => `${API_BASE}/posts/${id}`
    }
}