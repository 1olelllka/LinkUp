# LinkUp!

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

A distributed social platform built to explore microservices architecture, polyglot persistence, and event-driven communication patterns.

<img width="1440" height="829" alt="Landing Page LinkUp!" src="https://github.com/user-attachments/assets/768df65f-1fca-4ff7-8cac-5f745aa235be" />

> Note: Demo videos are available on [Wiki](https://github.com/1olelllka/LinkUp/wiki/LinkUp!-v2.0-Release-Documentation).

## What is this

LinkUp! splits a social platform — auth, profiles, posts, feeds, stories, chat, notifications, image storage — across ten independently deployable services behind a single API Gateway. Each service uses whichever data store fits its access pattern (PostgreSQL for relational post data, Neo4j for the social graph, Redis for feed caching) rather than sharing one database. Services talk to each other through a mix of synchronous REST calls and asynchronous events over RabbitMQ, with a Discovery Service handling registration and health checks.

## Tech Stack

| | |
|---|---|
| **Backend** | Java (Spring Boot), Python (Django) |
| **Frontend** | React, TypeScript, Tailwind CSS, shadcn/ui |
| **Data** | PostgreSQL, MongoDB, Neo4j, Redis |
| **Messaging** | RabbitMQ |
| **Infra** | Docker, Docker Compose, GitHub Actions |

## Quick Start

### Via `git clone`
```bash
git clone https://github.com/1olelllka/LinkUp.git
cd LinkUp
docker compose up
```

- Frontend: `http://localhost:5173`
- Gateway: `http://localhost:8080`

### Via "Releases"

> Coming soon

That's enough to explore the app. For Google OAuth, image uploads (via LocalTunnel), and the demo data population script, see [Setup in the Wiki](https://github.com/1olelllka/LinkUp/wiki/LinkUp!-v2.0-Release-Documentation).

## Architecture

![LinkUpArchitectureOverview](https://github.com/user-attachments/assets/fa8beec9-68f7-49f5-89d4-e53b3c29ca31)

Full dependency graph, event-driven message flow, and per-service data ownership: see the [Wiki](https://github.com/1olelllka/LinkUp/wiki/LinkUp!-v2.0-Release-Documentation).

## Status

This is a student/portfolio project, not production-hardened. There's no permanent live deployment — an eleven-service, five-datastore stack isn't realistically free to host continuously — but the wiki has full setup instructions and recorded demo walkthroughs.

## Documentation

- [Wiki Home](https://github.com/1olelllka/LinkUp/wiki) — release notes, full architecture diagrams, setup guide, demo recordings

## License

This project is licensed under the **GNU General Public License v3.0** — see [LICENSE](LICENSE) for the full text.
