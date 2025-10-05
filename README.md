# LinkUp!
![Status](https://img.shields.io/badge/status-in%20development-yellow)
![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)  
*LinkUp! is a social networking platform designed to connect users through posts, stories, chat, and real-time notifications. Built with a microservices architecture, it emphasizes scalability, resilience, and modern frontend design.*

## Table of Contents
 - [Releases](#releases)
    - [0.98-BETA](#-098-beta)
 - [Current Status](#current-status)
 - [Tech Stack](#tech-stack)
 - [Project Structure](#project-structure)
    - [Backend Architecture](#backend-architecture)
 - [License](#license)

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

### How to Run 0.98-BETA
**Prerequisites:**  
- Docker & Docker Compose installed  
- Sufficient RAM for chosen version (5.5GB full / 3.4GB demo)

**Steps**
1. Download the 0.98-BETA Release archives (they should start with *linkup_0.98-beta_...*) from *Releases Tab* and unzip it
   - Create folder where you want to download all zip archives
   - Unzip frontend archive
```bash
unzip linkup_0.98-beta_frontend_and_readme.zip
```
   - Then, run a bash script to unarchive backend
```bash
   ./unzip_backend.sh
```
2. **Start backend services** using the bash script:  
```bash
  cd backend
  ./run_full_backend.sh   # OR ./run_demo_backend
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
    # Install Node (if not preinstalled)
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
```

8. In order to stop frontend run this command inside of **frontend folder**
```bash
  # Given you're inside of frontend folder
  docker compose down
```

**Note: if you encounter permission errors while running bash scripts, run this command**
```bash
   chmod +x <name_of_bash_script>.sh
```

#### ⚠️ Known issues
  - Bad responsiveness on small screens
  - At the beginning the '500 Error' may occur
      - *Reason #1: some of the critical services (profile/auth) weren't registered by gateway*
      - *Fix #1: Wait for a short period of time (approx. 2 minutes) and then try again*
      - *Reason #2: some critical services (gateway/profile/auth) have problems with health*
      - *Fix #2: Check if all of the containers are up and running, afterwards rerun the server container*

## Current Status

#### 🚧 Development in Progress 🚧

## Tech Stack
- **Client**: React.js, ShadCN, TailwindCSS

- **Server**: Spring Boot (for most services), Django (only for posts service)

- **Communication**: REST, WebSocket, RabbitMQ

- **Database**: MongoDB, PostgreSQL, Neo4J, Redis

- **Cache**: Redis

- **Search Engine**: ElasticSearch (primary), with Neo4J as fallback

- **Web Server**: nginx (in plans)

- **CDN**: Cloudflare (if public domain available)

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
