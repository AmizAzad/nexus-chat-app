/* ═══════════════════════════════════════════
   Nexus Chat — Profile Management
   ═══════════════════════════════════════════ */

async function previewSetupPic(input) {
  const f = input.files[0];
  if (!f) return;
  if (f.size > 2 * 1024 * 1024) { showToast('Image must be less than 2MB', 'error'); return; }
  const data = await readFileAsDataURL(f);
  NexusState.setupProfilePic = data;
  document.getElementById('setup-pic-preview').innerHTML = `<img src="${data}"/>`;
}

async function submitProfileSetup() {
  const e = document.getElementById('setup-email').value.trim();
  if (!e) { showToast('Email is required', 'error'); return; }
  try {
    const p = await api('/api/profile', 'PUT', {
      nickname: document.getElementById('setup-nickname').value.trim() || null,
      email: e,
      phone: document.getElementById('setup-phone').value.trim() || null,
      linkedinUrl: document.getElementById('setup-linkedin').value.trim() || null,
      address: document.getElementById('setup-address').value.trim() || null,
      profilePicture: NexusState.setupProfilePic,
    });
    NexusState.me.profileComplete = true;
    if (p.profilePicture) NexusState.me.profilePicture = p.profilePicture;
    NexusState.me.displayName = p.displayName;
    updateMeDisplay();
    hideModal('modal-profile-setup');
    showToast('Profile saved!', 'success');
  } catch (err) {
    showToast('Failed to save profile', 'error');
  }
}

async function previewEditPic(input) {
  const f = input.files[0];
  if (!f) return;
  if (f.size > 2 * 1024 * 1024) { showToast('Image must be less than 2MB', 'error'); return; }
  const data = await readFileAsDataURL(f);
  NexusState.editProfilePic = data;
  document.getElementById('edit-pic-preview').innerHTML = `<img src="${data}"/>`;
}

async function showEditProfileModal() {
  try {
    const p = await api('/api/profile/me');
    document.getElementById('edit-display').value = p.displayName || '';
    document.getElementById('edit-nickname').value = p.nickname || '';
    document.getElementById('edit-email').value = p.email || '';
    document.getElementById('edit-phone').value = p.phone || '';
    document.getElementById('edit-linkedin').value = p.linkedinUrl || '';
    document.getElementById('edit-address').value = p.address || '';
    document.getElementById('edit-status').value = p.status || 'ACTIVE';
    document.getElementById('edit-status-msg').value = p.statusMessage || '';
    NexusState.editProfilePic = p.profilePicture || null;
    document.getElementById('edit-pic-preview').innerHTML = p.profilePicture
      ? `<img src="${p.profilePicture}"/>` : '<span class="pic-preview__icon">📷</span>';
    showModal('modal-edit-profile');
  } catch (err) {
    showToast('Failed to load profile', 'error');
  }
}

async function submitEditProfile() {
  try {
    const p = await api('/api/profile', 'PUT', {
      displayName: document.getElementById('edit-display').value.trim(),
      nickname: document.getElementById('edit-nickname').value.trim() || null,
      email: document.getElementById('edit-email').value.trim(),
      phone: document.getElementById('edit-phone').value.trim() || null,
      linkedinUrl: document.getElementById('edit-linkedin').value.trim() || null,
      address: document.getElementById('edit-address').value.trim() || null,
      profilePicture: NexusState.editProfilePic,
    });
    NexusState.me.displayName = p.displayName;
    NexusState.me.profilePicture = p.profilePicture;
    NexusState.me.profileComplete = true;

    await api('/api/status', 'PUT', {
      status: document.getElementById('edit-status').value,
      statusMessage: document.getElementById('edit-status-msg').value.trim(),
    });
    NexusState.me.status = document.getElementById('edit-status').value;

    updateMeDisplay();
    hideModal('modal-edit-profile');
    showToast('Profile updated!', 'success');
    await loadUsers();
    renderSidebar();
  } catch (err) {
    showToast('Failed to update profile', 'error');
  }
}

async function changePassword() {
  const cur = document.getElementById('edit-cur-pw').value;
  const nw = document.getElementById('edit-new-pw').value;
  if (!cur || !nw) { showToast('Fill in both password fields', 'error'); return; }
  try {
    const r = await api('/api/auth/password', 'PUT', { currentPassword: cur, newPassword: nw });
    showToast(r.message || 'Password changed', 'success');
    document.getElementById('edit-cur-pw').value = '';
    document.getElementById('edit-new-pw').value = '';
  } catch (err) {
    showToast('Password change failed', 'error');
  }
}

