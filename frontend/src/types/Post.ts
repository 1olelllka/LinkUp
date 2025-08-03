

export type Post = {
  id: number;
  user_id: string;
  desc: string;
  image: string;
  created_at: string;
  title: string
};

export type Comment = {
  id: number,
  text: string,
  photo: string | undefined,
  username: string,
  name: string
  replies: Comment[]
}