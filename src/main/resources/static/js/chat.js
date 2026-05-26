/* ═══════════════════════════════════════════
   Nexus Chat — Chat / Messages
   ═══════════════════════════════════════════ */

async function openGroup(id) {
  const S = NexusState;
  const g = S.myGroups.find(g => g.id === id);
  if (!g) return;
  if (S.activeChat?.subscription) S.activeChat.subscription.unsubscribe();
  if (S.activeChat?.updateSub) S.activeChat.updateSub.unsubscribe();
  if (S.activeChat?.typingSub) S.activeChat.typingSub.unsubscribe();

  S.activeChat = { type: 'group', id, name: g.name, icon: g.iconEmoji, group: g };
  S.unreadCounts['g' + id] = 0;
  renderMainArea();

  if (S.stompClient?.connected) {
    S.activeChat.subscription = S.stompClient.subscribe('/topic/group/' + id, f => {
      const m = JSON.parse(f.body);
      if (S.activeChat?.type === 'group' && S.activeChat?.id === id) appendMessage(m);
      else { S.unreadCounts['g' + id] = (S.unreadCounts['g' + id] || 0) + 1; renderSidebar(); }
    });
    S.activeChat.updateSub = S.stompClient.subscribe('/topic/group/' + id + '/updates', f => handleMessageUpdate(JSON.parse(f.body)));
    S.activeChat.typingSub = S.stompClient.subscribe('/topic/typing/group/' + id, f => {
      const ev = JSON.parse(f.body);
      if (ev.username !== S.me.username) showTyping(ev);
    });
  }

  try { renderMessages(await api('/api/groups/' + id + '/messages')); } catch (e) {}
  api('/api/read/group_' + id, 'POST', null).catch(() => {});
  renderSidebar();
  document.querySelector('.sidebar')?.classList.remove('open');
}

async function openDm(u, d, c) {
  const S = NexusState;
  if (S.activeChat?.subscription) S.activeChat.subscription.unsubscribe();
  if (S.activeChat?.updateSub) S.activeChat.updateSub.unsubscribe();
  S.dmPartners.add(u);
  S.activeChat = { type: 'dm', target: u, name: d, avatarColor: c };
  S.unreadCounts['dm' + u] = 0;
  renderMainArea();
  renderSidebar();
  try { renderMessages(await api('/api/dm/' + u + '/messages')); } catch (e) {}
  document.querySelector('.sidebar')?.classList.remove('open');
}

