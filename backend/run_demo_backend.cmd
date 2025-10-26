@echo off
ECHO Starting up demo-version of backend...

ECHO Opening Global Services...

docker compose up -d

ECHO Waiting for Global Services to start up...
SLEEP 7

ECHO Opening Discovery Server...
CD discovery-server
mvn clean package -DskipTests
docker compose up --build -d

ECHO Opening Profile Service...
CD ../gateway
mvn clean package -DskipTests
docker compose up --build -d

ECHO Opening Profile Service...
cd ../profile-service
mvn clean package -DskipTests
docker compose up --build -d

ECHO Opening Chat Service...
cd ../posts-service
docker compose up --build -d

ECHO Opening Custom Image Storage...
cd ../image-storage
mvn clean package -DskipTests
docker compose up --build -d

ECHO Completed! 

PAUSE
