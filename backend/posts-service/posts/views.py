from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.generics import DestroyAPIView
from .models import Post, Comment
from .serializers import PostSerializer, CommentSerializer
from django.shortcuts import get_object_or_404
from django.http import Http404
import hashlib
from .message_publisher import publish_message
from .auth.authentication import JWTAuthentication
from .auth.permissions import IsOwner
from datetime import datetime, timedelta, timezone
from django.core.cache import cache
import requests
from django.http import JsonResponse
from rest_framework.permissions import IsAuthenticated
from django.core.exceptions import ValidationError
from drf_spectacular.utils import (
    extend_schema, extend_schema_view, OpenApiResponse, OpenApiExample
)
from drf_spectacular.types import OpenApiTypes
import os

profile_uri = os.environ.get("PROFILE_URI") or "http://localhost:8001/profiles"

@extend_schema(tags=['Posts management'])
@extend_schema_view(
    create=extend_schema(
        summary="Create new post",
        responses={
            201: PostSerializer,
            400: OpenApiResponse(
                description='Bad Request – validation error or unexpected client error',
                response=OpenApiTypes.OBJECT,
                examples=[
                    OpenApiExample(
                        'Validation error',
                        value={"error": "Unexpected client error occurred. Please try again later"},
                        status_codes=['400'],
                    )
                ]
            ),
            401: OpenApiResponse(
                description='Unauthorized – user not authenticated or token invalid',
                response=OpenApiTypes.OBJECT,
                examples=[
                    OpenApiExample(
                        'Unauthorized',
                        value={"detail": "Authentication credentials were not provided."},
                        status_codes=['401'],
                    )
                ]
            ),
            404: OpenApiResponse(
                description='User with such id does not exist',
                response=OpenApiTypes.OBJECT,
                examples=[
                    OpenApiExample(
                        'User not found',
                        value={"error": "User with such id does not exist"},
                        status_codes=['404'],
                    )
                ]
            )
        },
    ),
    list=extend_schema(summary="List all posts by user")
)
class UserPostViewSet(viewsets.ModelViewSet):
    queryset = Post.objects.all().order_by('-created_at')
    serializer_class = PostSerializer
    authentication_classes = [JWTAuthentication]

    def create(self, request, user_id):
        profile_response = requests.get(f"{profile_uri}/{user_id}")
        if profile_response.status_code == 404:
            return Response(data={"error": "User with such id does not exist"}, status=404)
        elif profile_response.status_code >= 500:
            return Response(data={"error": "An error occurred while processing your request"}, status=500)
        elif 400 <= profile_response.status_code < 404 and 404 < profile_response.status_code < 500:
            return Response(data={"error":"Unexpected client error occurred. Please try again later"}, status=profile_response.status_code)
        mutable_data = request.data.copy()
        mutable_data['user_id'] = user_id
        serializer = self.get_serializer(data=mutable_data)
        if serializer.is_valid():
            serializer.save()
            post_id = serializer.data['id']
            message_data = {"postId": f"{post_id}", "profileId": user_id, "timestamp": datetime.now().isoformat()}
            publish_message(message=message_data)
            return Response(data=serializer.data, status=201)
        return Response(data=serializer.errors, status=400)
    
    def get_queryset(self):
        return self.queryset.filter(user_id=self.kwargs['user_id'])
    
    def get_permissions(self):
        if (self.request.method == 'POST'):
            return [IsAuthenticated(), IsOwner()]
        return []
    
@extend_schema(
    tags=['Posts management']
)
@extend_schema_view(
    retrieve=extend_schema(
        summary="Get specific post",
        responses={
            200: PostSerializer,
            401: OpenApiResponse(
                description='Unauthorized – user not authenticated or token invalid',
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Unauthorized', value={"detail": "Authentication credentials were not provided."}, status_codes=['401'])]
            ),
            404: OpenApiResponse(
                description='Post not found',
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Not Found', value={"error": "Post not found"}, status_codes=['404'])]
            ),
        }
    ),
    partial_update=extend_schema(
        summary="Update the post",
        responses={
            200: PostSerializer,
            400: OpenApiResponse(
                description='Bad Request – validation error',
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Validation error', value={"error": "Invalid data"}, status_codes=['400'])]
            ),
            401: OpenApiResponse(
                description='Unauthorized – user not authenticated or not the owner',
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Unauthorized', value={"detail": "You do not have permission to perform this action."}, status_codes=['401'])]
            ),
            404: OpenApiResponse(
                description='Post not found',
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Not Found', value={"error": "Post not found"}, status_codes=['404'])]
            ),
        }
    ),
    destroy=extend_schema(
        summary="Delete the post",
        responses={
            204: OpenApiResponse(description='Post deleted successfully'),
            401: OpenApiResponse(
                description='Unauthorized – user not authenticated or not the owner',
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Unauthorized', value={"detail": "You do not have permission to perform this action."}, status_codes=['401'])]
            ),
            404: OpenApiResponse(
                description='Post not found',
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Not Found', value={"error": "Post not found"}, status_codes=['404'])]
            ),
        }
    ),
)
class PostViewSet(viewsets.ModelViewSet):
    queryset = Post.objects.all().order_by('-created_at')
    serializer_class = PostSerializer
    authentication_classes=[JWTAuthentication]

    def retrieve(self, request, *args, **kwargs):
        post_id = kwargs['post_id']
        cache_key = "post:" + hashlib.sha256((f"Post# {kwargs['post_id']}").encode('utf-8')).hexdigest()
        cached_data = cache.get(cache_key)
        if cached_data:
            return Response(cached_data)

        post = get_object_or_404(self.queryset, pk=post_id)
        serializer = self.get_serializer(post)
        if post.updated_at >= datetime.now(timezone.utc) - timedelta(days=1):
            cache.set(key=cache_key, value=serializer.data, timeout=60*60*24)
        return Response(serializer.data)

    def partial_update(self, request, *args, **kwargs):
        post = get_object_or_404(self.queryset, pk=kwargs['post_id'])
        mutable_data = request.data.copy()
        mutable_data['user_id'] = post.user_id
        serializer = self.get_serializer(post, data=mutable_data)
        serializer.is_valid(raise_exception=True)
        self.perform_update(serializer)
        cache_key = "post:" + hashlib.sha256((f"Post# {kwargs['post_id']}").encode('utf-8')).hexdigest()
        cache.set(key=cache_key, value=serializer.data, timeout=60*60*24)
        return Response(serializer.data)

    def destroy(self, request, *args, **kwargs):
        try:
            post = Post.objects.get(pk=kwargs['post_id'])
            self.perform_destroy(post)
            cache_key = "post:" + hashlib.sha256((f"Post# {kwargs['post_id']}").encode('utf-8')).hexdigest()
            if cache_key in cache:
                cache.delete(cache_key)
            return Response(status=204)
        except Post.DoesNotExist:
            return Response(status=204)
    
    def get_permissions(self):
        if (self.request.method in ['PATCH', 'DELETE']):
            return [IsAuthenticated(), IsOwner()]
        return []
        

