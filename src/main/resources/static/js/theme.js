/* ═══════════════════════════════════════════
   Nexus Chat — Theme & Chat Background
   ═══════════════════════════════════════════ */

const NEXUS_THEMES = [
  {
    id: 'light',
    name: '☀️ Reddit Light',
    preview: '#FF4500',
    vars: {
      '--bg-primary': '#DAE0E6',
      '--bg-secondary': '#FFFFFF',
      '--bg-tertiary': '#F6F7F8',
      '--bg-elevated': '#FFFFFF',
      '--bg-hover': '#F0F1F2',
      '--bg-active': 'rgba(255, 86, 0, 0.08)',
      '--border': '#EDEFF1',
      '--border-light': '#E0E2E4',
      '--border-focus': '#FF4500',
      '--text-primary': '#1C1C1C',
      '--text-secondary': '#576F76',
      '--text-tertiary': '#878A8C',
      '--text-inverse': '#FFFFFF',
      '--accent': '#FF4500',
      '--accent-hover': '#FF5722',
      '--accent-subtle': 'rgba(255, 69, 0, 0.10)',
      '--accent-glow': 'rgba(255, 69, 0, 0.06)',
    }
  },
  {
    id: 'dark',
    name: '🌙 Midnight Dark',
    preview: '#1A1A2E',
    vars: {
      '--bg-primary': '#0F0F17',
      '--bg-secondary': '#1A1A2E',
      '--bg-tertiary': '#16213E',
      '--bg-elevated': '#1A1A2E',
      '--bg-hover': '#22224A',
      '--bg-active': 'rgba(107, 91, 255, 0.15)',
      '--border': '#2D2D4E',
      '--border-light': '#3A3A5C',
      '--border-focus': '#6B5BFF',
      '--text-primary': '#E8E8F0',
      '--text-secondary': '#A0A0C0',
      '--text-tertiary': '#6A6A8A',
      '--text-inverse': '#FFFFFF',
      '--accent': '#6B5BFF',
      '--accent-hover': '#7C6EFF',
      '--accent-subtle': 'rgba(107, 91, 255, 0.15)',
      '--accent-glow': 'rgba(107, 91, 255, 0.08)',
    }
  },
  {
    id: 'ocean',
    name: '🌊 Ocean Blue',
    preview: '#0077B6',
    vars: {
      '--bg-primary': '#CAF0F8',
      '--bg-secondary': '#FFFFFF',
      '--bg-tertiary': '#E0F7FA',
      '--bg-elevated': '#FFFFFF',
      '--bg-hover': '#B2EBF2',
      '--bg-active': 'rgba(0, 119, 182, 0.10)',
      '--border': '#B0D4E0',
      '--border-light': '#90C4D4',
      '--border-focus': '#0077B6',
      '--text-primary': '#03045E',
      '--text-secondary': '#0077B6',
      '--text-tertiary': '#48CAE4',
      '--text-inverse': '#FFFFFF',
      '--accent': '#0077B6',
      '--accent-hover': '#0096C7',
      '--accent-subtle': 'rgba(0, 119, 182, 0.12)',
      '--accent-glow': 'rgba(0, 119, 182, 0.06)',
    }
  },
  {
    id: 'forest',
    name: '🌿 Forest Green',
    preview: '#2D6A4F',
    vars: {
      '--bg-primary': '#D8F3DC',
      '--bg-secondary': '#FFFFFF',
      '--bg-tertiary': '#E9F5DB',
      '--bg-elevated': '#FFFFFF',
      '--bg-hover': '#C7EDCA',
      '--bg-active': 'rgba(45, 106, 79, 0.10)',
      '--border': '#B7D8BD',
      '--border-light': '#95C49E',
      '--border-focus': '#2D6A4F',
      '--text-primary': '#1B4332',
      '--text-secondary': '#40916C',
      '--text-tertiary': '#74C69D',
      '--text-inverse': '#FFFFFF',
      '--accent': '#2D6A4F',
      '--accent-hover': '#40916C',
      '--accent-subtle': 'rgba(45, 106, 79, 0.12)',
      '--accent-glow': 'rgba(45, 106, 79, 0.06)',
    }
  },
  {
    id: 'dracula',
    name: '🧛 Dracula',
    preview: '#282A36',
    vars: {
      '--bg-primary': '#191A21',
      '--bg-secondary': '#282A36',
      '--bg-tertiary': '#21222C',
      '--bg-elevated': '#282A36',
      '--bg-hover': '#3A3C4E',
      '--bg-active': 'rgba(189, 147, 249, 0.15)',
      '--border': '#44475A',
      '--border-light': '#6272A4',
      '--border-focus': '#BD93F9',
      '--text-primary': '#F8F8F2',
      '--text-secondary': '#CFD8DC',
      '--text-tertiary': '#6272A4',
      '--text-inverse': '#282A36',
      '--accent': '#BD93F9',
      '--accent-hover': '#CFA5FF',
      '--accent-subtle': 'rgba(189, 147, 249, 0.15)',
      '--accent-glow': 'rgba(189, 147, 249, 0.06)',
    }
  },
  {
    id: 'solarized',
    name: '☀️ Solarized Dark',
    preview: '#002B36',
    vars: {
      '--bg-primary': '#002B36',
      '--bg-secondary': '#073642',
      '--bg-tertiary': '#004052',
      '--bg-elevated': '#073642',
      '--bg-hover': '#0A4C5E',
      '--bg-active': 'rgba(38, 139, 210, 0.15)',
      '--border': '#094E5E',
      '--border-light': '#186080',
      '--border-focus': '#268BD2',
      '--text-primary': '#EEE8D5',
      '--text-secondary': '#93A1A1',
      '--text-tertiary': '#657B83',
      '--text-inverse': '#002B36',
      '--accent': '#268BD2',
      '--accent-hover': '#2AA198',
      '--accent-subtle': 'rgba(38, 139, 210, 0.15)',
      '--accent-glow': 'rgba(38, 139, 210, 0.06)',
    }
  }
];

