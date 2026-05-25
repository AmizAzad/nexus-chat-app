/* ═══════════════════════════════════════════
   Nexus Chat — WebSocket Connection
   ═══════════════════════════════════════════ */

function connectWebSocket() {
  const S = NexusState;
  const socket = new SockJS('/ws');
  S.stompClient = Stomp.over(socket);
  S.stompClient.debug = null;

  S.stompClient.connect({}, frame => {
    // DMs
    S.stompClient.subscribe('/user/queue/dm', f => {
      const msg = JSON.parse(f.body);
      S.dmPartners.add(msg.senderUsername === S.me.username ? S.activeChat?.target || '' : msg.senderUsername);
      if (S.activeChat?.type === 'dm' && msg.dmChannel === buildDmChannel(S.me.username, S.activeChat.target)) {
        appendMessage(msg);
      } else {
        const key = 'dm' + (msg.senderUsername !== S.me.username ? msg.senderUsername : '');
        S.unreadCounts[key] = (S.unreadCounts[key] || 0) + 1;
        renderSidebar();
        showToast(`📩 ${msg.senderDisplayName}: ${(msg.fileName ? '📎 ' + msg.fileName : msg.content?.substring(0, 50)) || ''}`);
      }
      renderSidebar();
    });

    // Presence
    S.stompClient.subscribe('/topic/presence', f => {
      const ev = JSON.parse(f.body);
      const u = S.allUsers.find(u => u.username === ev.username);
      if (u) { u.online = ev.online; if (ev.status) u.status = ev.status; renderSidebar(); }
    });

    // Reactions
    S.stompClient.subscribe('/topic/reactions', f => {
      // Could reload specific message — simplified
    });

    // Notifications
    S.stompClient.subscribe('/user/queue/notifications', f => {
      const notif = JSON.parse(f.body);
      showToast(`📢 ${notif.fromDisplayName} mentioned you: ${(notif.content || '').substring(0, 50)}`, 'success');
    });

    // Typing (DM)
    S.stompClient.subscribe('/user/queue/typing', f => {
      const ev = JSON.parse(f.body);
      if (S.activeChat?.type === 'dm' && ev.username !== S.me.username) showTyping(ev);
    });

    // DM updates
    S.stompClient.subscribe('/topic/dm/updates', f => {
      handleMessageUpdate(JSON.parse(f.body));
    });

    // Reconnect group subscriptions if active
    if (S.activeChat?.type === 'group') {
      S.activeChat.subscription = S.stompClient.subscribe('/topic/group/' + S.activeChat.id, f => appendMessage(JSON.parse(f.body)));
      S.activeChat.updateSub = S.stompClient.subscribe('/topic/group/' + S.activeChat.id + '/updates', f => handleMessageUpdate(JSON.parse(f.body)));
    }

    // Announce presence
    S.stompClient.send('/app/presence', {}, JSON.stringify({
      username: S.me.username, online: true,
      displayName: S.me.displayName, avatarColor: S.me.avatarColor,
      status: S.me.status || 'ACTIVE',
    }));
  }, err => {
    console.error('WebSocket error', err);
  });
}

window.connectWebSocket = connectWebSocket;