function renderMainArea() {
  const S = NexusState;
  const m = document.getElementById('main-area');
  if (!m) return;

  if (!S.activeChat) {
    m.innerHTML = `
      <div class="empty-state">
        <div class="empty-state__icon">💬</div>
        <h3>Welcome to Nexus</h3>
        <p>Select a group or start a conversation from the sidebar.<br/><br/>
        <strong>@username</strong> to mention · <strong>@bot</strong> for AI<br/>
        📎 Files ≤2MB · ⏱ Configurable TTL<br/>
        💬 React · 📌 Pin · ✏️ Edit · 🗑️ Delete</p>
      </div>`;
    return;
  }

  const ig = S.activeChat.type === 'group';
  const g = ig ? S.myGroups.find(g => g.id === S.activeChat.id) : null;
  const mc = g?.members?.length || 0;
  const ttlLabel = g?.messageTtlMinutes === 0 ? '∞ No expiry' : g?.messageTtlMinutes ? `⏱ ${g.messageTtlMinutes}m` : '⏱ 30m';

  m.innerHTML = `
    <div class="chat-header">
      <span class="chat-header__icon">${ig ? (g?.iconEmoji || '💬') : '👤'}</span>
      <div class="chat-header__info" ${!ig ? `style="cursor:pointer" onclick="viewProfile('${S.activeChat.target}')"` : ''}>
        <div class="chat-header__name">${esc(S.activeChat.name)}</div>
        <div class="chat-header__sub">${ig ? `${mc} member${mc !== 1 ? 's' : ''} · ${ttlLabel}` : '@' + S.activeChat.target + ' · Direct message'}</div>
      </div>
      <div class="chat-header__actions">
        ${ig ? `
          <button class="btn-icon" onclick="showPinnedPanel()" title="Pinned messages">📌</button>
          <button class="btn-icon" onclick="summarizeChat()" title="AI Summary">🤖</button>
          <button class="btn-icon" onclick="showGroupSettings(${S.activeChat.id})" title="Settings">⚙️</button>
          <button class="btn-icon" onclick="showAddMemberModal(${S.activeChat.id})" title="Add member">👤+</button>
        ` : ''}
      </div>
    </div>
    <div class="pinned-panel" id="pinned-panel">
      <div class="pinned-panel__title">📌 Pinned Messages</div>
      <div id="pinned-list"></div>
    </div>
    <div class="messages" id="messages-container"></div>
    <div class="typing-bar" id="typing-bar"></div>
    <div class="input-area">
      <div class="reply-bar" id="reply-bar">
        <span>↩</span>
        <span class="reply-bar__sender" id="rb-sender"></span>
        <span class="reply-bar__text" id="rb-text"></span>
        <button class="reply-bar__close" onclick="cancelReply()">✕</button>
      </div>
      <div class="file-bar" id="file-preview-bar">
        <span>📎</span>
        <span class="file-bar__name" id="file-preview-name"></span>
        <span class="file-bar__size" id="file-preview-size"></span>
        <button class="file-bar__remove" onclick="clearPendingFile()">✕</button>
      </div>
      <div class="input-wrapper" id="input-wrapper">
        <div class="autocomplete" id="autocomplete-list" style="display:none"></div>
        <textarea id="message-input" rows="1" placeholder="Message ${ig ? '#' + S.activeChat.name : S.activeChat.name}…" onkeydown="handleInputKey(event)" oninput="handleInputChange(event)"></textarea>
        <div class="input-footer">
          <span class="input-footer__hint">↵ Send · Shift+↵ Newline · @mention · @bot</span>
          <div class="input-footer__actions">
            <button class="attach-btn" onclick="document.getElementById('file-input').click()" title="Attach file">📎</button>
            <button class="send-btn" onclick="sendMessage()">Send ↗</button>
          </div>
        </div>
      </div>
    </div>`;

  document.getElementById('message-input').focus();
  // Apply stored background for this chat
  applyChatBackground();
}

/* ── Messages rendering ───────────────── */
function renderMessages(msgs) {
  const c = document.getElementById('messages-container');
  if (!c) return;
  c.innerHTML = '';
  if (!msgs.length) {
    c.innerHTML = '<div class="empty-state"><div class="empty-state__icon">✨</div><p>No messages yet. Say hello!</p></div>';
    return;
  }
  let lastDate = null;
  msgs.forEach(msg => {
    const d = new Date(msg.timestamp).toDateString();
    if (d !== lastDate) {
      lastDate = d;
      const div = document.createElement('div');
      div.className = 'date-divider';
      div.textContent = fmtDate(msg.timestamp);
      c.appendChild(div);
    }
    c.appendChild(buildMessageEl(msg));
  });
  c.scrollTop = c.scrollHeight;
}

function appendMessage(msg) {
  const c = document.getElementById('messages-container');
  if (!c) return;
  const es = c.querySelector('.empty-state');
  if (es) es.remove();
  c.appendChild(buildMessageEl(msg));
  c.scrollTop = c.scrollHeight;
}

