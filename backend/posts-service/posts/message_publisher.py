from pika import BlockingConnection, ConnectionParameters, PlainCredentials
import json;

def publish_message(message):
    connection = None
    try:
        credentials = PlainCredentials('myuser', 'secret')
        connection = BlockingConnection(ConnectionParameters('localhost', 5672, '/', credentials))
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