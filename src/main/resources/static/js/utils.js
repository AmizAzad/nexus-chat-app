/* ═══════════════════════════════════════════
   Nexus Chat — Utility Functions
   ═══════════════════════════════════════════ */

async function api(path, method = 'GET', body = null) {
  const opts = {
    method,
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
  };
  const token = localStorage.getItem('jwt');
  if (token) opts.headers['Authorization'] = 'Bearer ' + token;
  if (body !== null) opts.body = JSON.stringify(body);

  const res = await fetch(path, opts);
  if (res.status === 401) {
    localStorage.removeItem('jwt');
    // Reload to show auth screen if not already there
    if (document.getElementById('app')?.classList.contains('show')) {
      showToast('Session expired. Please sign in again.', 'error');
      setTimeout(() => location.reload(), 1500);
    }
    throw new Error(401);
  }
  if (!res.ok) throw new Error(res.status);
  const txt = await res.text();
  return txt ? JSON.parse(txt) : null;
}

function initials(name) {
  if (!name) return '?';
  return name.split(' ').map(w => w[0]).join('').toUpperCase().substring(0, 2);
}

function esc(s) {
  return String(s || '')
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

function fmtTime(t) {
  return new Date(t).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

function fmtDate(t) {
  const d = new Date(t);
  const n = new Date();
  if (d.toDateString() === n.toDateString()) return 'Today';
  const y = new Date(n);
  y.setDate(n.getDate() - 1);
  if (d.toDateString() === y.toDateString()) return 'Yesterday';
  return d.toLocaleDateString([], { weekday: 'long', month: 'long', day: 'numeric' });
}

function fmtSize(b) {
  if (b < 1024) return b + ' B';
  if (b < 1024 * 1024) return (b / 1024).toFixed(1) + ' KB';
  return (b / (1024 * 1024)).toFixed(2) + ' MB';
}

function fmtTtl(ms) {
  const m = Math.floor(ms / 60000);
  const s = Math.floor((ms % 60000) / 1000);
  return m > 0 ? `${m}m ${s}s` : `${s}s`;
}

function fmtContent(t) {
  return esc(t)
    .replace(/@bot/gi, '<span class="bot-tag">@bot</span>')
    .replace(/@([a-zA-Z0-9_]+)/g, '<span class="mention-tag" onclick="viewProfile(\'$1\')">@$1</span>')
    .replace(/\n/g, '<br/>');
}

function getFileIcon(type, name) {
  if (type.startsWith('image/')) return '🖼️';
  if (type.startsWith('video/')) return '🎬';
  if (type.includes('pdf')) return '📄';
  if (type.includes('word') || name.endsWith('.doc')) return '📝';
  if (type.includes('sheet') || name.endsWith('.csv')) return '📊';
  return '📦';
}

function buildDmChannel(u1, u2) {
  return [u1, u2].sort().join('__');
}

function showToast(message, type = '') {
  const el = document.createElement('div');
  el.className = 'toast' + (type ? ` toast--${type}` : '');
  el.textContent = message;
  document.getElementById('toasts').appendChild(el);
  setTimeout(() => el.remove(), 3500);
}

function hideModal(id) {
  document.getElementById(id).classList.remove('show');
}

function showModal(id) {
  document.getElementById(id).classList.add('show');
}

function downloadFile(data, name, type) {
  const a = document.createElement('a');
  a.href = `data:${type};base64,${data}`;
  a.download = name;
  a.click();
}

function showLightbox(src) {
  document.getElementById('lightbox-img').src = src;
  document.getElementById('lightbox').classList.add('show');
}

function readFileAsDataURL(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = e => resolve(e.target.result);
    reader.onerror = reject;
    reader.readAsDataURL(file);
  });
}

// Expose globally
window.api = api;
window.initials = initials;
window.esc = esc;
window.fmtTime = fmtTime;
window.fmtDate = fmtDate;
window.fmtSize = fmtSize;
window.fmtTtl = fmtTtl;
window.fmtContent = fmtContent;
window.getFileIcon = getFileIcon;
window.buildDmChannel = buildDmChannel;
window.showToast = showToast;
window.hideModal = hideModal;
window.showModal = showModal;
window.downloadFile = downloadFile;
window.showLightbox = showLightbox;
window.readFileAsDataURL = readFileAsDataURL;

