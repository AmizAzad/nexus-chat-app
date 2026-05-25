/* ═══════════════════════════════════════════
   Nexus Chat — App Initialization
   ═══════════════════════════════════════════ */

async function initApp() {
  document.getElementById('auth-screen').style.display = 'none';
  document.getElementById('app').classList.add('show');
  updateMeDisplay();
  await loadUsers();
  await loadGroups();
  renderSidebar();
  connectWebSocket();
  startTtlTimer();
}

/* ── Event listeners ──────────────────── */
document.addEventListener('DOMContentLoaded', () => {
  // Close modals on overlay click
  document.querySelectorAll('.modal-overlay').forEach(o => {
    o.addEventListener('click', e => { if (e.target === o) o.classList.remove('show'); });
  });

  // Enter key on auth forms
  document.addEventListener('keydown', e => {
    if (e.key === 'Enter') {
      if (document.getElementById('tab-login').style.display !== 'none' &&
          (e.target.id === 'login-username' || e.target.id === 'login-password')) doLogin();
      if (document.getElementById('tab-register').style.display !== 'none' &&
          ['reg-username', 'reg-password', 'reg-display', 'reg-email', 'reg-nickname', 'reg-phone', 'reg-linkedin', 'reg-address'].includes(e.target.id)) doRegister();
    }
  });

  // Drag & drop files
  document.addEventListener('dragover', e => e.preventDefault());
  document.addEventListener('drop', e => {
    e.preventDefault();
    if (!NexusState.activeChat) return;
    const f = e.dataTransfer?.files?.[0];
    if (f) {
      const dt = new DataTransfer();
      dt.items.add(f);
      const input = document.getElementById('file-input');
      input.files = dt.files;
      handleFileSelect(input);
    }
  });

  // Auto-login if session exists
  (async () => {
    try {
      const r = await api('/api/auth/me');
      if (r?.success) {
        NexusState.me = r;
        initApp();
        if (!r.profileComplete) setTimeout(() => showModal('modal-profile-setup'), 500);
      }
    } catch (e) {}
  })();
});

window.initApp = initApp;

