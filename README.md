# 🚀 Nexus Chat Application

A modern, real-time chat application built with **Spring Boot** and **WebSockets**, featuring group messaging, direct messages, AI-powered bot integration, file sharing, user profiles, and ephemeral messaging.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green) ![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-blue) ![H2](https://img.shields.io/badge/Database-H2-yellow)

---

## ✨ Features

### 💬 Real-Time Messaging
- **Group Chat** — Create groups with custom emoji icons, descriptions, and manage members
- **Direct Messages (DM)** — One-on-one private conversations with any user
- **@Mentions** — Tag users with `@username` to notify them; highlighted in gold
- **AI Bot** — Mention `@bot` in any chat to get AI-powered responses via Claude API

### 📎 File Sharing
- Share **images, videos, documents** (`.pdf`, `.docx`, `.txt`, `.csv`, `.xlsx`, `.pptx`, etc.)
- **2 MB file size limit** enforced on both client and server
- **Inline previews** for images and videos
- **Image lightbox** — Click any image to view it full-screen
- **Download button** for document attachments
- **Drag & drop** file upload support

### ⏱ Ephemeral Messages (30-Minute TTL)
- Every message (text or file) automatically **expires after 30 minutes**
- **Live countdown timer** displayed on each message
- Messages fade visually when **< 5 minutes** remain
- Expired messages are **permanently deleted** from the database via a scheduled cleanup job
- Messages also **disappear from the UI in real-time** when their TTL expires

### 👤 User Profiles
- **Profile setup on first login** — Users are prompted to complete their profile when creating an account
- Profile fields:
  - 📷 **Profile Picture** (visible to all users, shown in avatars)
  - ✏️ **Nickname**
  - 📧 **Email** (required)
  - 📱 **Phone Number** (optional)
  - 🔗 **LinkedIn Profile URL** (optional)
  - 📍 **Address** (optional)
- **View any user's profile** by clicking their name or avatar in chat
- **Edit your own profile** anytime by clicking your profile card in the sidebar
- Profile pictures appear in the sidebar, chat messages, and autocomplete suggestions

### 🎨 UI/UX
- Beautiful **dark theme** with gradient accents
- **Responsive layout** with sidebar and main chat area
- **Real-time presence indicators** — See who's online/offline
- **Unread message badges** on groups and DMs
- **Toast notifications** for events (new messages, errors, etc.)
- **Autocomplete** for `@mentions` with user avatars
- **Date dividers** in chat history

---

## 🛠 Tech Stack

### ☕ Backend

| Layer | Technology | Details |
|---|---|---|
| **Language** | Java 17 | LTS version |
| **Framework** | Spring Boot 3.2.0 | Parent framework |
| **REST API** | Spring Web (MVC) | `spring-boot-starter-web` |
| **Real-time** | Spring WebSocket + STOMP | `spring-boot-starter-websocket` |
| **Security** | Spring Security | `spring-boot-starter-security` — session-based auth |
| **ORM / DB Access** | Spring Data JPA (Hibernate) | `spring-boot-starter-data-jpa` |
| **Database** | H2 (in-memory) | Embedded, runtime-scoped |
| **Reactive HTTP Client** | Spring WebFlux (`WebClient`) | Used for calling Claude AI API |
| **JSON** | Jackson Databind | Serialization/deserialization |
| **Boilerplate Reduction** | Lombok | Annotations for getters, builders, etc. |
| **Build Tool** | Maven | `pom.xml` based |

### 🤖 AI Integration

| Service | Details |
|---|---|
| **Claude AI** (Anthropic) | Via `ClaudeService.java` using `WebClient` (WebFlux) — triggered by `@bot` mentions |

### 🌐 Frontend

| Layer | Technology | Details |
|---|---|---|
| **Markup** | HTML5 | Single-page `index.html` served as static resource |
| **Styling** | Vanilla CSS | CSS custom properties (variables), no framework |
| **Fonts** | Google Fonts | `DM Sans` + `Space Mono` |
| **Real-time (client)** | SockJS 1.6.1 | WebSocket fallback transport |
| **Messaging Protocol** | STOMP.js 2.3.3 | Pub/sub messaging over WebSocket |
| **Logic** | Vanilla JavaScript (ES6+) | No frontend framework (no React/Vue/Angular) |

### 🏗️ Architecture

- **Monolithic** Spring Boot app serving both backend APIs and the frontend static file
- **Session-based authentication** (Spring Security + HTTP session cookies)
- **STOMP over WebSocket** for real-time group and DM messaging
- **In-memory H2 database** — data resets on restart (no persistence)
- **Single `index.html` SPA** — no bundler, no build step on the frontend

---

## 🚀 Getting Started

### Prerequisites
- **Java 17+**
- **Maven 3.8+**

### Run the Application

```bash
# Clone the repository
git clone https://github.com/your-username/nexus-chat-app.git
cd nexus-chat-app

# Build and run
mvn spring-boot:run
```

Open your browser at **[http://localhost:8080](http://localhost:8080)**

### Demo Accounts

| Username | Password   | Display Name |
|----------|------------|--------------|
| `alice`  | `password` | Alice Chen   |
| `bob`    | `password` | Bob Kumar    |
| `carol`  | `password` | Carol Smith  |
| `dave`   | `password` | Dave Patel   |

---

## 📁 Project Structure

```
src/main/java/com/chatapp/
├── NexusChatApplication.java        # Main application entry point
├── config/
│   ├── DataInitializer.java         # Demo account seeding
│   ├── SecurityConfig.java          # Spring Security configuration
│   └── WebSocketConfig.java         # STOMP WebSocket configuration
├── controller/
│   ├── AuthController.java          # Login, register, logout, session
│   ├── ChatRestController.java      # REST APIs for groups, DMs, profiles
│   └── WebSocketController.java     # Real-time message handling
├── dto/
│   └── ChatDTOs.java                # Data transfer objects
├── model/
│   ├── Group.java                   # Group entity
│   ├── Message.java                 # Message entity (with file & TTL fields)
│   └── User.java                    # User entity (with profile fields)
├── repository/
│   ├── GroupRepository.java
│   ├── MessageRepository.java       # Includes TTL cleanup queries
│   └── UserRepository.java
└── service/
    ├── ChatService.java             # Core business logic + scheduled cleanup
    ├── ClaudeService.java           # AI bot integration
    └── CustomUserDetailsService.java
```

---

## 🔧 Configuration

Key settings in `application.properties`:

| Property | Default | Description |
|----------|---------|-------------|
| `server.port` | `8080` | Server port |
| `claude.api.key` | — | Anthropic API key for AI bot |
| `spring.servlet.multipart.max-file-size` | `2MB` | Max file upload size |
| `server.servlet.session.timeout` | `24h` | Session timeout |

### AI Bot Setup
Set your Claude API key:
```bash
# Via environment variable
export CLAUDE_API_KEY=your-api-key-here
```
Or update `application.properties` directly.

---

## 📝 API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register new user with profile |
| POST | `/api/auth/login` | Login |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Get current user info |

### Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | List all users |
| GET | `/api/groups` | List user's groups |
| POST | `/api/groups` | Create group |
| POST | `/api/groups/{id}/members` | Add member to group |
| GET | `/api/groups/{id}/messages` | Get group message history |
| GET | `/api/dm/{username}/messages` | Get DM history |

### Profile
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/profile/{username}` | View user profile |
| GET | `/api/profile/me` | Get own profile |
| PUT | `/api/profile` | Update own profile |

### WebSocket
| Destination | Description |
|-------------|-------------|
| `/app/chat.send` | Send message (text + optional file) |
| `/app/presence` | Announce online/offline status |
| `/topic/group/{id}` | Subscribe to group messages |
| `/user/queue/dm` | Receive direct messages |
| `/topic/presence` | Receive presence updates |

---

## 📜 License

This project is open-source and available under the [MIT License](LICENSE).