const NEXUS_BG_PRESETS = [
  { id: 'none', name: 'Default', preview: '', value: '' },
  { id: 'dots', name: 'Dots', preview: '⠿', value: 'radial-gradient(circle, rgba(0,0,0,0.06) 1px, transparent 1px) 0 0 / 20px 20px' },
  { id: 'lines', name: 'Lines', preview: '≡', value: 'repeating-linear-gradient(0deg, transparent, transparent 19px, rgba(0,0,0,0.05) 20px)' },
  { id: 'grid', name: 'Grid', preview: '⊞', value: 'linear-gradient(rgba(0,0,0,0.04) 1px, transparent 1px) 0 0 / 40px 40px, linear-gradient(90deg, rgba(0,0,0,0.04) 1px, transparent 1px) 0 0 / 40px 40px' },
  { id: 'diagonal', name: 'Diagonal', preview: '╱', value: 'repeating-linear-gradient(45deg, transparent, transparent 10px, rgba(0,0,0,0.03) 10px, rgba(0,0,0,0.03) 11px)' },
  { id: 'sunset', name: 'Sunset', preview: '🌅', value: 'linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%)' },
  { id: 'night', name: 'Night', preview: '🌌', value: 'linear-gradient(135deg, #0c0c1d 0%, #111132 50%, #0c0c1d 100%)' },
  { id: 'spring', name: 'Spring', preview: '🌸', value: 'linear-gradient(135deg, #ffeef8 0%, #ffd6e0 50%, #ffe8f0 100%)' },
  { id: 'teal', name: 'Teal', preview: '🩵', value: 'linear-gradient(135deg, #e0f7fa 0%, #b2ebf2 100%)' },
];

function applyThemeVars(vars) {
  const root = document.documentElement;
  Object.entries(vars).forEach(([k, v]) => root.style.setProperty(k, v));
}

function applyTheme(id) {
  const theme = NEXUS_THEMES.find(t => t.id === id);
  if (!theme) return;
  applyThemeVars(theme.vars);
  localStorage.setItem('nexus-theme', id);
  // Update active state in grid
  document.querySelectorAll('.theme-card').forEach(c => c.classList.toggle('active', c.dataset.id === id));
  showToast('Theme applied: ' + theme.name, 'success');
}

function showThemeModal() {
  const grid = document.getElementById('theme-grid');
  const current = localStorage.getItem('nexus-theme') || 'light';
  grid.innerHTML = NEXUS_THEMES.map(t => `
    <div class="theme-card ${t.id === current ? 'active' : ''}" data-id="${t.id}" onclick="applyTheme('${t.id}')" title="${t.name}">
      <div class="theme-card__swatch" style="background:${t.preview}"></div>
      <div class="theme-card__name">${t.name}</div>
    </div>`).join('');
  showModal('modal-theme');
}

