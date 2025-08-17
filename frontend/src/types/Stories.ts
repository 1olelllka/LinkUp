
export type Story = {
    id : string,
    image : string;
    userId : string;
    available : boolean;
    createdAt : string;
}

export type StoryPage = {
    pageable: {
        pageNumber: number,
        offset: number
    },
    totalElements: number,
    totalPages: number,
    content: Story[],
    last: boolean,
    numberOfElements: number,
}