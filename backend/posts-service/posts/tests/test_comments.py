from django.test import TestCase
from rest_framework.test import APIClient
from ..models import Post, Comment


class TestCommentAPI(TestCase):

    def setUp(self):
        self.client = APIClient()
        self.post = Post.objects.create(user_id=1, desc="desc 1", image="url")
        self.comment_post_1 = Comment.objects.create(user_id=1, post_id=self.post.pk, text="text 1")
        self.comment_post_2 = Comment.objects.create(user_id=2, post_id=self.post.pk, text="text 2")
        self.comment_post_3 = Comment.objects.create(user_id=2, post_id=self.post.pk, text="reply to text 1", parent_id=self.comment_post_1.pk)
        
    def test_get_comments_to_specific_post_returns_Http404(self):
        response = self.client.get("/posts/123/comments")
        self.assertEqual(response.status_code, 404)
    
    def test_get_comments_to_specific_post_returns_Http200(self):
        response = self.client.get(f'/posts/{self.post.pk}/comments')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()['results']), 2)
    
    def test_create_comments_for_specific_post_returns_Http404(self):
        response = self.client.post("/posts/123/comments")
        self.assertEqual(response.status_code, 404)
    
    def test_create_comments_for_specific_post_returns_Http400(self):
        response = self.client.post(f"/posts/{self.post.pk}/comments", {"text":""})
        self.assertEqual(response.status_code, 400)

    def test_create_comments_for_specific_post_returns_Http201(self):
        response = self.client.post(f"/posts/{self.post.pk}/comments", {"text":"text", "user_id":12})
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.json()['user_id'], 12)
        self.assertEqual(response.json()['text'], 'text')
    
    def test_create_reply_for_specific_comment_returns_Http400(self):
        response = self.client.post(f"/posts/{self.post.pk}/comments", {"text":"text", "user_id":12, "parent":1235})
        self.assertEqual(response.status_code, 400)
    
    def test_create_reply_for_specific_comment_returns_Http201(self):
        response = self.client.post(f"/posts/{self.post.pk}/comments", {"text":"reply", "user_id":13, "parent":self.comment_post_1.pk})
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.json()['parent'], self.comment_post_1.pk)

    def test_delete_comment_returns_Http204(self):
        response = self.client.delete(f"/posts/comments/{self.comment_post_1.pk}")
        self.assertEqual(response.status_code, 204)