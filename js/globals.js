const chatroomListContainer = document.querySelector('.chatroom-list');
const currentRoomName = document.querySelector('.current-room-name');
const currentRoomAvatar = document.querySelector('.header-avatar');
const sidebarSearch = document.getElementById('sidebarSearch');
const sendBtn = document.querySelector('.outer-send-btn');

const mainUserAvatar = document.querySelector('.user-avatar.main-avatar');
const profileSettingsModal = document.getElementById('profileSettingsModal');
const closeProfileSettingsBtn = document.getElementById('closeProfileSettingsBtn');
const profileModalAvatar = document.getElementById('profileModalAvatar');
const avatarFileInput = document.getElementById('avatarFileInput');
const deleteAvatarBtn = document.getElementById('deleteAvatarBtn');
const settingsUsername = document.getElementById('settingsUsername');
const settingsFullName = document.getElementById('settingsFullName');
const settingsBio = document.getElementById('settingsBio');
const settingsPhone = document.getElementById('settingsPhone');
const selfDangerZone = document.getElementById('selfDangerZone');
const logoutBtn = document.getElementById('logoutBtn');
const deleteAccountBtn = document.getElementById('deleteAccountBtn');
const saveProfileBtn = document.getElementById('saveProfileBtn');

const contextMenu = document.getElementById('messageContextMenu');
const ctxEditBtn = document.getElementById('ctxEditBtn');
const ctxDeleteBtn = document.getElementById('ctxDeleteBtn');
const ctxReportBtn = document.getElementById('ctxReportBtn');
let selectedMessageId = null;

const headerSearchBox = document.getElementById('headerSearchBox');
const headerMsgSearch = document.getElementById('headerMsgSearch');
const gearIcon = document.querySelector('.gear-icon');
const headerSettingsMenu = document.getElementById('headerSettingsMenu');
const headerViewProfileBtn = document.getElementById('headerViewProfileBtn');

const groupAddMemberModal = document.getElementById('groupAddMemberModal');
const closeGroupAddMemberBtn = document.getElementById('closeGroupAddMemberBtn');
const groupAddMemberContactsList = document.getElementById('groupAddMemberContactsList');

const groupLogsModal = document.getElementById('groupLogsModal');
const closeGroupLogsBtn = document.getElementById('closeGroupLogsBtn');
const groupLogsList = document.getElementById('groupLogsList');
const logEditedTabBtn = document.getElementById('logEditedTabBtn');
const logDeletedTabBtn = document.getElementById('logDeletedTabBtn');
let currentLogTab = 'edited'; // edited | deleted

const messageInput = document.getElementById('messageTextInput');
const attachBtn = document.getElementById('attachBtn');
const imageFileInput = document.getElementById('imageFileInput');
const messagesContainer = document.querySelector('.messages-container');
const appContainer = document.querySelector('.app-container');

let isViewingArchive = false;
const archivedFolderHeader = document.getElementById('archivedFolderHeader');
const archiveBackBtn = document.getElementById('archiveBackBtn');

let messageTimestamps = [];

const API_BASE_URL = 'http://localhost:8081/api';

let CURRENT_USER_ID = localStorage.getItem("CURRENT_USER_ID");
let activeChat = {type: null, targetId: null};

let updateIntervalId = null;
let currentModalContext = { type: null, targetId: null };