function buildMessageEl(msg) {
  const me = NexusState.me;
  const div = document.createElement('div');
  const isMention = msg.content && msg.content.toLowerCase().includes('@' + me.username.toLowerCase());

  let cls = 'msg';
  if (msg.deleted) cls += ' msg--deleted';
  else if (msg.botMessage) cls += ' msg--bot';
  else if (msg.pinned) cls += ' msg--pinned';
  else if (isMention) cls += ' msg--mention';
  div.className = cls;
  div.dataset.msgId = msg.id;
  div.dataset.expiresAt = msg.expiresAt || '';

  if (msg.expiresAt) {
    const rem = new Date(msg.expiresAt) - new Date();
    if (rem < 5 * 60000 && rem > 0) div.classList.add('msg--expiring');
  }

  // Avatar
  const avatarEl = document.createElement('div');
  avatarEl.className = 'avatar';
  avatarEl.style.background = msg.senderAvatarColor || '#6366f1';
  avatarEl.style.cursor = 'pointer';
  avatarEl.onclick = () => viewProfile(msg.senderUsername);
  if (msg.senderProfilePicture) avatarEl.innerHTML = `<img src="${msg.senderProfilePicture}"/>`;
  else avatarEl.textContent = initials(msg.senderDisplayName || msg.senderUsername);

  // Body
  const body = document.createElement('div');
  body.className = 'msg__body';

  let ttlHtml = '';
  if (msg.expiresAt) {
    const rem = new Date(msg.expiresAt) - new Date();
    if (rem > 0) ttlHtml = `<span class="msg__ttl" data-expires="${msg.expiresAt}">⏱ ${fmtTtl(rem)}</span>`;
  }

  let replyHtml = '';
  if (msg.replyToId) {
    replyHtml = `<div class="reply-preview"><span class="reply-preview__sender">${esc(msg.replyToSender || '')}</span>${esc(msg.replyToContent || '')}</div>`;
  }

  let fileHtml = '';
  if (msg.fileName && !msg.deleted) {
    const icon = getFileIcon(msg.fileType || '', msg.fileName);
    if (msg.fileType && msg.fileType.startsWith('image/')) {
      fileHtml = `<img class="file-preview-img" src="data:${msg.fileType};base64,${msg.fileData}" alt="${esc(msg.fileName)}" onclick="showLightbox(this.src)"/>`;
    } else if (msg.fileType && msg.fileType.startsWith('video/')) {
      fileHtml = `<video class="file-preview-video" controls><source src="data:${msg.fileType};base64,${msg.fileData}" type="${msg.fileType}"/></video>`;
    } else {
      fileHtml = `
        <div class="file-card">
          <span class="file-card__icon">${icon}</span>
          <div class="file-card__info">
            <div class="file-card__name">${esc(msg.fileName)}</div>
            <div class="file-card__size">${fmtSize(msg.fileSize)}</div>
          </div>
          <button class="btn btn-sm" onclick="downloadFile('${msg.fileData}','${esc(msg.fileName)}','${msg.fileType}')">Download</button>
        </div>`;
    }
  }

  let reactionsHtml = '';
  if (msg.reactions && Object.keys(msg.reactions).length) {
    reactionsHtml = '<div class="reactions">' +
      Object.entries(msg.reactions).map(([emoji, users]) =>
        `<div class="reaction ${users.includes(me.username) ? 'mine' : ''}" onclick="toggleReaction(${msg.id},'${emoji}')" title="${users.join(', ')}">${emoji} <span class="reaction__count">${users.length}</span></div>`
      ).join('') + '</div>';
  }

  const isOwn = msg.senderUsername === me.username;
  const safeContent = esc((msg.content || '').replace(/'/g, "\\'"));
  const actionsHtml = msg.deleted ? '' : `
    <div class="msg__actions">
      <button class="msg__action" onclick="setReply(${msg.id},'${esc(msg.senderDisplayName)}','${safeContent}')" title="Reply">↩</button>
      ${REACTION_EMOJIS.slice(0, 4).map(e => `<button class="msg__action" onclick="toggleReaction(${msg.id},'${e}')" title="${e}">${e}</button>`).join('')}
      ${msg.type === 'GROUP' ? `<button class="msg__action" onclick="togglePin(${msg.id})" title="Pin">📌</button>` : ''}
      ${isOwn ? `
        <button class="msg__action" onclick="editMsg(${msg.id},'${safeContent}')" title="Edit">✏️</button>
        <button class="msg__action" onclick="deleteMsg(${msg.id})" title="Delete">🗑️</button>
      ` : ''}
    </div>`;

  const contentHtml = msg.deleted
    ? '<div class="msg__text" style="color:var(--text-tertiary);font-style:italic">🗑️ This message was deleted</div>'
    : (msg.content ? `<div class="msg__text">${fmtContent(msg.content)}</div>` : '');

  body.innerHTML = `
    ${replyHtml}
    <div class="msg__meta">
      <span class="msg__author" style="color:${msg.senderAvatarColor || 'var(--accent-hover)'}" onclick="viewProfile('${msg.senderUsername}')">${esc(msg.senderDisplayName || msg.senderUsername)}</span>
      ${msg.botMessage ? '<span class="tag tag--bot">🤖 BOT</span>' : ''}
      ${msg.pinned ? '<span class="tag tag--pin">📌 PINNED</span>' : ''}
      <span class="msg__time">${fmtTime(msg.timestamp)}</span>
      ${msg.edited ? '<span class="msg__edited">(edited)</span>' : ''}
      ${ttlHtml}
    </div>
    ${contentHtml}
    ${fileHtml}
    ${reactionsHtml}
    ${actionsHtml}`;

  div.appendChild(avatarEl);
  div.appendChild(body);
  return div;
}

/* ── TTL timer ────────────────────────── */
function startTtlTimer() {
  setInterval(() => {
    document.querySelectorAll('.msg__ttl[data-expires]').forEach(el => {
      const rem = new Date(el.dataset.expires) - new Date();
      if (rem <= 0) {
        const m = el.closest('.msg');
        if (m) { m.style.opacity = '0'; m.style.transition = 'opacity 0.5s'; setTimeout(() => m.remove(), 500); }
      } else {
        el.textContent = '⏱ ' + fmtTtl(rem);
        if (rem < 5 * 60000) el.closest('.msg')?.classList.add('msg--expiring');
      }
    });
  }, 1000);
}

/* ── Message actions ──────────────────── */
function setReply(id, sender, content) {
  NexusState.replyingTo = { id, sender, content: content?.substring(0, 80) || '' };
  const rb = document.getElementById('reply-bar');
  if (rb) {
    rb.classList.add('show');
    document.getElementById('rb-sender').textContent = sender;
    document.getElementById('rb-text').textContent = NexusState.replyingTo.content;
  }
  document.getElementById('message-input')?.focus();
}
function cancelReply() {
  NexusState.replyingTo = null;
  document.getElementById('reply-bar')?.classList.remove('show');
}

function toggleReaction(msgId, emoji) {
  if (!NexusState.stompClient?.connected) return;
  NexusState.stompClient.send('/app/reaction', {}, JSON.stringify({ messageId: msgId, emoji }));
}

function editMsg(msgId, content) {
  const newContent = prompt('Edit message:', content);
  if (newContent !== null && newContent !== content && NexusState.stompClient?.connected) {
    NexusState.stompClient.send('/app/message.edit', {}, JSON.stringify({ messageId: msgId, content: newContent }));
  }
}

function deleteMsg(msgId) {
  if (!confirm('Delete this message?')) return;
  if (NexusState.stompClient?.connected) {
    NexusState.stompClient.send('/app/message.delete', {}, JSON.stringify({ messageId: msgId }));
  }
}

function togglePin(msgId) {
  if (NexusState.stompClient?.connected) {
    NexusState.stompClient.send('/app/message.pin', {}, JSON.stringify({ messageId: msgId }));
  }
}

function handleMessageUpdate(ev) {
  const el = document.querySelector(`[data-msg-id="${ev.messageId}"]`);
  if (!el) return;

  if (ev.action === 'delete') {
    el.className = 'msg msg--deleted';
    const body = el.querySelector('.msg__body');
    if (body) {
      body.querySelector('.msg__text')?.remove();
      body.querySelector('.file-card')?.remove();
      body.querySelector('.file-preview-img')?.remove();
      body.querySelector('.file-preview-video')?.remove();
      body.querySelector('.msg__actions')?.remove();
      const txt = document.createElement('div');
      txt.className = 'msg__text';
      txt.style.cssText = 'color:var(--text-tertiary);font-style:italic';
      txt.textContent = '🗑️ This message was deleted';
      body.appendChild(txt);
    }
  } else if (ev.action === 'edit') {
    const txt = el.querySelector('.msg__text');
    if (txt) txt.innerHTML = fmtContent(ev.content);
    if (!el.querySelector('.msg__edited')) {
      const meta = el.querySelector('.msg__meta');
      if (meta) { const ed = document.createElement('span'); ed.className = 'msg__edited'; ed.textContent = '(edited)'; meta.appendChild(ed); }
    }
  } else if (ev.action === 'pin' || ev.action === 'unpin') {
    if (ev.action === 'pin') {
      el.classList.add('msg--pinned');
      const meta = el.querySelector('.msg__meta');
      if (meta && !meta.querySelector('.tag--pin')) { const p = document.createElement('span'); p.className = 'tag tag--pin'; p.textContent = '📌 PINNED'; meta.appendChild(p); }
    } else {
      el.classList.remove('msg--pinned');
      el.querySelector('.tag--pin')?.remove();
    }
  }
}

/* ── Pinned panel ─────────────────────── */
async function showPinnedPanel() {
  const panel = document.getElementById('pinned-panel');
  if (!panel || !NexusState.activeChat?.id) return;
  panel.classList.toggle('show');
  if (panel.classList.contains('show')) {
    try {
      const msgs = await api('/api/groups/' + NexusState.activeChat.id + '/pinned');
      document.getElementById('pinned-list').innerHTML = msgs.length
        ? msgs.map(m => `<div class="pinned-panel__item"><span class="pinned-panel__sender">${esc(m.senderDisplayName)}</span>${esc((m.content || '').substring(0, 80))}</div>`).join('')
        : '<div style="font-size:12px;color:var(--text-tertiary);padding:6px 0">No pinned messages</div>';
    } catch (e) {}
  }
}

/* ── AI Summarize ─────────────────────── */
async function summarizeChat() {
  if (!NexusState.activeChat?.id) return;
  showToast('Generating summary…');
  try {
    const r = await api('/api/groups/' + NexusState.activeChat.id + '/summarize', 'POST');
    appendMessage({
      id: 0, content: '📋 **AI Summary:**\n' + (r.summary || 'No summary available'),
      senderUsername: 'bot', senderDisplayName: 'Nexus Bot', senderAvatarColor: '#6366f1',
      botMessage: true, timestamp: new Date().toISOString(), type: 'GROUP',
    });
  } catch (e) { showToast('Failed to generate summary', 'error'); }
}

/* ── Typing ───────────────────────────── */
function showTyping(ev) {
  const bar = document.getElementById('typing-bar');
  if (!bar) return;
  bar.textContent = ev.typing ? `${ev.displayName || ev.username} is typing…` : '';
  if (ev.typing) setTimeout(() => { if (bar.textContent.includes(ev.username)) bar.textContent = ''; }, 3000);
}

function emitTyping() {
  const S = NexusState;
  if (!S.stompClient?.connected || !S.activeChat) return;
  const now = Date.now();
  if (now - S.lastTypingSent < 2000) return;
  S.lastTypingSent = now;
  S.stompClient.send('/app/typing', {}, JSON.stringify({
    type: S.activeChat.type.toUpperCase(),
    groupId: S.activeChat.id || null,
    dmTarget: S.activeChat.target || null,
    typing: true,
  }));
}

/* ── File handling ────────────────────── */
function handleFileSelect(input) {
  const f = input.files[0];
  if (!f) return;
  if (f.size > 2 * 1024 * 1024) { showToast('File must be less than 2 MB', 'error'); input.value = ''; return; }
  const r = new FileReader();
  r.onload = e => {
    NexusState.pendingFile = { name: f.name, type: f.type || 'application/octet-stream', size: f.size, data: e.target.result.split(',')[1] };
    const b = document.getElementById('file-preview-bar');
    if (b) { b.classList.add('show'); document.getElementById('file-preview-name').textContent = f.name; document.getElementById('file-preview-size').textContent = fmtSize(f.size); }
  };
  r.readAsDataURL(f);
  input.value = '';
}

function clearPendingFile() {
  NexusState.pendingFile = null;
  document.getElementById('file-preview-bar')?.classList.remove('show');
}

/* ── Send message ─────────────────────── */
function sendMessage() {
  const S = NexusState;
  const input = document.getElementById('message-input');
  if (!input) return;
  const content = input.value.trim();
  if (!content && !S.pendingFile) return;
  if (!S.activeChat || !S.stompClient?.connected) return;

  const payload = { content: content || '', type: S.activeChat.type.toUpperCase() };
  if (S.activeChat.type === 'group') payload.groupId = S.activeChat.id;
  else payload.dmTarget = S.activeChat.target;
  if (S.pendingFile) { payload.fileName = S.pendingFile.name; payload.fileType = S.pendingFile.type; payload.fileSize = S.pendingFile.size; payload.fileData = S.pendingFile.data; }
  if (S.replyingTo) payload.replyToId = S.replyingTo.id;

  S.stompClient.send('/app/chat.send', {}, JSON.stringify(payload));
  input.value = '';
  input.style.height = '';
  hideAutocomplete();
  clearPendingFile();
  cancelReply();
}

/* ── Input handling ───────────────────── */
function handleInputKey(e) {
  const list = document.getElementById('autocomplete-list');
  const items = list?.querySelectorAll('.autocomplete__item') || [];

  if (list?.style.display !== 'none' && items.length) {
    if (e.key === 'ArrowDown') { e.preventDefault(); NexusState.autocompleteIndex = Math.min(NexusState.autocompleteIndex + 1, items.length - 1); hlAc(items); return; }
    if (e.key === 'ArrowUp') { e.preventDefault(); NexusState.autocompleteIndex = Math.max(NexusState.autocompleteIndex - 1, 0); hlAc(items); return; }
    if (e.key === 'Tab' || e.key === 'Enter') { if (NexusState.autocompleteIndex >= 0 && items[NexusState.autocompleteIndex]) { e.preventDefault(); items[NexusState.autocompleteIndex].click(); return; } }
    if (e.key === 'Escape') { hideAutocomplete(); return; }
  }

  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); return; }
  const ta = e.target;
  setTimeout(() => { ta.style.height = ''; ta.style.height = Math.min(ta.scrollHeight, 120) + 'px'; }, 0);
}

