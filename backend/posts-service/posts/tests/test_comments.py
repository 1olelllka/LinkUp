from django.test import TestCase
from rest_framework.test import APIClient
from unittest.mock import patch
import uuid
from ..models import Post, Comment


@patch("py_eureka_client.eureka_client.init", return_value=(None, None))
class TestCommentAPI(TestCase):
    databases = {'default'}

    def setUp(self):
        self.client = APIClient()
        self.post = Post.objects.create(user_id='1', desc="desc 1", image="url")
        self.comment_post_1 = Comment.objects.create(user_id="1", post_id=self.post.pk, text="text 1")
        self.comment_post_2 = Comment.objects.create(user_id="2", post_id=self.post.pk, text="text 2")
        self.comment_post_3 = Comment.objects.create(user_id="2", post_id=self.post.pk, text="reply to text 1", parent_id=self.comment_post_1.pk)
        
    def test_get_comments_to_specific_post_returns_Http404(self, *args):
        response = self.client.get("/posts/123/comments")
        self.assertEqual(response.status_code, 404)
    
    def test_get_comments_to_specific_post_returns_Http200(self, *args):
        response = self.client.get(f'/posts/{self.post.pk}/comments')
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()['results']), 2)

    @patch("jwt.decode")
    def test_create_comments_for_specific_post_returns_Http404(self, mock_jwt, *args):
        mock_jwt.return_value = {'sub':"id"}
        response = self.client.post("/posts/123/comments", headers={"Authorization": "Bearer jwt_token"})
        self.assertEqual(response.status_code, 404)
    
    @patch("posts.views.requests.get")
    @patch("jwt.decode")
    def test_create_comments_for_specific_post_returns_Http400(self, mock_jwt, mock_get, *args):
        mock_jwt.return_value = {'sub':self.post.user_id}
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = {
            "username": "some_username",
            "name": "test_name",
            "photo":""
        }
        response = self.client.post(f"/posts/{self.post.pk}/comments", {"text":""}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 400)

    @patch("posts.views.requests.get")
    @patch("jwt.decode")
    def test_create_comments_for_specific_post_returns_Http201(self, mock_jwt, mock_get, *args):
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = {
            "username": "some_username",
            "name": "test_name",
            "photo":""
        }
        mock_jwt.return_value = {'sub':12}
        response = self.client.post(f"/posts/{self.post.pk}/comments", {"text":"text"}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.json()['user_id'], '12')
        self.assertEqual(response.json()['text'], 'text')
        self.assertEqual(response.json()['username'], 'some_username')
        self.assertEqual(response.json()['name'], "test_name")
        self.assertEqual(response.json()['photo'], "")
    

    @patch("posts.views.requests.get")
    @patch("jwt.decode")
    def test_create_reply_for_specific_comment_returns_Http400(self, mock_jwt, mock_get, *args):
        mock_jwt.return_value = {'sub':self.post.user_id}
        mock_get.return_value.status_code = 200
        response = self.client.post(f"/posts/{self.post.pk}/comments", {"text":"text", "user_id":12, "parent":1235}, headers={"Authorization": "Bearer jwt_token"})
        self.assertEqual(response.status_code, 400)
    
    @patch("posts.views.requests.get")
    @patch("jwt.decode")
    def test_create_reply_for_specific_comment_returns_Http201(self, mock_jwt, mock_get, *args):
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = {
            "username": "reply_username",
            "name": "reply_name",
            "photo":""
        }
        mock_jwt.return_value = {'sub':self.post.user_id}
        response = self.client.post(f"/posts/{self.post.pk}/comments", {"text":"reply", "user_id":13, "parent":self.comment_post_1.pk}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.json()['parent'], self.comment_post_1.pk)
        self.assertEqual(response.json()['username'], "reply_username")
        self.assertEqual(response.json()['name'], "reply_name")
        self.assertEqual(response.json()['photo'], '')
    
    @patch("posts.views.requests.get")
    @patch("jwt.decode")
    def test_create_comment_or_reply_returns_Http404(self, mock_jwt, mock_get, *args):
        mock_get.return_value.status_code = 404
        mock_jwt.return_value = {'sub':self.post.user_id}
        profile_id = uuid.uuid4()
        response = self.client.post(f"/posts/{self.post.pk}/comments", {"text":"reply", "user_id":profile_id, "parent":self.comment_post_1.pk}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 404)

    @patch("jwt.decode")
    def test_delete_comment_returns_Http403(self, mock_jwt, *args):
        mock_jwt.return_value = {'sub':123}
        response = self.client.delete(f"/posts/comments/{self.comment_post_1.pk}", headers={"Authorization":"Bearer incorrect_jwt"})
        self.assertEqual(response.status_code, 403)

    @patch("jwt.decode")
    def test_delete_comment_returns_Http204(self, mock_jwt, *args):
        mock_jwt.return_value = {'sub':self.comment_post_1.user_id}
        response = self.client.delete(f"/posts/comments/{self.comment_post_1.pk}", headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 204)