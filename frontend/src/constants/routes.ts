export const API_BASE = "http://localhost:8080/api";

export const API_ROUTES = {
    auth: {
        login: `${API_BASE}/auth/login`,
        signup: `${API_BASE}/auth/register`,
        me: `/auth/me`,
        logout: '/auth/logout',
        health: `${API_BASE}/auth/actuator/health`
    },
    profile: {
        search: `${API_BASE}/profiles?search=`,
        profileDetail: `/profiles/`,
        profileFollowers: (userId: string, page: number = 0) => `${API_BASE}/profiles/${userId}/followers?page=${page}`,
        profileFollowees: (userId: string, page: number = 0) => `${API_BASE}/profiles/${userId}/followees?page=${page}`,
        followStatus: (from: string, to: string) => `${API_BASE}/profiles/follow-status?from=${from}&to=${to}`,
        follow: `/profiles/follow`,
        unfollow: `/profiles/unfollow`,
        health: `${API_BASE}/profiles/actuator/health`
    },
    chats: {
        list: "/chats/users/",
        detail: (id: string) => `/chats/${id}`
    },
    messages: {
        list: "/chats/",
        details: (id: string) => `/chats/messages/${id}`
    },
    feed: {
        list: "/feeds"
    },
    posts: {
        list: `${API_BASE}/posts/users/`,
        detail: (id: number) => `${API_BASE}/posts/${id}`
    },
    comments: {
        create: (post: number) => `/posts/${post}/comments`,
        list: (post: number) => `${API_BASE}/posts/${post}/comments`,
        delete: (id: number) => `${API_BASE}/posts/comments/${id}`
    },
    notifications: {
        list: (userId: string) => `/notifications/users/${userId}`,
        delete: (id: string) => `/notifications/${id}`,
        update_status: (ids: string[]) => `/notifications/read?ids=${ids}`
    },
    stories: {
        list: (userId: string) => `/stories/users/${userId}`,
        archive: (userId : string) => `/stories/archive/${userId}`,
        detail: (id: string) => `/stories/${id}`
    },
    images: {
        upload: `https://linkup.loca.lt/upload`
    },
    gateway: {
        health: `http://localhost:8080/actuator/health`
    }
}