function handleInputChange(e) {
  emitTyping();
  const val = e.target.value, cur = e.target.selectionStart;
  const match = val.substring(0, cur).match(/@([a-zA-Z0-9_]*)$/);
  if (match) {
    const q = match[1].toLowerCase();
    let s = NexusState.allUsers.filter(u => u.username.toLowerCase().startsWith(q) || u.displayName.toLowerCase().startsWith(q));
    if ('bot'.startsWith(q)) s = [{ username: 'bot', displayName: 'Nexus Bot', avatarColor: '#6366f1', online: true }, ...s];
    if (s.length) showAutocomplete(s, match[0], match.index, cur);
    else hideAutocomplete();
  } else hideAutocomplete();
}

function showAutocomplete(users, trigger, start, cur) {
  NexusState.autocompleteIndex = -1;
  const list = document.getElementById('autocomplete-list');
  if (!list) return;
  list.style.display = 'block';
  list.innerHTML = users.slice(0, 8).map(u => `
    <div class="autocomplete__item" onclick="insertMention('${u.username}',${start},${cur})">
      <div class="avatar avatar--sm" style="background:${u.avatarColor}">${u.profilePicture ? `<img src="${u.profilePicture}"/>` : initials(u.displayName)}</div>
      <span>${esc(u.displayName)}</span>
      <span class="autocomplete__handle">@${u.username}</span>
    </div>`).join('');
}

