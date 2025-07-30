
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