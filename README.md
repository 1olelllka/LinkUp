# LinkUp!
*A web-based social platform (In Development)*

## Table of Contents
 - [Tech Stack](#tech-stack)
 - [Project Structure](#project-structure)
 - [Current Status](#current-status)
 - [License](#license)

## Tech Stack
- **Client**: React.js (in plans)

- **Server**: Spring Boot (Java), Django (Python)

- **Communication**: REST, WebSocket, RabbitMQ

- **Database**: MongoDB, PostgreSQL, Neo4J, Redis

- **Cache**: Redis

- **Search Engine**: ElasticSearch, Neo4J (in case of ElasticSearch's failure)

- **Web Server**: nginx (in plans)

- **CDN**: Cloudflare (if public domain available)

- **Logs**: ELK Stack (in plans)

## Project Structure 
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
<img width="4555" height="2807" alt="ProfileService" src="https://github.com/user-attachments/assets/c8540289-cacb-4edd-9cae-ac0a34221b40" />

#### Posts Service
![PostsService](https://github.com/user-attachments/assets/7535f4ff-28a5-40ab-a8cd-d1849a4ee389)

#### Feed Service
*⚠️ Dependent on Profile and Posts service*
![FeedService](https://github.com/user-attachments/assets/6c921ac1-5368-415b-8372-e3bdb0418067)

#### Auth Service
<img width="2935" height="1739" alt="AuthService" src="https://github.com/user-attachments/assets/6f426fde-9361-4512-9d8e-6d6a79d48a3f" />

#### Chat Service
![ChatService](https://github.com/user-attachments/assets/23e2ff4e-6e05-4f15-a2fa-ae9da251a9dc)

#### Stories Service
![StoriesService](https://github.com/user-attachments/assets/4830ad49-696f-49f0-b7da-cb593740538a)

#### Notification Service
![NotificationService](https://github.com/user-attachments/assets/6c7b4054-cc8a-4f88-8491-7599be8dad79)

#### Image storage Service
![ImageStorageService](https://github.com/user-attachments/assets/08e6f7bb-bc8f-4cb8-86aa-bc4d902a8a09)

*[Excalidraw](https://excalidraw.com/) was used for illustrations*

## Current Status

#### 🚧 Development in Progress 🚧

I am actively working on core functionalities, and the platform is not yet available for public use.

## License
[Apache License 2.0](https://choosealicense.com/licenses/apache-2.0/)
