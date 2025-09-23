import json
from django.db import transaction
import pika
from time import sleep

class RabbitMQConsumer:
    def __init__(self, host="localhost", port=5672, username="myuser", password="secret"):
        self.connection_params = {
            "host": host,
            "port": port,
            "credentials": pika.PlainCredentials(username, password)
        }
        self.exchange_name = "profile_fanout_exchange"
        self.queue_name = "delete_profile_queue_post"
        self.channel = None
        self.connection = None

    def connect(self):
        if not self.connection or self.connection.is_closed:
            self.connection = pika.BlockingConnection(
                pika.ConnectionParameters(**self.connection_params)
            )
            self.channel = self.connection.channel()
            
            self.channel.exchange_declare(
                exchange=self.exchange_name,
                exchange_type="fanout",
                durable=True
            )
            
            self.channel.queue_declare(
                queue=self.queue_name,
                durable=True,
            )
            
            self.channel.queue_bind(
                exchange=self.exchange_name,
                queue=self.queue_name,
            )
            
            self.channel.basic_qos(prefetch_count=1)

    def process_message(self, ch, method, properties, body):
        try:
            user_id = json.loads(body)
            with transaction.atomic():
                from posts.models import Post, Comment
                posts_deleted = Post.objects.filter(user_id=user_id).delete()
                comments_deleted = Comment.objects.filter(user_id=user_id).delete()
                
                print(f"Deleted {posts_deleted[0]} posts and {comments_deleted[0]} comments for user {user_id}")
            
            ch.basic_ack(delivery_tag=method.delivery_tag)
            print("Message processed successfully")
            
        except Exception as e:
            print(f"Error processing message: {e}")
            ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)

    def run(self):
        """Run the consumer"""
        while True:
            try:
                self.connect()                
                self.channel.basic_consume(
                    queue=self.queue_name,
                    on_message_callback=self.process_message,
                    auto_ack=False 
                )
                self.channel.start_consuming()
                
            except pika.exceptions.ConnectionClosedByBroker:
                print("Connection closed by broker, retrying...")
                continue
                
            except pika.exceptions.AMQPChannelError as e:
                print(f"Channel error: {e}, retrying...")
                continue
                
            except pika.exceptions.AMQPConnectionError:
                print("Connection was closed, retrying...")
                sleep(5)
                continue
                
            except KeyboardInterrupt:
                print("Shutting down consumer...")
                if self.channel:
                    self.channel.close()
                if self.connection:
                    self.connection.close()
                break
                
            except Exception as e:
                print(f"Unexpected error: {e}")
                sleep(5)
                continue