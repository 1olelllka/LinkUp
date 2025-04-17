from django.test import TestCase
from rest_framework.test import APIClient
from ..models import Post
from unittest.mock import patch
import uuid
from django.test import TestCase
from rest_framework.test import APIClient
from unittest.mock import patch
import uuid
from ..models import Post
from testcontainers.rabbitmq import RabbitMqContainer
import time

@patch("py_eureka_client.eureka_client.init", return_value=(None, None))
class TestPostsAPI(TestCase):
    databases = {'default'}

    @classmethod
    def setUpClass(cls):
        cls.rabbitmq = RabbitMqContainer("rabbitmq:3.13-management")
        cls.rabbitmq.start()
        
        cls.rabbitmq_host = "localhost"
        cls.rabbitmq_port = cls.rabbitmq.get_exposed_port(5672)
        cls.rabbitmq_user = "myuser"
        cls.rabbitmq_password = "secret"
        
        time.sleep(3)  # Adjust based on your setup

    @classmethod
    def tearDownClass(cls):
        # Stop the RabbitMQ container after tests
        cls.rabbitmq.stop()

    def setUp(self):
        self.client = APIClient()
        self.user1_post = Post.objects.create(user_id='1', desc="User 1 - Post 1", image="url")
        self.user2_post = Post.objects.create(user_id='2', desc="User 2 - Post 1", image="url")

    def test_get_posts_by_specific_user(self, *args):
        response = self.client.get("/posts/users/1")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(len(response.json()['results']), 1)
    
    @patch("posts.views.requests.get")
    @patch("jwt.decode")
    def test_create_post_for_specific_user_returns_Http400(self, mock_jwt, mock_get, *args):
        mock_jwt.return_value = {'sub':"1"}
        mock_get.return_value.status_code = 200
        response = self.client.post("/posts/users/1", data={"user_id": "1", "desc": "something"}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 400)
    
    @patch("posts.views.requests.get")
    @patch("jwt.decode")
    def test_create_post_for_specific_user_returns_Http403(self, mock_jwt, mock_get, *args):
        mock_jwt.return_value = {'sub':"52"}
        mock_get.return_value.status_code = 200
        response = self.client.post("/posts/users/1", data={"user_id": "1", "desc": "something"}, headers={"Authorization":"Bearer incorrect_jwt"})
        self.assertEqual(response.status_code, 403)
    
    @patch("posts.views.requests.get")
    @patch("jwt.decode")
    def test_create_post_for_specific_user_returns_Http201(self, mock_jwt, mock_get, *args):
        mock_jwt.return_value = {'sub':'1'}
        mock_get.return_value.status_code = 200
        response = self.client.post("/posts/users/1", {"desc": "something", "image":"url"}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 201)
        self.assertEqual(response.json()['desc'], 'something')

    @patch("posts.views.requests.get")
    @patch("jwt.decode")
    def test_create_post_for_specific_user_returns_Http404(self, mock_jwt, mock_get, *args):
        mock_get.return_value.status_code = 404
        profile_id = uuid.uuid4()
        mock_jwt.return_value = {"sub":str(profile_id)}
        response = self.client.post(f"/posts/users/{profile_id}", {"user_id": str(profile_id), "desc": "something", "image":"url"}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 404)
    
    def test_get_specific_post_returns_Http404(self, *args):
        response = self.client.get('/posts/123')
        self.assertEqual(response.status_code, 404)

    def test_get_specific_post_returns_Http200(self, *args):
        response = self.client.get(f"/posts/{self.user1_post.pk}")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()['id'], self.user1_post.pk)

    @patch("jwt.decode")
    def test_patch_specific_post_returns_Http404(self, mock_jwt, *args):
        mock_jwt.return_value = {'sub':123}
        response = self.client.patch("/posts/123", {"user_id": "12", "desc": "desc", "image": "image"}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 404)
    
    def test_patch_specific_post_returns_Http403(self, mock_jwt, *args):
        respone = self.client.patch("/posts/123", {"user_id": "12", "desc": "desc", "image": "image"})
        self.assertEqual(respone.status_code, 403)
    
    @patch("jwt.decode")
    def test_patch_specific_post_returns_Http400(self, mock_jwt, *args):
        mock_jwt.return_value = {'sub':self.user1_post.user_id}
        response = self.client.patch(f"/posts/{self.user1_post.pk}", {"user_id": self.user1_post.user_id}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 400)

    @patch("jwt.decode")
    def test_patch_specific_post_returns_Http200(self, mocK_jwt, *args):
        mocK_jwt.return_value = {'sub':self.user1_post.user_id}
        response = self.client.patch(f"/posts/{self.user1_post.pk}", {"user_id": self.user1_post.user_id, "desc": "UPDATED", "image": "UPDATED"}, headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json()['desc'], "UPDATED")
        self.assertEqual(response.json()['image'], "UPDATED")
    
    @patch("jwt.decode")
    def test_delete_specific_post_returns_Http204(self, mock_jwt, *args):
        mock_jwt.return_value = {'sub':self.user2_post.user_id}
        response = self.client.delete(f"/posts/{self.user2_post.pk}", headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 204)
    
    @patch("jwt.decode")
    def test_delete_specific_post_returns_Http403(self, mock_jwt, *args):
        mock_jwt.return_value = {'sub':"1234"}
        response = self.client.delete(f"/posts/{self.user2_post.pk}", headers={"Authorization":"Bearer incorrect_jwt"})
        self.assertEqual(response.status_code, 403)
    
    @patch("jwt.decode")
    def test_delete_specific_post_returns_Http404(self, mock_jwt, *args):
        mock_jwt.return_value = {'sub':"1235"}
        response = self.client.delete("/posts/7654", headers={"Authorization":"Bearer jwt_token"})
        self.assertEqual(response.status_code, 404)
