# LinkUp!
![Status](https://img.shields.io/badge/status-in%20development-yellow)
![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)  
> A modern social networking platform built with microservices architecture, emphasizing scalability and real-time interactions.

LinkUp! connects users through posts, stories, real-time chat, and live notifications. Built as a portfolio project to demonstrate full-stack development with modern cloud-native patterns.

## Table of Contents 
 - [Key Features](#key-features)
 - [Releases](#releases)
    - [0.98-BETA](#-098-beta)
    - [1.0-STABLE](#-10-stable)
 - [How to Run LinkUp!](#how-to-run-linkup)
 - [Current Status](#current-status)
 - [Demo Videos](#demo-videos)
 - [Tech Stack](#tech-stack)
 - [Project Structure](#project-structure)
    - [Backend Architecture](#backend-architecture)
 - [License](#license)

## Key Features
- 📱 Real-time chat and notifications via WebSocket
- 🎨 Modern, responsive UI with React and TailwindCSS
- 🏗️ Microservices architecture with 8+ independent services
- 🔍 Advanced search with ElasticSearch
- 🐳 Fully containerized with Docker
- 🔐 OAuth2 authentication (Google)

## Releases

### 🚀 0.98-BETA
#### Release Notes
- Most Viable Product (MVP) — feature-complete
- Dockerized for easy deployment
- Backend start-up automated with bash scripts
- Full and demo versions available:
  - **Full Version**: Requires at least 5.5GB dedicated Docker RAM
  - **Demo Version**: Requires at least 3.4GB dedicated Docker RAM
- Frontend bugs may occur in some components
- Backend uses microservices architecture for scalability and resilience
- Custom image storage uses localtunnel, emulating CDN

#### Highlights
- Fully working MVP with posts, feeds, chat, stories, and notifications
- Automated start-up script for all services
- Frontend-ready with React, TailwindCSS, and ShadCN

### 🔮 1.0-STABLE

#### Release Notes
- First stable version – fixed critical bugs from 0.98-BETA
- Docker Compose files now preserve secrets correctly via .env
- Added automated start-up script for Windows
- Fixed chat service’s last message handling
- Fixed circuit breaker behavior with service unavailable error for improved UX
- Cosmetic and bug fixes in some frontend components
  
#### Highlights
- Secure and configurable setup via .env for local or containerized environments
- One-step automated start-up script for all services

## How to Run LinkUp!
**Prerequisites:**  
- Docker & Docker Compose installed  
- Sufficient RAM for chosen version (5.5GB full / 3.4GB demo)

**Steps (released versions)**
1. Download the Release archives (they should start with `linkup_<version>_...`) from *Releases Tab* and unzip it
   - Create folder where you want to download all zip archives
   - Unzip frontend archive
```bash
unzip linkup_<version>_frontend_and_readme.zip
```
   - Then, run a bash script to unarchive backend
```bash
   ./unzip_backend.sh
   # Or run unzip_backend.cmd file if using Windows (available from 1.0-STABLE)
```
2. **Start backend services** using the bash script:  
```bash
  cd backend
  ./run_full_backend.sh   # OR ./run_demo_backend.sh
  # If you use Windows, run the same files with .cmd extension (available from 1.0-STABLE)
```
*Note: First launches may take longer if services like MongoDB, Redis, etc., are not preinstalled.*

3. Verify services are running in **Docker Desktop** (all containers should be up).
4. **Start frontend**
```bash
  cd ../frontend # given you're on ./backend folder
  docker compose up --build -d
```
5. Access the website in your browser by url http://localhost:5173 *(Only accessible on your machine)*
6. Next, configure LocalTunnel (used as a custom CDN emulator for images)
```bash
    # Install Node (if not preinstalled) (for Windows visit https://nodejs.org/en/download)
    brew install node # MacOS via Homebrew
    sudo apt install -y nodejs npm # Ubuntu/Debian
    # Run localtunnel
    npx localtunnel --port 8888 --subdomain linkup
    # On another terminal run following to get localtunnel password
    curl https://loca.lt/mytunnelpassword
    # Afterwards rerun the localtunnel and go to https://linkup.loca.lt and pass the localtunnel password
    # retreived from previous command (it should look like IPv4 address)
```
7. Instead of custom stopping & removal of backend containers, run this command inside of **backend folder**
```bash
   # Given you're inside of backend folder
   ./close_backend.sh
   # If you use Windows, run the same files with .cmd extension (available from 1.0-STABLE)
```

8. In order to stop frontend run this command inside of **frontend folder**
```bash
  # Given you're inside of frontend folder
  docker compose down
```

**Note: if you encounter permission errors while running bash scripts, run this command**
```bash
   chmod +x <name_of_bash_script>.sh # on Linux/Unix-based systems
```
<hr>

**Steps (cloning repository)**

*Note: stable updates are added to main branch*
1. Clone the repository
```bash
   git clone https://github.com/1olelllka/LinkUp.git
```
2. Add your own **application.properties** file for auth service and paste this:
```.env
server.port=8010
spring.application.name=auth-service

eureka.client.serviceUrl.defaultZone=${EUREKA_URI:http://localhost:8761/eureka}
eureka.client.fetchRegistry=true
eureka.client.registerWithEureka=true
eureka.instance.preferIpAddress=true
eureka.instance.metadata-map.version=v0.8
eureka.instance.metadata-map.region=eu-central

management.endpoint.health.show-details=always
management.endpoints.web.exposure.include=health, info
management.endpoints.web.base-path=/auth/actuator
management.endpoints.path-mapping.health=/health

spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6375}

spring.data.mongodb.uri=${MONGO_URI:mongodb://admin:admin@localhost:27014/auth?authSource=admin}
spring.data.mongodb.uuid-representation=standard

spring.rabbitmq.host=${RABBIT_HOST:localhost}
spring.rabbitmq.port=${RABBIT_PORT:5672}
spring.rabbitmq.username=${RABBIT_USERNAME:myuser}
spring.rabbitmq.password=${RABBIT_PASSWORD:secret}

spring.cloud.compatibility-verifier.enabled=false

spring.security.oauth2.client.registration.google.client-id=<your-google-client-id>
spring.security.oauth2.client.registration.google.client-secret=<your-google-client-secret>
spring.security.oauth2.client.registration.google.redirect-uri=http://localhost:8010/login/oauth2/code/google

springdoc.api-docs.path=/auth/v3/api-docs
springdoc.swagger-ui.path=/auth/swagger-ui.html
```
*Add your google client id and google client secret key, otherwise contact me to get my own keys*


3. Follow the steps on releases tab (from step 2)


*Note: if you want to change environmental variables to docker compose files you should override* `.env.example` *files I added as example and rename them to* `.env` *. Do this with your own risk, because it can make cascade changes. By default all of the services have dummy values that are only for demo purposes.*

#### ⚠️ Known issues
  - Bad responsiveness on small screens
  - At the beginning the '500 Error' may occur
      - *Reason #1: some of the critical services (profile/auth) weren't registered by gateway*
      - *Fix #1: Wait for a short period of time (approx. 2 minutes) and then try again*
      - *Reason #2: some critical services (gateway/profile/auth) have problems with health*
      - *Fix #2: Check if all of the containers are up and running, afterwards rerun the server container*

## Current Status

#### 👨🏻‍💻 Stable version 1.0 released. Developing new functionality and improving old ones for future releases

## Demo Videos
### 1️⃣ Authentication, Profile & Social Interaction (1:55)
<video src="https://github.com/user-attachments/assets/46cb272e-8198-4011-b64f-e99684463583" controls width="600"></video>


### 2️⃣ Chatting and Notifications (1:22)
<video src="https://github.com/user-attachments/assets/920913f0-12f9-42e0-9552-c1601e1e039b" controls width="600"></video>


### 3️⃣ Managing Posts, Comments and Stories (2:46)
<video src="https://github.com/user-attachments/assets/b67cc21e-a24d-4c80-8121-efba593004ee" controls width="600"></video>


#### ⚠️ Note on image loading speed
> Images in the demo load slowly due to LocalTunnel + custom S3-like storage (no CDN).
In a production setup (e.g., AWS S3 + CloudFront), images load instantly.

## Tech Stack
- **Client**: React.js, ShadCN, TailwindCSS

- **Server**: Spring Boot (for most services), Django (only for posts service)

- **Communication**: REST, WebSocket, RabbitMQ

- **Database**: MongoDB, PostgreSQL, Neo4J, Redis

- **Cache**: Redis

- **Search Engine**: ElasticSearch (primary), with Neo4J as fallback

- **Web Server**: nginx (in plans)

- **Logs**: ELK Stack (in plans)

## Project Structure 
*The system follows a microservices architecture, where each core functionality (profiles, posts, chat, etc.) is isolated into its own service.*

### Backend Architecture
  - [Simplified Project Structure](#simplified-project-structure)
  - [Profile Service](#profile-service)
  - [Posts Service](#posts-service)
  - [Feed Service](#feed-service)
  - [Auth Service](#auth-service)
  - [Chat Service](#chat-service)
  - [Stories Service](#stories-service)
  - [Notification Service](#notification-service)
  - [Image storage Service](#image-storage-service)

#### Simplified Project Structure
<img width="4920" height="2985" alt="CompleteProjectArchitecture" src="https://github.com/user-attachments/assets/11a380cf-a812-461b-aef4-762f102596f2" />

#### Profile Service 
*🛑 Critical Service*
<img width="4517" height="3104" alt="ProfileService" src="https://github.com/user-attachments/assets/d3b39012-b332-4413-a9f6-4503ffbd5eca" />

#### Posts Service
<img width="4980" height="2860" alt="PostsService" src="https://github.com/user-attachments/assets/16075c8b-f6ba-41d1-8b44-f4f96c98d1da" />

#### Feed Service
*⚠️ Dependent on Profile and Posts service*
<img width="4315" height="2000" alt="FeedService" src="https://github.com/user-attachments/assets/b2d26a72-0a0d-4cc2-80ee-b9a7a99c4664" />

#### Auth Service
<img width="4402" height="2609" alt="AuthService" src="https://github.com/user-attachments/assets/a2526584-3579-444f-a870-fd7f8425bdc9" />

#### Chat Service
<img width="4359" height="2322" alt="ChatService" src="https://github.com/user-attachments/assets/94935272-0df9-4ac5-9ad1-6d6e8b4a41ac" />

#### Stories Service
<img width="4014" height="2379" alt="StoriesService" src="https://github.com/user-attachments/assets/2bc4d761-f81e-408c-b541-72a09ac7341d" />

#### Notification Service
<img width="3918" height="2557" alt="NotificationService" src="https://github.com/user-attachments/assets/a6910e3b-a72f-4205-8a0c-8df51d538585" />

#### Image storage Service
![ImageStorageService](https://github.com/user-attachments/assets/08e6f7bb-bc8f-4cb8-86aa-bc4d902a8a09)

*[Excalidraw](https://excalidraw.com/) was used for illustrations*


## License
[Apache License 2.0](./LICENSE)
