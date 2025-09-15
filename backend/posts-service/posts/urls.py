from django.urls import path
from . import views
from drf_spectacular.views import SpectacularAPIView, SpectacularSwaggerView


urlpatterns = [
    path("users/<str:user_id>", views.UserPostViewSet.as_view({'get': 'list', 'post':'create'}) , name='user-posts'),
    path("<int:post_id>", views.PostViewSet.as_view({'get': 'retrieve', 'patch': 'partial_update', 'delete': 'destroy'}), name="posts"),
    path('<int:post_id>/comments', views.CommentViewSet.as_view({'get': 'list', 'post': 'create'}), name="comments"),
    path('comments/<int:comment_id>', views.CommentDeleteAPIView.as_view(), name="delete-comment"),
    path("health", views.health, name="health_check"),
    path("info", views.info, name="info"),
    path('schema/', SpectacularAPIView.as_view(), name='schema'),
    path('docs/', SpectacularSwaggerView.as_view(
        url='/api/posts/schema/'),  # absolute path on the gateway (won't work on posts service on itself, but works on gateway)
        name='swagger-ui'),
]