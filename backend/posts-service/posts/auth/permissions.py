from rest_framework.permissions import BasePermission
from ..models import Post, Comment
from django.shortcuts import get_object_or_404

class IsOwner(BasePermission):
    def has_permission(self, request, obj):
        # print("check --> " + request.user.get('sub') + " vs. " + obj.kwargs.get('user_id'))
        if (obj.kwargs.get('user_id') != None):
            return request.user.get('sub') == obj.kwargs.get('user_id')
        elif (obj.kwargs.get('post_id') != None):
            model = get_object_or_404(Post, pk=obj.kwargs.get('post_id'))
            return request.user.get('sub') == model.user_id
        elif (obj.kwargs.get('comment_id') != None):
            model = get_object_or_404(Comment, pk=obj.kwargs.get('comment_id'))
            return request.user.get('sub') == model.user_id
        else:
            return False