/* ═══════════════════════════════════════════
   Nexus Chat — Groups & Modals
   ═══════════════════════════════════════════ */

function selectEmoji(el) {
  document.querySelectorAll('.emoji-opt').forEach(e => e.classList.remove('selected'));
  el.classList.add('selected');
  NexusState.selectedEmoji = el.dataset.emoji;
}

/* ── Create group ─────────────────────── */
function showCreateGroupModal() {
  loadUsers().then(() => {
    document.getElementById('member-checklist').innerHTML = NexusState.allUsers.map(u => `
      <label class="user-list-item">
        <input type="checkbox" value="${u.username}"/>
        <div class="avatar avatar--sm" style="background:${u.avatarColor}">${u.profilePicture ? `<img src="${u.profilePicture}"/>` : initials(u.displayName)}</div>
        <span class="user-list-item__name">${esc(u.displayName)}</span>
        <span class="user-list-item__handle">@${u.username}</span>
      </label>`).join('');
  });
  showModal('modal-create-group');
}

async function submitCreateGroup() {
  const name = document.getElementById('new-group-name').value.trim();
  if (!name) { showToast('Group name required', 'error'); return; }
  const ttlVal = document.getElementById('new-group-ttl').value;
  try {
    const g = await api('/api/groups', 'POST', {
      name, description: document.getElementById('new-group-desc').value.trim(),
      iconEmoji: NexusState.selectedEmoji,
      memberUsernames: [...document.querySelectorAll('#member-checklist input:checked')].map(i => i.value),
      privateGroup: document.getElementById('new-group-private').value === 'true',
      messageTtlMinutes: ttlVal === '' ? null : parseInt(ttlVal),
    });
    NexusState.myGroups.push(g);
    hideModal('modal-create-group');
    renderSidebar();
    openGroup(g.id);
    showToast('Group created!', 'success');
    document.getElementById('new-group-name').value = '';
    document.getElementById('new-group-desc').value = '';
  } catch (e) { showToast('Failed to create group', 'error'); }
}

/* ── Add member ───────────────────────── */
function showAddMemberModal(gid) {
  NexusState.addMemberGroupId = gid;
  const g = NexusState.myGroups.find(g => g.id === gid);
  const existing = new Set(g?.memberUsernames || []);
  const nonMembers = NexusState.allUsers.filter(u => !existing.has(u.username));
  document.getElementById('modal-add-member').querySelector('h2').textContent = '👤+ Add Member';
  document.getElementById('add-member-list').innerHTML = nonMembers.length
    ? nonMembers.map(u => `
        <div class="user-list-item" onclick="addMemberNow('${u.username}')">
          <div class="avatar avatar--sm" style="background:${u.avatarColor}">${u.profilePicture ? `<img src="${u.profilePicture}"/>` : initials(u.displayName)}</div>
          <span class="user-list-item__name">${esc(u.displayName)}</span>
          <span class="user-list-item__handle">@${u.username}</span>
          <button class="btn btn-sm" style="margin-left:auto">Add</button>
        </div>`).join('')
    : '<div style="padding:12px;color:var(--text-tertiary);font-size:13px">All users are already members</div>';
  showModal('modal-add-member');
}

async function addMemberNow(u) {
  try {
    const g = await api('/api/groups/' + NexusState.addMemberGroupId + '/members', 'POST', { username: u });
    const idx = NexusState.myGroups.findIndex(g => g.id === NexusState.addMemberGroupId);
    if (idx !== -1) NexusState.myGroups[idx] = g;
    showToast(`@${u} added!`, 'success');
    hideModal('modal-add-member');
    renderSidebar();
    if (NexusState.activeChat?.type === 'group' && NexusState.activeChat?.id === NexusState.addMemberGroupId) renderMainArea();
  } catch (e) { showToast('Failed to add member', 'error'); }
}

/* ── Group settings ───────────────────── */
async function showGroupSettings(id) {
  NexusState.settingsGroupId = id;
  const g = NexusState.myGroups.find(g => g.id === id);
  if (!g) return;
  document.getElementById('gs-name').value = g.name;
  document.getElementById('gs-desc').value = g.description || '';
  document.getElementById('gs-private').value = g.privateGroup ? 'true' : 'false';
  document.getElementById('gs-ttl').value = g.messageTtlMinutes != null ? String(g.messageTtlMinutes) : '';

  document.getElementById('gs-members').innerHTML = g.members.map(m => `
    <div class="user-list-item">
      <div class="avatar avatar--sm" style="background:${m.avatarColor}">${m.profilePicture ? `<img src="${m.profilePicture}"/>` : initials(m.displayName)}</div>
      <span class="user-list-item__name">${esc(m.displayName)}</span>
      ${g.adminUsernames?.has?.(m.username) || g.createdByUsername === m.username
        ? '<span class="tag tag--admin" style="margin-left:auto">Admin</span>'
        : `<button class="btn btn-sm btn-danger" style="margin-left:auto;padding:3px 10px;font-size:11px" onclick="removeMemberFromGroup(${id},'${m.username}')">Remove</button>`}
    </div>`).join('');

  document.getElementById('gs-invite-link').textContent = '';
  showModal('modal-group-settings');
}

