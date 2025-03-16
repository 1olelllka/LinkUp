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

- **Database**: MongoDB, PostgreSQL, Neo4J, Redis, Amazon S3 (in plans)

- **Cache**: Redis

- **Search Engine**: ElasticSearch, Neo4J (in case of ElasticSearch's failure)

- **Web Server**: nginx (in plans)

- **CDN**: Cloudflare (in plans)

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

#### Simplified Project Structure
![CompleteProjectArchitecture](https://github.com/user-attachments/assets/361da87f-51a9-426e-9855-8d76a5c1d619)

#### Profile Service 
*🛑 Critical Service*
![ProfileService](https://github.com/user-attachments/assets/548e812b-d985-4dfa-8fdd-5bb9f9152b52)

#### Posts Service
![PostsService](https://github.com/user-attachments/assets/366e1074-eb2b-45a4-b768-d0dfe4f85c19)

#### Feed Service
*⚠️ Dependent on Profile and Posts service*
![FeedService](https://github.com/user-attachments/assets/6c921ac1-5368-415b-8372-e3bdb0418067)

#### Auth Service
![AuthService](https://github.com/user-attachments/assets/1c40e8da-2ded-4710-9859-3b056e392ca2)

#### Chat Service
![ChatService](https://github.com/user-attachments/assets/ae6abfff-a324-4305-bbb8-d238800fde5c)

#### Stories Service
![StoriesService](https://github.com/user-attachments/assets/4830ad49-696f-49f0-b7da-cb593740538a)

#### Notification Service
![NotificationService](https://github.com/user-attachments/assets/f060a00f-21da-4f4f-8369-8c0878e85b23)

*[Excalidraw](https://excalidraw.com/) was used for illustrations*

## Current Status

#### 🚧 Development in Progress 🚧

I am actively working on core functionalities, and the platform is not yet available for public use.

## License
[Apache License 2.0](https://choosealicense.com/licenses/apache-2.0/)
