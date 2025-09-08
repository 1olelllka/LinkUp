from typing import Iterable
from django.db import models
from django.core.exceptions import ValidationError
class Post(models.Model):
    user_id = models.CharField(db_index=True, blank=True) # blank=True for validation purposes
    image = models.CharField(max_length=200)
    desc = models.TextField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)

    def __str__(self):
        return 'Post #'  + str(self.pk) + ' by ' + str(self.user_id)
    
    class Meta:
        ordering = ['-created_at']
        verbose_name = 'Post'
        verbose_name_plural = 'Posts'

class Comment(models.Model):
    user_id = models.CharField(blank=True)
    photo = models.CharField(blank=True, null=True)
    username = models.CharField(blank=True)
    name = models.CharField(blank=True)
    post = models.ForeignKey(Post, on_delete=models.CASCADE)
    text = models.TextField()
    parent = models.ForeignKey('self', on_delete=models.CASCADE, null=True, blank=True, related_name='replies')
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return "Comment #" + str(self.pk) + " by " + str(self.user_id) + " on " + str(self.post.pk)
    

    def clean(self) -> None:
        if self.parent and self.parent.parent:
            raise ValidationError("Only one level of replies allowed!")
        
    def save(self, *args, **kwargs):
        self.full_clean()
        return super().save(*args, **kwargs);
    class Meta:
        verbose_name = 'Comment'
        verbose_name_plural = 'Comments'