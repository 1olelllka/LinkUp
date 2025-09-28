#!/bin/bash

echo "Starting up fully-functional backend..."

echo "Opening Global Services..."
docker compose up -d

echo "Waiting for Global Services to start up..."
sleep 7

echo "Opening Discovery Server..."
cd discovery-server
mvn clean package -DskipTests
docker compose up --build -d

echo "Opening Gateway..."
cd ../gateway
mvn clean package -DskipTests
docker compose up --build -d

echo "Opening Profile Service..."
cd ../profile-service
mvn clean package -DskipTests
docker compose up --build -d

echo "Opening Auth Service..."
cd ../auth-service
mvn clean package -DskipTests
docker compose up --build -d

echo "Opening Posts Service..."
cd ../posts-service
docker compose up --build -d

echo "Opening Feed Service..."
cd ../feed-service
mvn clean package -DskipTests
docker compose up --build -d

echo "Opening Stories Service..."
cd ../stories-service
mvn clean package -DskipTests
docker compose up --build -d

echo "Opening Chat Service..."
cd ../chat-service
mvn clean package -DskipTests
docker compose up --build -d

echo "Opening Notification Service..."
cd ../notification-service
mvn clean package -DskipTests
docker compose up --build -d

echo "Opening Custom Image Storage..."
cd ../image-storage
mvn clean package -DskipTests
docker compose up --build -d

echo "Completed!"
