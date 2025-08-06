export type Profile = {
  id: string,
  username: string,
  email: string,
  name: string,
  authProvider: string,
  photo: string,
  aboutMe: string,
  gender: "MALE" | "FEMALE" | "UNDEFINED",
  dateOfBirth: string,
  createdAt: string
}

type Pageable = {
    pageNumber: number,
}

export type ProfilePage = {
    content: UserList[],
    first: boolean,
    last: boolean,
    totalPages: number,
    totalElements: number,
    pageable: Pageable
}


export type UserList = {
    id: string,
    name: string,
    username: string,
    photo: string
}