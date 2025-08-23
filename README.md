# LinkUp!
![Status](https://img.shields.io/badge/status-in%20development-yellow)
![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)  
*LinkUp! is a social networking platform designed to connect users through posts, stories, chat, and real-time notifications. Built with a microservices architecture, it emphasizes scalability, resilience, and modern frontend design.*

## Table of Contents
 - [Tech Stack](#tech-stack)
 - [Project Structure](#project-structure)
 - [Current Status](#current-status)
 - [License](#license)

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

*(it will be updated if changes occur)*

### Architecture
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
![CompleteProjectArchitecture](https://github.com/user-attachments/assets/348d2da5-7a2b-493d-b5e9-c098ba759d31)

#### Profile Service 
*🛑 Critical Service*
<img width="4555" height="2882" alt="ProfileService" src="https://github.com/user-attachments/assets/5ef301bf-fe1c-4c97-81eb-03b7ada587a6" />

#### Posts Service
![PostsService](https://github.com/user-attachments/assets/7535f4ff-28a5-40ab-a8cd-d1849a4ee389)

#### Feed Service
*⚠️ Dependent on Profile and Posts service*
![FeedService](https://github.com/user-attachments/assets/6c921ac1-5368-415b-8372-e3bdb0418067)

#### Auth Service
<img width="2935" height="1739" alt="AuthService" src="https://github.com/user-attachments/assets/6f426fde-9361-4512-9d8e-6d6a79d48a3f" />

#### Chat Service
<img width="4339" height="2187" alt="ChatService" src="https://github.com/user-attachments/assets/607225da-4649-49a5-b596-1a2c0af55d1a" />

#### Stories Service
<img width="4270" height="3027" alt="StoriesService" src="https://github.com/user-attachments/assets/4c25603e-9741-45f7-9dcb-64d851f0ce54" />

#### Notification Service
<img width="3919" height="2557" alt="NotificationService" src="https://github.com/user-attachments/assets/d9b066cd-c944-43d7-acde-e72ca05c8fbf" />

#### Image storage Service
![ImageStorageService](https://github.com/user-attachments/assets/08e6f7bb-bc8f-4cb8-86aa-bc4d902a8a09)

*[Excalidraw](https://excalidraw.com/) was used for illustrations*

## Current Status

#### 🚧 Development in Progress 🚧

- Backend: feature-complete (core services implemented, security hardening in progress)  
- Frontend: implements all core functionalities, polishing in progress (error handling, UX improvements)

## License
[Apache License 2.0](./LICENSE)
