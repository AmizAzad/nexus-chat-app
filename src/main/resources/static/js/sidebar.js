/* ═══════════════════════════════════════════
   Nexus Chat — Sidebar & Navigation
   ═══════════════════════════════════════════ */

async function loadUsers() {
  try { NexusState.allUsers = await api('/api/users'); }
  catch (e) { NexusState.allUsers = []; }
}

async function loadGroups() {
  try { NexusState.myGroups = await api('/api/groups'); }
  catch (e) { NexusState.myGroups = []; }
}

function renderSidebar() {
  const { myGroups, allUsers, activeChat, unreadCounts, dmPartners } = NexusState;

  // Groups
  const gl = document.getElementById('group-list');
  gl.innerHTML = myGroups.length
    ? myGroups.map(g => `
        <div class="channel ${activeChat?.type === 'group' && activeChat?.id === g.id ? 'active' : ''}" onclick="openGroup(${g.id})">
          <span class="channel__icon">${g.iconEmoji}</span>
          <div class="channel__info"><div class="channel__name">${esc(g.name)}</div></div>
          <span class="channel__privacy" style="font-size:10px">${g.privateGroup ? '🔒' : '🌐'}</span>
          ${unreadCounts['g' + g.id] ? `<span class="badge">${unreadCounts['g' + g.id]}</span>` : ''}
        </div>`).join('')
    : '<div style="padding:6px 16px;font-size:12px;color:var(--text-tertiary)">No groups yet</div>';

  // DMs: partners first, then others
  const dl = document.getElementById('dm-list');
  const dmU = allUsers.filter(u => dmPartners.has(u.username));
  const otherU = allUsers.filter(u => !dmPartners.has(u.username));

  dl.innerHTML = [...dmU, ...otherU].map(u => {
    const dotColor = u.status === 'DND' ? 'var(--danger)' : u.status === 'AWAY' ? 'var(--warning)' : u.online ? 'var(--success)' : 'var(--text-tertiary)';
    return `
      <div class="channel ${activeChat?.type === 'dm' && activeChat?.target === u.username ? 'active' : ''}" onclick="openDm('${u.username}','${esc(u.displayName)}','${u.avatarColor}')">
        <div class="avatar avatar--sm" style="background:${u.avatarColor}">
          ${u.profilePicture ? `<img src="${u.profilePicture}"/>` : initials(u.displayName)}
        </div>
        <div class="channel__info"><div class="channel__name">${esc(u.displayName)}</div></div>
        <span class="channel__status" style="background:${dotColor}"></span>
        ${unreadCounts['dm' + u.username] ? `<span class="badge">${unreadCounts['dm' + u.username]}</span>` : ''}
      </div>`;
  }).join('');
}

// Expose
window.loadUsers = loadUsers;
window.loadGroups = loadGroups;
window.renderSidebar = renderSidebar;

