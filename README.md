# wc26-backend

Ktor backend for the **FIFA World Cup 2026 Fan Social App**. This project serves as a robust, containerized, and feature-rich API service powering social interactions, match tracking, and real-time push notifications for football fans.

---

## Architecture & Tech Stack

The application is built using a modern, reactive Kotlin stack with a modular, feature-oriented package structure.

- **Framework:** [Ktor 2.x](https://ktor.io/) (utilizing the high-performance Netty engine)
- **Database:** PostgreSQL 16
- **SQL Framework & ORM:** [JetBrains Exposed](https://github.com/JetBrains/Exposed) (Kotlin-first SQL library)
- **Database Migrations:** [Flyway](https://flywaydb.org/) (runs migrations automatically on server startup)
- **Push Notifications:** Firebase Admin SDK for Firebase Cloud Messaging (FCM)
- **Security:** JWT (JSON Web Tokens) with separate `user` and `admin` scopes
- **Containerization:** Docker & Docker Compose
- **Build Tool:** Gradle (Kotlin DSL) with ShadowJar for fat JAR packaging

---

## Project Structure

The project organizes code by features rather than layers, making components cohesive and easy to locate:

```
src/main/kotlin/com/adel/
├── Application.kt                # Application entrypoint (EngineMain)
├── common/                       # Shared modules
│   ├── database/                 # Common database query utility helpers
│   ├── security/                 # Password hashing (BCrypt) & JWT/UserId retrieval helpers
│   ├── pagination/               # DTO and server-side cursor/offset paginated results
│   └── data/                     # Global data transfer objects (health response, build info)
├── config/                       # Application config loaders (database, JWT, AppConfig)
├── plugins/                      # Ktor-specific configuration modules
│   ├── DatabaseConfig.kt         # Exposed DB connection pool initialization
│   ├── FirebaseConfig.kt         # Firebase Admin SDK setup
│   ├── Authentication.kt         # JWT auth schemes setup (User/Admin routes protection)
│   ├── Http.kt                   # CORS, default headers, and general HTTP settings
│   ├── Serialization.kt          # Content negotiation using kotlinx.serialization JSON
│   ├── StatusPages.kt            # Global exception mapper & response builder
│   └── Routing.kt                # Application routes orchestration and dependency injection
└── features/                     # Domain features containing their respective routes, services, tables, repositories
    ├── auth/                     # Register, login, and token generation
    ├── users/                    # Public profiles lookup and admin-level role updates
    ├── matches/                  # Tournament schedule, live scoreboards, stage designations
    ├── posts/                    # Social posts associated with matches (cursor-based pagination)
    ├── likes/                    # Post like actions, list of likers, and liked posts
    ├── comments/                 # Match-specific posts comment sections (likes & deletions)
    ├── notifications/            # Push token registrations & in-app notification feed log
    └── system/                   # Application status config (maintenance mode toggling & version checks)
```

---

## Local Development & Quick Start

### Prerequisites
- **JDK 21**
- **Docker** and **Docker Compose**
- **Firebase Service Account Key** (optional, required for sending FCM push notifications)

### 1. Spin up Database and Services
Start the PostgreSQL database and pgAdmin using Docker Compose:
```bash
docker compose up -d
```
This boots up:
- **PostgreSQL 16** listening on `localhost:5432` (DB: `wc26`, User: `wc26_user`)
- **pgAdmin 4** listening on `localhost:5050` (Email: `admin@wc26.com`, Password: `admin`)

### 2. Configure Firebase Admin SDK (Optional)
Download your Firebase project's service account JSON key file, place it in the project root directory, and name it `service-account-key.json` (or set `FIREBASE_KEY_PATH` to point to its path).

### 3. Run the Dev Server
Launch the Ktor application locally:
```bash
./gradlew run
```
The API server will listen on `http://localhost:8080`. You can test it by visiting the root healthcheck endpoint:
```bash
curl http://localhost:8080/
```
Response format:
```json
{
  "service": "wc26-backend",
  "status": "ok",
  "version": "2.4",
  "timestamp": "2026-06-19T20:00:00Z"
}
```

---

## ⚙️ Environment Configurations

The application reads configuration parameters from `src/main/resources/application.yaml`. The following environment variables can override the defaults:

| Environment Variable | Default Value | Description |
| :--- | :--- | :--- |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/wc26` | JDBC connection URL for PostgreSQL |
| `DATABASE_USER` | `wc26_user` | Database connection username |
| `DATABASE_PASSWORD` | `wc26_dev_password` | Database connection password |
| `JWT_SECRET` | `dev-secret-do-not-use-in-production...` | Secret key used to sign JWT tokens |
| `JWT_ISSUER` | `wc26-backend` | Issuer string embedded in the JWTs |
| `JWT_AUDIENCE` | `wc26-clients` | Intended audience of the JWTs |
| `FIREBASE_KEY_PATH` | `service-account-key.json` | Path to the Firebase service account JSON key file |

---

## Authentication & Role Protection

The backend secures client requests using JWT.
1. Authenticate or register using `POST /auth/login` or `POST /auth/register`.
2. Include the returned token in the headers of subsequent requests:
   ```http
   Authorization: Bearer <your_jwt_token>
   ```

### Roles:
- **`user`**: Standard user profile. Allowed to create/delete posts, like posts/comments, comment, and register FCM push tokens.
- **`admin`**: System administrator. Allowed to update match details (scores, statuses, and teams), manage system status settings, get the full list of users, and change user roles.

---

## System Configuration & Maintenance Interceptor

The application installs `SystemStatusInterceptor`, a custom middleware plugin that intercepts incoming requests to enforce rules:
- **Maintenance Mode:** When enabled, the backend intercepts all non-bypassed requests and returns `503 Service Unavailable` with a maintenance payload.
- **Android Version Lock:** Mobile clients (identified by `User-Agent` or the `X-App-Version` header) are checked against the minimum required version. If outdated, the interceptor responds with `426 Upgrade Required` containing the update URL.
- *Note:* `/system-status`, `/admin/system-status`, and `/` endpoints bypass this interceptor.

---

## API Reference

### 1. Authentication & Profile (`/auth`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/auth/register` | Public | Register a new user. Returns JWT and user details. |
| `POST` | `/auth/login` | Public | Login with email and password. Returns JWT and user details. |
| `GET` | `/auth/me` | User | Get the currently authenticated user's profile. |
| `PATCH` | `/auth/me` | User | Update own profile details (`displayName`, `avatarUrl`, `bio`). |

### 2. User Management (`/users`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/users/{id}` | Public | Retrieve public profile details of another user. |
| `GET` | `/users` | Admin | List all registered users in the database. |
| `PATCH` | `/users/{id}` | Admin | Edit any user's profile fields or upgrade their role (`admin`/`user`). |

### 3. Match Tracking (`/matches` & `/admin/matches`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/matches` | Public | List match schedule. Filters: `status` (`scheduled`/`live`/`finished`), `stage`, `limit`, `offset`. |
| `GET` | `/matches/{id}` | Public | Get details of a single match. |
| `PATCH` | `/admin/matches/{id}` | Admin | Update match score, status, or knockout teams. |

### 4. Posts Feed (`/posts`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/posts` | Optional | Scroll feed of all posts (cursor-based pagination, matches pre-loaded in batch). |
| `GET` | `/matches/{matchId}/posts` | Optional | Retrieve posts associated with a specific match. |
| `GET` | `/users/{userId}/posts` | Optional | Get posts authored by a specific user. |
| `GET` | `/posts/{id}` | Optional | Get single post details. |
| `POST` | `/matches/{matchId}/posts` | User | Create a post (max 500 characters). |
| `DELETE` | `/posts/{id}` | User | Delete a post (owner only). |

### 5. Likes System (`/posts/{postId}/likes`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/posts/{postId}/like` | User | Like a post. Dispatches FCM notification to the post's author. |
| `DELETE` | `/posts/{postId}/like` | User | Unlike a post. |
| `GET` | `/posts/{postId}/likes` | Public | List all users who liked the post (cursor pagination). |
| `GET` | `/users/{userId}/likes` | Public | List all posts liked by a user. |

### 6. Comments System (`/posts/{postId}/comments` & `/comments`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `POST` | `/posts/{postId}/comments` | User | Post a comment on a post (max 300 characters). Dispatches FCM notification to post author. |
| `GET` | `/posts/{postId}/comments` | Public | List comments on a post (offset pagination). |
| `DELETE` | `/comments/{id}` | User | Delete a comment (owner only). |
| `POST` | `/comments/{commentId}/like` | User | Like a comment. |
| `DELETE` | `/comments/{commentId}/like` | User | Unlike a comment. |
| `GET` | `/comments/{commentId}/likes` | Public | List users who liked a comment. |

### 7. FCM Push Tokens & Notifications (`/notifications`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/notifications` | User | Retrieve authenticated user's notification feed (offset pagination). |
| `GET` | `/notifications/unread-count` | User | Get the count of unread notifications. |
| `POST` | `/notifications/read` | User | Mark all of the user's notifications as read. |
| `PATCH` | `/notifications/{id}/read` | User | Mark a single notification as read. |
| `POST` | `/users/push-token` | User | Register a device FCM token for push notifications. |
| `DELETE` | `/users/push-token` | User | Unregister a device FCM token. |

### 8. System Config (`/system-status`)
| Method | Endpoint | Auth | Description |
| :--- | :--- | :---: | :--- |
| `GET` | `/system-status` | Public | Get current system configs (`min_android_version`, `maintenance_mode`, `android_update_url`). |
| `PATCH` | `/admin/system-status` | Admin | Update system configs. |

---

## Testing

The repository includes a comprehensive integration test suite.
To execute tests locally, ensure your database container is running (`docker compose up -d` mapping host port `5432`) and execute:
```bash
./gradlew test
```
The test suite validates:
- Endpoint accessibility and serialization format
- Auth token checks and roles enforcement
- Mobile client header validation and version/maintenance interchanging
- Push token registration cycles