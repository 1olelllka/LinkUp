import logging

import py_eureka_client.eureka_client as eureka

logging.basicConfig(level=logging.INFO)

try:
    registry_client, discovery_client = eureka.init(eureka_server="http://localhost:8761/eureka",
                                                    app_name="posts-service",
                                                    instance_port=8000)
    logging.info("Successfully registered with Eureka")
except Exception as e:
    logging.error(f"Failed to register with Eureka: {e}")