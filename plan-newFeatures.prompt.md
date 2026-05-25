# Plan: New Feature Ideas for Nexus Chat App

**Nexus Chat** is a Spring Boot + WebSocket real-time chat app with group chats, DMs, user profiles, file sharing (2MB), auto-expiring messages (30min TTL), a Claude AI bot (`@bot`), and online presence tracking. Below are creative features grouped by theme.

---

## 🗨️ Messaging Enhancements
1. **Message Reactions** — Add emoji reactions (👍❤️😂) to messages; store in a new `MessageReaction` entity and broadcast via WebSocket.
2. **Reply / Thread Quoting** — Allow replying to a specific message by adding a `replyToMessageId` FK on `Message`; show quoted preview in the UI.
3. **Message Edit & Delete** — Add `edited` flag and `deletedAt` to `Message`; broadcast edit/delete events over WebSocket.
4. **Read Receipts** — Track which users have seen each message with a `MessageRead` join table; send "seen by" indicators.
5. **Typing Indicators** — Broadcast a `TypingEvent` via WebSocket when a user is actively typing in a channel or DM.
6. **Message Search** — Add a `GET /api/search?q=` endpoint that queries message content across groups and DMs the user belongs to.
7. **Pinned Messages** — Add `pinned` flag on `Message`; expose a `GET /api/groups/{id}/pinned` endpoint and show pinned messages at the top of a channel.
8. **Configurable Message TTL** — Let users/admins set a custom expiry window per group (1h, 24h, 7d, never) instead of the hard-coded 30-minute TTL.

---

## 👥 Group & Channel Features
9. **Group Admin Roles** — Add an `AdminMembers` set on `Group` to differentiate owners, admins, and regular members; gate destructive actions.
10. **Private / Public Groups** — Add an `isPrivate` flag on `Group`; add `GET /api/groups/public` discovery endpoint so users can join open groups.
11. **Remove Member / Leave Group** — Add `DELETE /api/groups/{id}/members/{username}` and a self-leave endpoint.
12. **Group Announcements Channel** — A read-only sub-channel within a group where only admins can post; stored with a `channelType` field on `Message`.
13. **Group Invite Links** — Generate a one-time or expiring invite token that lets new users join a group without being manually added.

---

## 🤖 AI Bot Upgrades
15. **Slash Commands** — Parse `/summarize`, `/translate [lang]`, `/poll "question" "opt1" "opt2"` commands and route them to Claude or custom handlers.
16. **AI Message Summarizer** — Add a `POST /api/groups/{id}/summarize` endpoint that asks Claude to summarize the last N messages in a group.

---

## 🔔 Notifications & Status
18. **Push Notifications (Web Push API)** — Store `PushSubscription` per user; send browser push notifications for new DMs or @mentions when the tab is in the background.
19. **Do Not Disturb / Status** — Add a `UserStatus` enum (`ACTIVE`, `AWAY`, `DND`, `INVISIBLE`) to `User`; broadcast status changes via presence events.
20. **@Mentions with Notification Highlight** — Parse `@username` in messages, notify the mentioned user via their private WebSocket queue, and highlight the mention in the UI.
21. **Unread Message Counters** — Track last-read timestamps per user/channel in a `UserChannelState` table; compute and return unread counts in REST responses.

---

## 🔐 Security & Auth
22. **JWT Authentication** — Replace session-based auth with stateless JWT tokens (the `LoginResponse` already has a `token` field that currently goes unused).
23. **Password Change & Account Deletion** — Add `PUT /api/auth/password` and `DELETE /api/auth/account` endpoints with proper re-authentication guards.
24. **OAuth2 / Social Login** — Add Spring Security OAuth2 client for Google/GitHub SSO as an alternative to username/password registration.
25. **Rate Limiting** — Throttle message sends per user (e.g., 30 msg/min) using Bucket4j or a simple in-memory counter to prevent spam.

---

## 📁 File & Media
26. **Image Inline Preview** — Detect image MIME types and render them inline in the chat instead of as download links.
28. **Voice Messages** — Allow recording short audio clips (Web Audio API) and sending them as `.webm` file attachments with a built-in audio player.

---

## 📊 Analytics & Dashboard
29. **Activity Stats Dashboard** — Expose `GET /api/stats` returning per-user/group message counts, most active hours, and top contributors.
30. **Message Export** — Add `GET /api/groups/{id}/export` to download chat history as a JSON or CSV file (respecting TTL — only non-expired messages).

---

## Further Considerations
1. **Persistence:** Currently uses an in-memory H2 DB (`create-drop`), so all data is lost on restart. Migrating to PostgreSQL would be needed before shipping most features above.
2. **Priority pick:** The highest-impact, lowest-effort features are likely **Typing Indicators**, **Message Reactions**, **JWT Auth** (token field already exists), and **@Mentions** — great starting points.

