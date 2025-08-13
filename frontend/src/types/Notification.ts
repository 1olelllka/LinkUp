
export type Notification = {
    id : string,
    userId: string,
    text: string,
    read: boolean,
    createdAt: string
}

export type NotificationPage = {
    content: Notification[],
    last: boolean,
    totalPages: number,
    totalElements: number,
    pageable: {
        pageNumber: number
    }
}