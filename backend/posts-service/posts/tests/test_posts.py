from django.test import TestCase
from rest_framework.test import APIClient
from ..models import Post, Comment
from unittest.mock import patch

@patch("py_eureka_client.eureka_client.init", return_value=(None, None))
class TestPostsAPI(TestCase):

    def setUp(self):
        self.client = APIClient()
        self.user1_post = Post.objects.create(user_id=1, desc="User 1 - Post 1", image="url")
        self.user2_post = Post.objects.create(user_id=2, desc="User 2 - Post 1", image="url")

    def test_get_posts_by_specific_user(self, *args):
        response = self.client.get("/posts/users/1")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()['results']), 1)
    
    def test_create_post_for_specific_user_returns_Http400(self, *args):
        response = self.client.post("/posts/users/1", data={"user_id": 1, "desc": "something"})
        self.assertEqual(response.status_code, 400)
    
    def test_create_post_for_specific_user_returns_Http200(self, *args):
        response = self.client.post("/posts/users/1", {"user_id": 1, "desc": "something", "image":"url"})
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.json()['desc'], 'something')
    
    def test_get_specific_post_returns_Http404(self, *args):
        response = self.client.get('/posts/123')
        self.assertEqual(response.status_code, 404)

    def test_get_specific_post_returns_Http200(self, *args):
        response = self.client.get(f"/posts/{self.user1_post.pk}")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()['id'], self.user1_post.pk)

    def test_patch_specific_post_returns_Http404(self, *args):
        response = self.client.patch("/posts/123", {"user_id": 12, "desc": "desc", "image": "image"})
        self.assertEqual(response.status_code, 404)
    
    def test_patch_specific_post_returns_Http400(self, *args):
        response = self.client.patch(f"/posts/{self.user1_post.pk}", {"user_id": self.user1_post.user_id})
        self.assertEqual(response.status_code, 400)

    def test_patch_specific_post_returns_Http200(self, *args):
        response = self.client.patch(f"/posts/{self.user1_post.pk}", {"user_id": self.user1_post.user_id, "desc": "UPDATED", "image": "UPDATED"})
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()['desc'], "UPDATED")
        self.assertEqual(response.json()['image'], "UPDATED")
    
    def test_delete_specific_post_returns_Http204(self, *args):
        response = self.client.delete("/posts/1234")
        self.assertEqual(response.status_code, 204)

    def test_get_posts_by_users_returns_list_of_posts(self, *args):
        response = self.client.get("/posts/", {"user_ids": "{1,2,3}"})
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()), 2)
    
    def test_get_posts_by_users_returns_empty_list(self, *args):
        response = self.client.get("/posts/", {'user_ids': "{123,1234}"})
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()), 0)
