# Generated by Django 5.2 on 2025-04-18 11:52

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('posts', '0002_remove_post_likes_alter_post_user_id'),
    ]

    operations = [
        migrations.AddField(
            model_name='post',
            name='updated_at',
            field=models.DateTimeField(auto_now=True),
        ),
        migrations.AlterField(
            model_name='post',
            name='user_id',
            field=models.CharField(blank=True, db_index=True),
        ),
    ]