function importThemeFile(input) {
  const file = input.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = e => {
    try {
      const vars = JSON.parse(e.target.result);
      applyThemeVars(vars);
      localStorage.setItem('nexus-theme-custom', JSON.stringify(vars));
      localStorage.setItem('nexus-theme', '__custom__');
      showToast('Custom theme applied!', 'success');
      hideModal('modal-theme');
    } catch (err) {
      showToast('Invalid theme file. Must be a JSON object with CSS variables.', 'error');
    }
  };
  reader.readAsText(file);
  input.value = '';
}

/* ── Chat Backgrounds ─────────────────── */
function getChatBgKey() {
  const S = NexusState;
  if (!S.activeChat) return null;
  return S.activeChat.type === 'group' ? 'nexus-bg-group-' + S.activeChat.id : 'nexus-bg-dm-' + S.activeChat.target;
}

function applyChatBackground() {
  const key = getChatBgKey();
  const mc = document.getElementById('messages-container');
  if (!mc) return;
  const stored = key ? localStorage.getItem(key) : null;
  if (stored) {
    const parsed = JSON.parse(stored);
    mc.style.background = parsed.background || '';
    mc.style.backgroundSize = parsed.backgroundSize || '';
    mc.style.backgroundRepeat = parsed.backgroundRepeat || '';
    mc.style.backgroundPosition = parsed.backgroundPosition || '';
    mc.style.backgroundAttachment = 'local';
  } else {
    mc.style.background = '';
    mc.style.backgroundSize = '';
    mc.style.backgroundRepeat = '';
    mc.style.backgroundPosition = '';
    mc.style.backgroundAttachment = '';
  }
}

function setChatBackground(bgValue, extra) {
  const key = getChatBgKey();
  if (!key) return;
  const data = { background: bgValue, ...extra };
  localStorage.setItem(key, JSON.stringify(data));
  applyChatBackground();
  hideModal('modal-chat-bg');
  showToast('Background applied!', 'success');
}

function clearChatBg() {
  const key = getChatBgKey();
  if (key) localStorage.removeItem(key);
  applyChatBackground();
  hideModal('modal-chat-bg');
  showToast('Background removed', '');
}

function showChatBgModal() {
  if (!NexusState.activeChat) { showToast('Open a chat first', 'error'); return; }
  const grid = document.getElementById('bg-grid');
  grid.innerHTML = NEXUS_BG_PRESETS.map(b => `
    <div class="bg-card" data-id="${b.id}" onclick="setChatBgPreset('${b.id}')" title="${b.name}">
      <div class="bg-card__preview" ${b.value ? `style="background:${b.value};background-size:cover"` : ''}>${b.preview}</div>
      <div class="bg-card__name">${b.name}</div>
    </div>`).join('');
  showModal('modal-chat-bg');
}

function setChatBgPreset(id) {
  const preset = NEXUS_BG_PRESETS.find(b => b.id === id);
  if (!preset) return;
  if (!preset.value) { clearChatBg(); return; }
  setChatBackground(preset.value, { backgroundSize: 'auto', backgroundRepeat: 'repeat', backgroundPosition: 'top left' });
}

function importBgImage(input) {
  const file = input.files[0];
  if (!file) return;
  if (file.size > 5 * 1024 * 1024) { showToast('Image must be under 5MB', 'error'); return; }
  const reader = new FileReader();
  reader.onload = e => {
    const dataUrl = e.target.result;
    setChatBackground(`url("${dataUrl}")`, { backgroundSize: 'cover', backgroundRepeat: 'no-repeat', backgroundPosition: 'center' });
  };
  reader.readAsDataURL(file);
  input.value = '';
}

/* ── Init ──────────────────────────────── */
function initTheme() {
  const saved = localStorage.getItem('nexus-theme');
  if (saved === '__custom__') {
    const custom = localStorage.getItem('nexus-theme-custom');
    if (custom) try { applyThemeVars(JSON.parse(custom)); } catch (e) {}
  } else if (saved) {
    const theme = NEXUS_THEMES.find(t => t.id === saved);
    if (theme) applyThemeVars(theme.vars);
  }
}

// Apply theme immediately on load
initTheme();

// Expose
window.showThemeModal = showThemeModal;
window.applyTheme = applyTheme;
window.importThemeFile = importThemeFile;
window.showChatBgModal = showChatBgModal;
window.setChatBgPreset = setChatBgPreset;
window.importBgImage = importBgImage;
window.clearChatBg = clearChatBg;
window.applyChatBackground = applyChatBackground;

