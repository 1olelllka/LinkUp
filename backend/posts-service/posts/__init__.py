import logging

import py_eureka_client.eureka_client as eureka
import socket

logging.basicConfig(level=logging.INFO)
host_ip = socket.gethostbyname(socket.gethostname())


try:
    registry_client, discovery_client = eureka.init(eureka_server="http://localhost:8761/eureka",
                                                    app_name="posts-service",
                                                    instance_port=8000,
                                                    instance_host=host_ip
)
    logging.info("Successfully registered with Eureka")
except Exception as e:
    logging.error(f"Failed to register with Eureka: {e}")