async function deleteAccount() {
  if (!confirm('Are you sure? This cannot be undone.')) return;
  try {
    await api('/api/auth/account', 'DELETE');
    NexusState.me = null;
    NexusState.activeChat = null;
    localStorage.removeItem('jwt');
    document.getElementById('app').classList.remove('show');
    document.getElementById('auth-screen').style.display = 'flex';
    showToast('Account deleted', 'success');
  } catch (err) {
    showToast('Failed to delete account', 'error');
  }
}

async function viewProfile(u) {
  try {
    const p = await api('/api/profile/' + u);
    const me = NexusState.me;
    const statusIcon = p.status === 'DND' ? '🔴' : p.status === 'AWAY' ? '🟡' : p.status === 'INVISIBLE' ? '⚫' : '🟢';
    const statusClass = p.online ? 'online' : p.status === 'DND' ? 'dnd' : p.status === 'AWAY' ? 'away' : 'offline';

    document.getElementById('profile-content').innerHTML = `
      <div style="display:flex;align-items:center;gap:20px;margin-bottom:24px">
        <div class="avatar avatar--xl" style="background:${p.avatarColor}">
          ${p.profilePicture ? `<img src="${p.profilePicture}"/>` : initials(p.displayName)}
        </div>
        <div>
          <h3 style="font-size:18px;font-weight:700">${esc(p.displayName)}</h3>
          <div style="font-size:12px;color:var(--text-tertiary);font-family:var(--font-mono)">@${esc(p.username)}</div>
          ${p.nickname ? `<div style="font-size:13px;color:var(--accent-hover);margin-top:2px">"${esc(p.nickname)}"</div>` : ''}
          <div style="margin-top:8px"><span class="status-pill status-pill--${statusClass}">${statusIcon} ${p.status || 'ACTIVE'}${p.statusMessage ? ' — ' + esc(p.statusMessage) : ''}</span></div>
        </div>
      </div>
      ${p.email ? `<div class="profile-row"><span class="profile-row__icon">📧</span><div><div class="profile-row__label">Email</div><div class="profile-row__value">${esc(p.email)}</div></div></div>` : ''}
      ${p.phone ? `<div class="profile-row"><span class="profile-row__icon">📱</span><div><div class="profile-row__label">Phone</div><div class="profile-row__value">${esc(p.phone)}</div></div></div>` : ''}
      ${p.linkedinUrl ? `<div class="profile-row"><span class="profile-row__icon">🔗</span><div><div class="profile-row__label">LinkedIn</div><div class="profile-row__value"><a href="${esc(p.linkedinUrl)}" target="_blank">${esc(p.linkedinUrl)}</a></div></div></div>` : ''}
      ${p.address ? `<div class="profile-row"><span class="profile-row__icon">📍</span><div><div class="profile-row__label">Address</div><div class="profile-row__value">${esc(p.address)}</div></div></div>` : ''}
      <div class="modal-footer">
        <button class="btn btn-secondary" onclick="hideModal('modal-view-profile')">Close</button>
        ${p.username !== me.username ? `<button class="btn btn-primary" onclick="hideModal('modal-view-profile');openDm('${p.username}','${esc(p.displayName)}','${p.avatarColor}')">Message →</button>` : ''}
      </div>`;
    showModal('modal-view-profile');
  } catch (err) {
    showToast('Failed to load profile', 'error');
  }
}

function updateMeDisplay() {
  const me = NexusState.me;
  document.getElementById('me-display-name').textContent = me.displayName;
  document.getElementById('me-handle').textContent = '@' + me.username;
  const s = me.status || 'ACTIVE';
  const si = s === 'DND' ? '🔴' : s === 'AWAY' ? '🟡' : s === 'INVISIBLE' ? '⚫' : '🟢';
  document.getElementById('me-status-text').textContent = si + ' ' + s;
  const a = document.getElementById('me-avatar');
  a.style.background = me.avatarColor;
  a.innerHTML = me.profilePicture ? `<img src="${me.profilePicture}"/>` : initials(me.displayName);
}

// Expose
window.previewSetupPic = previewSetupPic;
window.submitProfileSetup = submitProfileSetup;
window.previewEditPic = previewEditPic;
window.showEditProfileModal = showEditProfileModal;
window.submitEditProfile = submitEditProfile;
window.changePassword = changePassword;
window.deleteAccount = deleteAccount;
window.viewProfile = viewProfile;
window.updateMeDisplay = updateMeDisplay;