@extend_schema(tags=['Comments management'])
@extend_schema_view(
    list=extend_schema(
        summary="List all comments by post",
        responses={
            200: CommentSerializer(many=True),
            404: OpenApiResponse(
                description="No comments found for this post",
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Not Found', value={"error": "No comments was found"}, status_codes=['404'])]
            ),
        }
    ),
    create=extend_schema(
        summary="Create new comment",
        responses={
            201: CommentSerializer,
            400: OpenApiResponse(
                description="Bad request – validation error or missing profile id",
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Validation error', value={"error": "Profile id is required"}, status_codes=['400'])]
            ),
            401: OpenApiResponse(
                description="Unauthorized – user not authenticated",
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Unauthorized', value={"detail": "Authentication credentials were not provided."}, status_codes=['401'])]
            ),
            404: OpenApiResponse(
                description="User or post not found",
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Not Found', value={"error": "User with such id does not exist"}, status_codes=['404'])]
            )
        }
    )
)
class CommentViewSet(viewsets.ModelViewSet):
    queryset = Comment.objects.filter(parent=None)
    serializer_class = CommentSerializer
    authentication_classes = [JWTAuthentication]

    def get_queryset(self):
        queryset =  self.queryset.filter(post_id=self.kwargs['post_id'])
        if not queryset.exists():
            raise Http404("No comments was found")
        return queryset

    def create(self, request, post_id):
        post = get_object_or_404(Post, pk=post_id)
        if request.user.id is not None:
            profile_response = requests.get(f"{profile_uri}/{request.user.id}")
        else:
            return Response(data={"error": "Profile id is required"}, status=400)
        if profile_response.status_code == 404:
            return Response(data={"error": "User with such id does not exist"}, status=404)
        elif profile_response.status_code >= 500:
            return Response(data={"error": "An error occurred while processing your request"}, status=500)
        elif 400 <= profile_response.status_code < 404 and 404 < profile_response.status_code < 500:
            return Response(data={"error":"Unexpected client error occurred. Please try again later"}, status=profile_response.status_code)
        data = request.data.copy()
        data['post'] = post.pk
        data['user_id'] = request.user.id
        data['username'] = profile_response.json()['username']
        data['name'] = profile_response.json()['name']
        data['photo'] = profile_response.json()['photo']
        serializer = self.get_serializer(data=data)
        if serializer.is_valid():
            try:
                serializer.save()
            except ValidationError as e:
                return Response(data={"errors":e.message_dict}, status=400)
            return Response(data=serializer.data, status=201)
        return Response(data=serializer.errors, status=400)

    def get_permissions(self):
        if (self.request.method == 'POST'):
            return [IsAuthenticated()]
        return []
    
@extend_schema(tags=['Comments management'])
@extend_schema_view(
    delete=extend_schema(
        summary="Delete specific comment",
        responses={
            204: OpenApiResponse(description="Comment deleted successfully"),
            401: OpenApiResponse(
                description="Unauthorized - user not authenticated or not owner",
                response=OpenApiTypes.OBJECT,
                examples=[OpenApiExample('Unauthorized', value={"detail": "You do not have permission to perform this action."}, status_codes=['401'])]
            ),
        }
    )
)
class CommentDeleteAPIView(DestroyAPIView):
    queryset = Comment.objects.all()
    serializer_class = CommentSerializer
    authentication_classes = [JWTAuthentication]
    permission_classes = [IsAuthenticated, IsOwner]
    
    def destroy(self, request, *args, **kwargs):
        try:
            comment = Comment.objects.get(pk=kwargs['comment_id'])
            self.perform_destroy(comment)
            return Response(status=204)
        except Comment.DoesNotExist:
            return Response(status=204)
        

def health(request):
    return JsonResponse({"status":"UP"})

def info(request):
    return JsonResponse({})