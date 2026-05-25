<div align="center">

# 💬 Nexus Chat

**A full-featured real-time chat application**

Built with Spring Boot · WebSockets · JWT Auth · Claude AI

[![Java](https://img.shields.io/badge/Java-17+-orange?logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

</div>

---

## 📸 Overview

Nexus is a production-ready real-time chat application featuring group chats, direct messages, AI-powered bot assistance, message reactions, rich user profiles, and a polished responsive dark-themed UI — all in a single deployable JAR.

---

## ✨ Features at a Glance

| Category | Features |
|----------|----------|
| **💬 Messaging** | Real-time group & DM chat, message reactions (👍❤️😂😮😢🔥🎉👀), reply/thread quoting, edit & delete, pinned messages, file attachments (≤2 MB with inline preview), configurable message TTL, @mentions with autocomplete, typing indicators, full-text search |
| **👥 Groups** | Admin roles (creator + promoted admins), private/public groups, group settings (name, TTL, privacy), remove/leave members, invite links with expiry, member picker |
| **🤖 AI Bot** | Claude AI integration via `@bot`, context-aware responses, one-click group conversation summarizer |
| **👤 Profiles** | Display name, nickname, email, phone, LinkedIn, address, profile picture, user status (Active/Away/DND/Invisible), custom status message |
| **🔐 Security** | JWT authentication + session fallback, bcrypt password hashing, password change, account deletion, rate limiting (30 msg/min), input validation |
| **🔔 Notifications** | @mention toast alerts, DM notifications, unread badge counters |
| **📱 UI/UX** | Responsive mobile layout with slide-out sidebar, dark theme, drag-and-drop upload, image lightbox, date dividers, expiry animations |

---

## 🛠 Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 17, Spring Boot 3.2, Spring Security, Spring WebSocket (STOMP/SockJS), Spring Data JPA |
| **Auth** | JWT ([jjwt 0.12](https://github.com/jwtk/jjwt)) + HTTP session fallback |
| **Database** | H2 (in-memory, default for dev) · PostgreSQL (production-ready driver included) |
| **AI** | Anthropic Claude API (Haiku model) via Spring WebFlux |
| **Frontend** | Vanilla HTML / CSS / JavaScript, SockJS, STOMP.js |
| **Build** | Apache Maven |

---

## 🚀 Running Locally

### Prerequisites

| Requirement | Version | Download |
|-------------|---------|----------|
| **JDK** | 17 or higher | [Adoptium](https://adoptium.net/) or [Oracle](https://www.oracle.com/java/technologies/downloads/) |
| **Maven** | 3.8+ | [maven.apache.org](https://maven.apache.org/download.cgi) _(or use your IDE's bundled Maven)_ |
| **Git** | Any | [git-scm.com](https://git-scm.com/downloads) _(optional — you can download the ZIP instead)_ |

> **Verify installations:**
> ```bash
> java -version    # should show 17+
> mvn -version     # should show 3.8+
> ```

---

### Step 1 — Clone the Repository

```bash
git clone https://github.com/your-username/nexus-chat-app.git
cd nexus-chat-app
```

---

### Step 2 — Database Configuration

Nexus ships with **two database options** out of the box. Choose the one that fits your needs:

#### Option A: H2 In-Memory Database (Default — Zero Setup)

This is pre-configured and requires **no installation**. The database lives in memory and resets on every restart. Perfect for quick demos and development.

The default settings in `src/main/resources/application.properties` already use H2:

```properties
spring.datasource.url=jdbc:h2:mem:nexuschat;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
```

- **H2 Console** is available at [http://localhost:8080/h2-console](http://localhost:8080/h2-console) for inspecting tables.
- Connect with JDBC URL: `jdbc:h2:mem:nexuschat`, username: `sa`, password: _(empty)_.

> ⚠️ **Note:** All data is lost when the app restarts. Use PostgreSQL for persistent data.

#### Option B: PostgreSQL (Persistent — Recommended for Production)

1. **Install PostgreSQL** — [postgresql.org/download](https://www.postgresql.org/download/)

2. **Create the database:**
   ```bash
   # Connect to PostgreSQL
   psql -U postgres

   # Inside the psql shell:
   CREATE DATABASE nexuschat;
   CREATE USER nexususer WITH PASSWORD 'nexuspass';
   GRANT ALL PRIVILEGES ON DATABASE nexuschat TO nexususer;
   \q
   ```

3. **Update `application.properties`** — replace the H2 block with:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/nexuschat
   spring.datasource.driver-class-name=org.postgresql.Driver
   spring.datasource.username=nexususer
   spring.datasource.password=nexuspass
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false
   ```

4. **Disable the H2 console** (optional):
   ```properties
   spring.h2.console.enabled=false
   ```

> The PostgreSQL JDBC driver is already included in `pom.xml` — no dependency changes needed.

---

### Step 3 — Environment Variables (Optional)

Set these before running, or the app will start with defaults:

| Variable | Purpose | Required? | Default |
|----------|---------|-----------|---------|
| `PORT` | HTTP server port | No | `8080` |
| `CLAUDE_API_KEY` | Anthropic API key for the AI bot | No | _(bot disabled if unset)_ |
| `JWT_SECRET` | Secret key for signing JWT tokens | No | _(built-in fallback)_ |

**Linux / macOS:**
```bash
export CLAUDE_API_KEY="sk-ant-..."
export JWT_SECRET="my-production-secret-key-at-least-32-characters"
```

**Windows PowerShell:**
```powershell
$env:CLAUDE_API_KEY = "sk-ant-..."
$env:JWT_SECRET = "my-production-secret-key-at-least-32-characters"
```

**Windows CMD:**
```cmd
set CLAUDE_API_KEY=sk-ant-...
set JWT_SECRET=my-production-secret-key-at-least-32-characters
```

> 💡 If `CLAUDE_API_KEY` is not set, the `@bot` command will return a friendly "bot not configured" message instead of crashing.

---

### Step 4 — Build & Run

**Option 1 — Run with Maven (recommended for development):**
```bash
mvn spring-boot:run
```

**Option 2 — Build a JAR and run it:**
```bash
mvn clean package -DskipTests
java -jar target/nexus-chat-1.0.0.jar
```

You should see output ending with:
```
Started NexusChatApplication in X.XXX seconds
```

---

### Step 5 — Open the App

Navigate to **[http://localhost:8080](http://localhost:8080)** in your browser.

#### Demo Accounts

Four demo users are auto-created on startup (password for all: **`password`**):

| Username | Display Name | Avatar Color |
|----------|-------------|--------------|
| `alice` | Alice Chen | 🔴 Red |
| `bob` | Bob Kumar | 🔵 Blue |
| `carol` | Carol Smith | 🟢 Green |
| `dave` | Dave Patel | 🟠 Orange |

Click any `@username` pill on the login screen to sign in instantly.

---

## 📂 Project Structure

```
nexus-chat-app/
├── pom.xml                                    # Maven build config & dependencies
├── src/main/
│   ├── java/com/chatapp/
│   │   ├── NexusChatApplication.java          # Spring Boot entry point
│   │   ├── config/
│   │   │   ├── DataInitializer.java           # Seeds demo users on startup
│   │   │   ├── JwtUtil.java                   # JWT token create / validate
│   │   │   ├── JwtAuthFilter.java             # Extracts JWT from Authorization header
│   │   │   ├── RateLimiter.java               # In-memory sliding-window rate limiter
│   │   │   ├── SecurityConfig.java            # Spring Security filter chain
│   │   │   └── WebSocketConfig.java           # STOMP broker & endpoint config
│   │   ├── controller/
│   │   │   ├── AuthController.java            # /api/auth/* — login, register, password, delete
│   │   │   ├── ChatRestController.java        # /api/* — groups, messages, search, profile, stats
│   │   │   ├── InviteController.java          # /api/invite/* — invite link accept
│   │   │   └── WebSocketController.java       # /app/* — real-time send, typing, reactions, edit
│   │   ├── dto/
│   │   │   └── ChatDTOs.java                  # All request / response payload classes
│   │   ├── model/
│   │   │   ├── Group.java                     # Group with admins, privacy, configurable TTL
│   │   │   ├── GroupInvite.java               # Expiring invite token entity
│   │   │   ├── Message.java                   # Message with reply, edit, delete, pin support
│   │   │   ├── MessageReaction.java           # Emoji reaction (user × message × emoji)
│   │   │   ├── User.java                      # User with profile fields & status
│   │   │   ├── UserChannelState.java          # Per-user last-read timestamp for unreads
│   │   │   └── UserStatus.java                # Enum: ACTIVE, AWAY, DND, INVISIBLE
│   │   ├── repository/                        # Spring Data JPA repositories (6 files)
│   │   └── service/
│   │       ├── ChatService.java               # Core business logic (~400 lines)
│   │       ├── ClaudeService.java             # Anthropic Claude API client
│   │       └── CustomUserDetailsService.java  # Spring Security UserDetailsService
│   └── resources/
│       ├── application.properties             # All configuration
│       └── static/
│           └── index.html                     # Complete SPA frontend (~900 lines)
```

---

## 🔌 API Reference (Key Endpoints)

### Authentication
| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/auth/register` | Create account |
| `POST` | `/api/auth/login` | Sign in (returns JWT token) |
| `POST` | `/api/auth/logout?username=X` | Sign out |
| `GET` | `/api/auth/me` | Current user info |
| `PUT` | `/api/auth/password` | Change password |
| `DELETE` | `/api/auth/account` | Delete account |

### Groups
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/groups` | My groups |
| `GET` | `/api/groups/public` | Discoverable public groups |
| `POST` | `/api/groups` | Create group |
| `POST` | `/api/groups/:id/join` | Join a public group |
| `POST` | `/api/groups/:id/leave` | Leave group |
| `PUT` | `/api/groups/:id/settings` | Update group settings (admin) |
| `POST` | `/api/groups/:id/invite` | Generate invite link (admin) |
| `DELETE` | `/api/groups/:id/members/:user` | Remove member (admin) |

### Messages
| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/groups/:id/messages` | Group message history |
| `GET` | `/api/dm/:username/messages` | DM history |
| `GET` | `/api/groups/:id/pinned` | Pinned messages |
| `GET` | `/api/search?q=term` | Full-text search |
| `POST` | `/api/groups/:id/summarize` | AI summary |

### WebSocket (STOMP)
| Destination | Direction | Description |
|-------------|-----------|-------------|
| `/app/chat.send` | Client → Server | Send a message |
| `/app/typing` | Client → Server | Typing indicator |
| `/app/reaction` | Client → Server | Toggle emoji reaction |
| `/app/message.edit` | Client → Server | Edit own message |
| `/app/message.delete` | Client → Server | Delete own message |
| `/app/message.pin` | Client → Server | Pin/unpin message |
| `/topic/group/{id}` | Server → Client | New messages in group |
| `/user/queue/dm` | Server → Client | New DM messages |
| `/topic/presence` | Server → Client | Online/offline events |
| `/user/queue/notifications` | Server → Client | @mention alerts |

---

## 🌐 Deployment (Free Hosting — No Credit Card)

### 1. Render ⭐ Recommended

| | |
|-|-|
| **Free tier** | 750 hours/month, auto-sleep after 15 min inactivity |
| **Best for** | Simple GitHub-connected deploys |

```
1. Push code to GitHub
2. render.com → New → Web Service → connect repo
3. Build Command:   mvn clean package -DskipTests
4. Start Command:   java -jar target/nexus-chat-1.0.0.jar
5. Add env vars:    CLAUDE_API_KEY, JWT_SECRET
6. Click "Create Web Service"
```

### 2. Railway

| | |
|-|-|
| **Free tier** | $5 credit/month |
| **Best for** | Zero-config Java detection |

```
1. railway.app → New Project → Deploy from GitHub
2. Select repo → Railway auto-detects Java
3. Add env vars: CLAUDE_API_KEY, PORT=8080
4. Deploys automatically
```

### 3. Koyeb

| | |
|-|-|
| **Free tier** | 1 nano instance |
| **Best for** | Docker-based deploys |

Create a `Dockerfile` in the project root:
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/nexus-chat-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```
```
1. mvn clean package -DskipTests
2. koyeb.com → Create App → GitHub → Docker build
3. Set env vars → Deploy
```

### 4. Fly.io

| | |
|-|-|
| **Free tier** | 3 shared VMs |
| **Best for** | CLI-savvy developers |

```
1. Install: curl -L https://fly.io/install.sh | sh
2. fly auth signup
3. fly launch   (auto-detects Java)
4. fly secrets set CLAUDE_API_KEY=sk-ant-...
5. fly deploy
```

---

## ⚙️ Configuration Reference

All settings live in `src/main/resources/application.properties`:

```properties
# ── Server ──────────────────────────────────────────
server.port=${PORT:8080}
server.servlet.session.timeout=24h

# ── Database (H2 default — see README for PostgreSQL) ──
spring.datasource.url=jdbc:h2:mem:nexuschat;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# ── JWT Auth ────────────────────────────────────────
jwt.secret=${JWT_SECRET:nexus-chat-app-secret-key-...}
jwt.expiration=86400000                          # 24 hours in ms

# ── AI Bot ──────────────────────────────────────────
claude.api.key=${CLAUDE_API_KEY:}                # leave empty to disable

# ── File Uploads ────────────────────────────────────
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=5MB
spring.websocket.message-size-limit=3145728      # 3 MB (base64 overhead)

# ── Logging ─────────────────────────────────────────
logging.level.com.chatapp=INFO
logging.level.org.springframework.web.socket=WARN
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m "Add amazing feature"`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
