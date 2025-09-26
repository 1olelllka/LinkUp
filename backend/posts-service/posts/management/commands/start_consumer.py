from django.core.management.base import BaseCommand
from posts.message_listener import RabbitMQConsumer
import os

class Command(BaseCommand):
    help = 'Starts the RabbitMQ consumer for profile deletion'

    def handle(self, *args, **options):
        consumer = RabbitMQConsumer(
            host=os.environ.get("RABBIT_HOST") or "localhost",
            port=os.environ.get("RABBIT_PORT") or 5672,
            username=os.environ.get("RABBIT_USERNAME") or "myuser",
            password=os.environ.get("RABBIT_PASSWORD") or "secret"
        )
        
        self.stdout.write(
            self.style.SUCCESS('Starting RabbitMQ consumer...')
        )
        
        consumer.run()