async function saveGroupSettings() {
  const S = NexusState;
  if (!S.settingsGroupId) return;
  try {
    const ttlVal = document.getElementById('gs-ttl').value;
    const g = await api('/api/groups/' + S.settingsGroupId + '/settings', 'PUT', {
      name: document.getElementById('gs-name').value,
      description: document.getElementById('gs-desc').value,
      iconEmoji: null,
      privateGroup: document.getElementById('gs-private').value === 'true',
      messageTtlMinutes: ttlVal === '' ? null : parseInt(ttlVal),
    });
    const idx = S.myGroups.findIndex(g => g.id === S.settingsGroupId);
    if (idx !== -1) S.myGroups[idx] = g;
    hideModal('modal-group-settings');
    renderSidebar();
    if (S.activeChat?.id === S.settingsGroupId) { S.activeChat.name = g.name; S.activeChat.group = g; renderMainArea(); }
    showToast('Group settings saved', 'success');
  } catch (e) { showToast('Failed to save settings', 'error'); }
}

async function removeMemberFromGroup(gid, u) {
  try {
    const g = await api(`/api/groups/${gid}/members/${u}`, 'DELETE');
    const idx = NexusState.myGroups.findIndex(g => g.id === gid);
    if (idx !== -1) NexusState.myGroups[idx] = g;
    showGroupSettings(gid);
    showToast(`@${u} removed`, 'success');
  } catch (e) { showToast('Failed to remove', 'error'); }
}

async function generateInvite() {
  if (!NexusState.settingsGroupId) return;
  try {
    const r = await api('/api/groups/' + NexusState.settingsGroupId + '/invite', 'POST', { maxUses: 0, expiresInHours: 24 });
    document.getElementById('gs-invite-link').innerHTML =
      `<strong>Invite token:</strong> ${r.token}<br/><span style="color:var(--text-tertiary)">Expires: ${r.expiresAt ? new Date(r.expiresAt).toLocaleString() : 'Never'}</span>`;
    showToast('Invite link generated!', 'success');
  } catch (e) { showToast('Failed to generate invite', 'error'); }
}

/* ── Public groups ────────────────────── */
async function showPublicGroups() {
  try {
    const groups = await api('/api/groups/public');
    if (!groups.length) { showToast('No public groups available'); return; }
    document.getElementById('add-member-list').innerHTML = groups.map(g => `
      <div class="user-list-item">
        <span style="font-size:18px">${g.iconEmoji}</span>
        <span class="user-list-item__name" style="flex:1">${esc(g.name)}</span>
        <span style="font-size:11px;color:var(--text-tertiary)">${g.members?.length || 0} members</span>
        <button class="btn btn-sm" onclick="joinGroup(${g.id})">Join</button>
      </div>`).join('');
    document.getElementById('modal-add-member').querySelector('h2').textContent = '🌐 Public Groups';
    showModal('modal-add-member');
  } catch (e) { showToast('Failed to load public groups', 'error'); }
}

async function joinGroup(id) {
  try {
    const g = await api('/api/groups/' + id + '/join', 'POST');
    NexusState.myGroups.push(g);
    hideModal('modal-add-member');
    renderSidebar();
    openGroup(g.id);
    showToast('Joined group!', 'success');
  } catch (e) { showToast('Failed to join group', 'error'); }
}

/* ── Search ───────────────────────────── */
function showSearchModal() {
  showModal('modal-search');
  document.getElementById('search-query').focus();
}

async function doSearch() {
  const q = document.getElementById('search-query').value.trim();
  if (!q) return;
  try {
    const r = await api('/api/search?q=' + encodeURIComponent(q));
    document.getElementById('search-results').innerHTML = r.messages.length
      ? r.messages.map(m => `
          <div style="padding:10px 0;border-bottom:1px solid var(--border)">
            <div style="font-size:12px;color:var(--text-tertiary)">${esc(m.senderDisplayName)} · ${fmtTime(m.timestamp)}</div>
            <div style="font-size:13px;margin-top:4px">${fmtContent(m.content || '')}</div>
          </div>`).join('')
      : '<div style="padding:20px;text-align:center;color:var(--text-tertiary)">No results found</div>';
  } catch (e) { showToast('Search failed', 'error'); }
}

function handleSidebarSearch(e) {
  if (e.key === 'Enter') {
    showSearchModal();
    document.getElementById('search-query').value = e.target.value;
    doSearch();
  }
}

// Expose
window.selectEmoji = selectEmoji;
window.showCreateGroupModal = showCreateGroupModal;
window.submitCreateGroup = submitCreateGroup;
window.showAddMemberModal = showAddMemberModal;
window.addMemberNow = addMemberNow;
window.showGroupSettings = showGroupSettings;
window.saveGroupSettings = saveGroupSettings;
window.removeMemberFromGroup = removeMemberFromGroup;
window.generateInvite = generateInvite;
window.showPublicGroups = showPublicGroups;
window.joinGroup = joinGroup;
window.showSearchModal = showSearchModal;
window.doSearch = doSearch;
window.handleSidebarSearch = handleSidebarSearch;

