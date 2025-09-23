import logging

import py_eureka_client.eureka_client as eureka
import socket

logging.basicConfig(level=logging.INFO)
host_ip = socket.gethostbyname(socket.gethostname())


try:
    registry_client, discovery_client = eureka.init(eureka_server="http://localhost:8761/eureka",
                                                    app_name="posts-service",
                                                    instance_port=8000,
                                                    instance_host=host_ip,
                                                    metadata={"region":"eu-center", "version":"v0.9"},
                                                    health_check_url="http://localhost:8000/posts/health",
                                                    status_page_url="http://localhost:8000/posts/info"
)
    logging.info("Successfully registered with Eureka")
except Exception as e:
    logging.error(f"Failed to register with Eureka: {e}")