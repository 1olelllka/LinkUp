from pika import BlockingConnection, ConnectionParameters, PlainCredentials
from django.conf import settings
import json
import os

def publish_message(message):
    connection = None
    try:
        credentials = PlainCredentials(os.environ.get("RABBIT_USERNAME") or 'myuser', os.environ.get("RABBIT_PASSWORD") or 'secret')
        connection = BlockingConnection(ConnectionParameters(os.environ.get("RABBIT_HOST") or 'localhost', settings.RABBIT_PORT, '/', credentials))
        channel = connection.channel()
        channel.exchange_declare(exchange='posts_exchange',
                               exchange_type='topic',
                               durable=True)

        channel.queue_declare(queue="feed_updates_queue", durable=True)
        channel.queue_bind(exchange='posts_exchange',
                          queue='feed_updates_queue',
                          routing_key='post.new')
        message_json = json.dumps(message)
        channel.basic_publish(exchange="posts_exchange", routing_key="post.new", body=message_json)
        print(" [x] Sent %r" % message_json)
    except Exception as e:
        print("Error: %s" % e)