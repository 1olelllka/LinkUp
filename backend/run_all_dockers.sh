#!/bin/bash

echo "Running Script..."

echo "Opening Global Docker..."
docker compose up -d

echo "Opening Gateway Docker..."
cd gateway
docker compose up -d

echo "Opening Profile Service Docker..."
cd ../profile-service
docker compose up -d

echo "Opening Auth Service Docker..."
cd ../auth-service
docker compose up -d

echo "Opening Posts Service Docker..."
cd ../posts-service
docker compose up -d

echo "Opening Feed Service Docker..."
cd ../feed-service
docker compose up -d

echo "Opening Stories Service Docker..."
cd ../stories-service
docker compose up -d

echo "Opening Chat Service Docker..."
cd ../chat-service
docker compose up -d

echo "Opening Notification Service Docker..."
cd ../notification-service
docker compose up -d

echo "Completed..."