function insertMention(u, s, c) {
  const i = document.getElementById('message-input'), v = i.value;
  i.value = v.substring(0, s) + '@' + u + ' ' + v.substring(c);
  i.focus();
  const p = s + u.length + 2;
  i.setSelectionRange(p, p);
  hideAutocomplete();
}

function hideAutocomplete() {
  const l = document.getElementById('autocomplete-list');
  if (l) l.style.display = 'none';
  NexusState.autocompleteIndex = -1;
}

function hlAc(items) {
  items.forEach((item, i) => item.classList.toggle('selected', i === NexusState.autocompleteIndex));
}

// Expose
window.openGroup = openGroup;
window.openDm = openDm;
window.renderMainArea = renderMainArea;
window.renderMessages = renderMessages;
window.appendMessage = appendMessage;
window.startTtlTimer = startTtlTimer;
window.setReply = setReply;
window.cancelReply = cancelReply;
window.toggleReaction = toggleReaction;
window.editMsg = editMsg;
window.deleteMsg = deleteMsg;
window.togglePin = togglePin;
window.handleMessageUpdate = handleMessageUpdate;
window.showPinnedPanel = showPinnedPanel;
window.summarizeChat = summarizeChat;
window.showTyping = showTyping;
window.handleFileSelect = handleFileSelect;
window.clearPendingFile = clearPendingFile;
window.sendMessage = sendMessage;
window.handleInputKey = handleInputKey;
window.handleInputChange = handleInputChange;
window.insertMention = insertMention;
window.hideAutocomplete = hideAutocomplete;

