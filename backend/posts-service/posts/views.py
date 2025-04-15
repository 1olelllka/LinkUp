from rest_framework import viewsets
from rest_framework.response import Response
from rest_framework.generics import DestroyAPIView
from .models import Post, Comment
from .serializers import PostSerializer, CommentSerializer
from django.shortcuts import get_object_or_404
from django.http import Http404
import hashlib
from .message_publisher import publish_message
from datetime import datetime, timedelta, timezone
from django.core.cache import cache
import requests
from django.http import JsonResponse

class UserPostViewSet(viewsets.ModelViewSet):
    queryset = Post.objects.all().order_by('-created_at')
    serializer_class = PostSerializer

    def create(self, request, user_id):
        profile_response = requests.get(f"http://localhost:8001/profiles/{user_id}")
        if profile_response.status_code == 404:
            return Response(data={"error": "User with such id does not exist"}, status=404)
        elif profile_response.status_code != 200:
            return Response(data={"error": "An error occurred while processing your request"}, status=500)
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
    

class PostViewSet(viewsets.ModelViewSet):
    queryset = Post.objects.all().order_by('-created_at')
    serializer_class = PostSerializer

    def retrieve(self, request, *args, **kwargs):
        cache_key = hashlib.sha256((f"Post# {kwargs['post_id']}").encode('utf-8')).hexdigest()
        if cache_key in cache:
            queryset = cache.get(cache_key)
            return Response(queryset)
        post = get_object_or_404(self.queryset, pk=kwargs['post_id'])
        serializer = self.get_serializer(post)
        if post.created_at >= datetime.now(timezone.utc) - timedelta(days=1):
            cache.set(key=cache_key, value=serializer.data, timeout=60*60*24)
        return Response(serializer.data)

    def partial_update(self, request, *args, **kwargs):
        post = get_object_or_404(self.queryset, pk=kwargs['post_id'])
        mutable_data = request.data.copy()
        mutable_data['user_id'] = post.user_id
        serializer = self.get_serializer(post, data=mutable_data)
        serializer.is_valid(raise_exception=True)
        self.perform_update(serializer)
        cache_key = hashlib.sha256((f"Post# {kwargs['post_id']}").encode('utf-8')).hexdigest()
        cache.set(key=cache_key, value=serializer.data, timeout=60*60*24)
        return Response(serializer.data)

    def destroy(self, request, *args, **kwargs):
        try:
            post = Post.objects.get(pk=kwargs['post_id'])
            self.perform_destroy(post)
            cache_key = hashlib.sha256((f"Post# {kwargs['post_id']}").encode('utf-8')).hexdigest()
            if cache_key in cache:
                cache.delete(cache_key)
            return Response(status=204)
        except Post.DoesNotExist:
            return Response(status=204)
        

class CommentViewSet(viewsets.ModelViewSet):
    queryset = Comment.objects.filter(parent=None).order_by('-created_at')
    serializer_class = CommentSerializer

    def get_queryset(self):
        queryset =  self.queryset.filter(post_id=self.kwargs['post_id'])
        if not queryset.exists():
            raise Http404("No comments was found")
        return queryset

    def create(self, request, post_id):
        post = get_object_or_404(Post, pk=post_id)
        if request.data.get('user_id') is not None:
            profile_response = requests.get(f"http://localhost:8001/profiles/{request.data['user_id']}")
        else:
            return Response(data={"error": "Profile id is required"}, status=400)
        if (profile_response.status_code != 200):
            return Response(data={"error": "User with such id does not exist"}, status=404)
        data = request.data.copy()
        data['post'] = post.pk
        serializer = self.get_serializer(data=data)
        if serializer.is_valid():
            serializer.save()
            return Response(data=serializer.data, status=201)
        return Response(data=serializer.errors, status=400)
    

class CommentDeleteAPIView(DestroyAPIView):
    queryset = Comment.objects.all()
    serializer_class = CommentSerializer
    
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