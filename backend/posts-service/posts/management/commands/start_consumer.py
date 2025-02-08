from django.core.management.base import BaseCommand
from posts.message_listener import RabbitMQConsumer

class Command(BaseCommand):
    help = 'Starts the RabbitMQ consumer for profile deletion'

    def handle(self, *args, **options):
        consumer = RabbitMQConsumer(
            host="localhost",
            port=5672,
            username="myuser",
            password="secret"
        )
        
        self.stdout.write(
            self.style.SUCCESS('Starting RabbitMQ consumer...')
        )
        
        consumer.run()