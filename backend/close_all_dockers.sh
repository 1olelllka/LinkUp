
echo "Running Script..."

echo "Closing Global Docker..."
docker compose down

echo "Closing Discovery Server..."
cd discovery-server
docker compose down

echo "Closing Gateway Docker..."
cd ../gateway
docker compose down

echo "Closing Profile Service Docker..."
cd ../profile-service
docker compose down

echo "Closing Auth Service Docker..."
cd ../auth-service
docker compose down

echo "Closing Posts Service Docker..."
cd ../posts-service
docker compose down

echo "Closing Feed Service Docker..."
cd ../feed-service
docker compose down

echo "Closing Stories Service Docker..."
cd ../stories-service
docker compose down

echo "Closing Chat Service Docker..."
cd ../chat-service
docker compose down

echo "Closing Notification Service Docker..."
cd ../notification-service
docker compose down

echo "Completed..."
