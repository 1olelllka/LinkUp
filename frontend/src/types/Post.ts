

export type Post = {
  id: number;
  user_id: string;
  desc: string;
  image: string;
  created_at: string;
};

export type Comment = {
  id: number,
  text: string,
  user_id: string,
  photo: string | undefined,
  username: string,
  name: string
  created_at: string,
  replies?: Comment[]
}

export type PostPage = {
  count: number,
  next: string,
  previous: string,
  results: Post[]
}

export type CommentPage = {
  count: number,
  next: string,
  previous: string,
  results: Comment[]
}

export type FeedPage = {
  content: Post[],
  last: boolean,
  first: boolean,
}