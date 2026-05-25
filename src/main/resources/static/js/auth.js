/* ═══════════════════════════════════════════
   Nexus Chat — Authentication
   ═══════════════════════════════════════════ */

function switchTab(t) {
  document.querySelectorAll('.auth-tab').forEach((e, i) =>
    e.classList.toggle('active', (i === 0) === (t === 'login'))
  );
  document.getElementById('tab-login').style.display = t === 'login' ? '' : 'none';
  document.getElementById('tab-register').style.display = t === 'register' ? '' : 'none';
  document.getElementById('auth-error').textContent = '';
}

function quickLogin(u) {
  document.getElementById('login-username').value = u;
  document.getElementById('login-password').value = 'password';
  switchTab('login');
  doLogin();
}

async function doLogin() {
  const u = document.getElementById('login-username').value.trim();
  const p = document.getElementById('login-password').value;
  if (!u || !p) { setAuthError('Please fill in all fields'); return; }

  try {
    const r = await api('/api/auth/login', 'POST', { username: u, password: p });
    if (r.success) {
      NexusState.me = r;
      if (r.token) localStorage.setItem('jwt', r.token);
      initApp();
      if (!r.profileComplete) {
        setTimeout(() => showModal('modal-profile-setup'), 500);
      }
    } else {
      setAuthError(r.message || 'Login failed');
    }
  } catch (e) {
    setAuthError('Server error. Is the app running?');
  }
}

async function doRegister() {
  const u = document.getElementById('reg-username').value.trim();
  const p = document.getElementById('reg-password').value;
  const d = document.getElementById('reg-display').value.trim();
  const e = document.getElementById('reg-email').value.trim();
  if (!u || !p) { setAuthError('Username and password required'); return; }
  if (!d) { setAuthError('Display name is required'); return; }
  if (!e) { setAuthError('Email is required'); return; }

  try {
    const r = await api('/api/auth/register', 'POST', {
      username: u, password: p, displayName: d, email: e,
      nickname: document.getElementById('reg-nickname').value.trim() || null,
      phone: document.getElementById('reg-phone').value.trim() || null,
      linkedinUrl: document.getElementById('reg-linkedin').value.trim() || null,
      address: document.getElementById('reg-address').value.trim() || null,
      profilePicture: NexusState.regProfilePic,
    });
    if (r.success) {
      document.getElementById('login-username').value = u;
      document.getElementById('login-password').value = p;
      switchTab('login');
      showToast('Account created! Signing in…', 'success');
      doLogin();
    } else {
      setAuthError(r.message || 'Registration failed');
    }
  } catch (err) {
    setAuthError('Server error');
  }
}

async function doLogout() {
  if (NexusState.stompClient) NexusState.stompClient.disconnect();
  await api('/api/auth/logout?username=' + NexusState.me.username, 'POST', null);
  NexusState.me = null;
  NexusState.activeChat = null;
  localStorage.removeItem('jwt');
  document.getElementById('app').classList.remove('show');
  document.getElementById('auth-screen').style.display = 'flex';
}

function setAuthError(m) {
  document.getElementById('auth-error').textContent = m;
}

async function previewRegPic(input) {
  const f = input.files[0];
  if (!f) return;
  if (f.size > 2 * 1024 * 1024) { showToast('Image must be less than 2MB', 'error'); return; }
  const data = await readFileAsDataURL(f);
  NexusState.regProfilePic = data;
  document.getElementById('reg-pic-preview').innerHTML = `<img src="${data}"/>`;
}

// Expose
window.switchTab = switchTab;
window.quickLogin = quickLogin;
window.doLogin = doLogin;
window.doRegister = doRegister;
window.doLogout = doLogout;
window.previewRegPic = previewRegPic;

