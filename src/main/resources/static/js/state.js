/* ═══════════════════════════════════════════
   Nexus Chat — Global State
   ═══════════════════════════════════════════ */

const State = {
  me: null,
  stompClient: null,
  activeChat: null,
  allUsers: [],
  myGroups: [],
  unreadCounts: {},
  dmPartners: new Set(),
  pendingFile: null,
  replyingTo: null,
  autocompleteIndex: -1,
  selectedEmoji: '💬',
  addMemberGroupId: null,
  settingsGroupId: null,
  regProfilePic: null,
  setupProfilePic: null,
  editProfilePic: null,
  lastTypingSent: 0,
};

const REACTION_EMOJIS = ['👍', '❤️', '😂', '😮', '😢', '🔥', '🎉', '👀'];

window.NexusState = State;
window.REACTION_EMOJIS = REACTION_EMOJIS;

