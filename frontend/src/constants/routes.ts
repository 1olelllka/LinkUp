
export const API_BASE = "http://localhost:8080/api";

export const API_ROUTES = {
    auth: {
        login: `${API_BASE}/auth/login`,
        signup: `${API_BASE}/auth/register`,
        me: `/auth/me`
    },
    profile: {
        search: `${API_BASE}/profiles?search=`,
        profileDetail: `/profiles/`
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
        list: `${API_BASE}/posts/users/`
    }
}