document.addEventListener('DOMContentLoaded', () => {
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
    const saveProfileBtn = document.getElementById('saveProfileBtn');

    const contextMenu = document.getElementById('messageContextMenu');
    const ctxEditBtn = document.getElementById('ctxEditBtn');
    const ctxDeleteBtn = document.getElementById('ctxDeleteBtn');
    let selectedMessageId = null;

    const gearIcon = document.querySelector('.gear-icon');
    const headerSettingsMenu = document.getElementById('headerSettingsMenu');
    const headerViewProfileBtn = document.getElementById('headerViewProfileBtn');

    const messageInput = document.querySelector('.input-container input');
    const messagesContainer = document.querySelector('.messages-container');
    const appContainer = document.querySelector('.app-container');

    let isViewingArchive = false;
    const archivedFolderHeader = document.getElementById('archivedFolderHeader');
    const archiveBackBtn = document.getElementById('archiveBackBtn');

    const API_BASE_URL = 'http://localhost:8081/api';
    
    let CURRENT_USER_ID = localStorage.getItem("CURRENT_USER_ID");
    if (!CURRENT_USER_ID) {
        alert("You must login first!");
        window.location.href = "index.html";
        return;
    }
    let activeChat = {type: null, targetId: null};
    
    let updateIntervalId = null;

    // =============( Switch Mode Button )=============
    if (themeToggleBtn) {
        themeToggleBtn.addEventListener('click', () => {
            const htmlElement = document.documentElement;
            const currentTheme = htmlElement.getAttribute('data-theme');
            const icon = themeToggleBtn.querySelector('i');
            if (currentTheme === 'dark') {
                htmlElement.setAttribute('data-theme', 'light');
                icon.className = 'fas fa-moon';
                localStorage.setItem('theme', 'light');
            } else {
                htmlElement.setAttribute('data-theme', 'dark');
                icon.className = 'fas fa-sun';
                localStorage.setItem('theme', 'dark');
            }
        });
        const savedTheme = localStorage.getItem('theme') || 'dark';
        document.documentElement.setAttribute('data-theme', savedTheme);
        const icon = themeToggleBtn.querySelector('i');
        if (savedTheme === 'dark') {
            icon.className = 'fas fa-sun';
        } else {
            icon.className = 'fas fa-moon';
        }
    }

    // =============( Mobile Back Button )=============
    const backBtn = document.querySelector('.back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            appContainer.classList.remove('chat-open');
        });
    }

    // =============( Archive Folder Back )=============
    if (archiveBackBtn) {
        archiveBackBtn.addEventListener('click', () => {
            isViewingArchive = false;
            archivedFolderHeader.style.display = 'none';
            LoadChatrooms({ preserveActive: true });
        });
    }

    // =============( Profile Avatar )=============
    function getFirstName(fullName, fallback = "") {
        if (fullName && fullName.trim()) {
            return fullName.trim().split(/\s+/)[0];
        }
        return fallback;
    }

    const pvDisplayNameCache = new Map();

    async function resolvePvDisplayName(targetId, username) {
        if (pvDisplayNameCache.has(targetId)) {
            return pvDisplayNameCache.get(targetId);
        }
        try {
            const res = await fetch(`${API_BASE_URL}/details?userId=${targetId}`);
            if (res.ok) {
                const data = await res.json();
                const displayName = data.fullName || username;
                pvDisplayNameCache.set(targetId, displayName);
                return displayName;
            }
        } catch (err) {
            console.error("Error resolving display name:", err);
        }
        return username;
    }

    function getAvatarHTML(name, base64Image) {
        if (base64Image && base64Image.trim() !== "") {
            return `<img src="${base64Image}" style="width:100%; height:100%; object-fit:cover;">`;
        }
        const firstLetter = name ? name.trim().charAt(0).toUpperCase() : '?';
        return firstLetter;
    }

    function updateMainUserAvatar(fullName, base64Image) {
        let avatarHTML = getAvatarHTML(fullName, base64Image);
        if (base64Image && base64Image.trim() !== "") {
            avatarHTML = `<img src="${base64Image}" class="user-main-img" style="width:100%; height:100%; object-fit:cover; border-radius:50%;">`;
        }
        if (mainUserAvatar) {
            mainUserAvatar.innerHTML = avatarHTML;
        }
        if (profileModalAvatar) {
            profileModalAvatar.innerHTML = avatarHTML;
        }
    }

    // =============( User Data and Chats )=============
    async function LoadChatrooms(options = {}) {
        try {
            const res = await fetch(`${API_BASE_URL}/user/init?userId=${CURRENT_USER_ID}`);
            const userData = await res.json();
            console.log("User data loaded:", userData);
            chatroomListContainer.innerHTML = '';
            const myDisplayName = getFirstName(
                userData.fullName,
                userData.userName || userData.username || "Me"
            );
            const myAvatar = userData.profilePicture || userData.avatar || "";
            updateMainUserAvatar(myDisplayName, myAvatar);
            if (settingsUsername) settingsUsername.value = userData.userName || userData.username || CURRENT_USER_ID;
            if (settingsFullName) settingsFullName.value = userData.fullName || "";
            if (settingsBio) settingsBio.value = userData.bio || "";
            if (settingsPhone) settingsPhone.value = userData.phone || "";

            const resolvedPvs = await Promise.all(
                (userData.pvs || []).map(async (pv) => ({
                    ...pv,
                    id: pv.targetId,
                    name: await resolvePvDisplayName(pv.targetId, pv.targetName),
                    type: 'pv',
                    profilePicture: pv.profilePicture || null,
                    isArchived: pv.chatState === 'archived' || pv.state === 'archived', 
                    isPinned: pv.chatState === 'pinned' || pv.state === 'pinned'
                }))
            );

            const groupsList = (userData.groups || []).map(group => ({
                id: group.groupId,
                name: group.groupName,
                type: 'group',
                profilePicture: null,
                isArchived: group.chatState === 'archived' || group.state === 'archived',
                isPinned: group.chatState === 'pinned' || group.state === 'pinned'
            }));

            const allRooms = [...resolvedPvs, ...groupsList];

            if (!isViewingArchive) { // Render Archive Item
                createArchiveFolderItemHTML();
            }

            const filteredRooms = allRooms.filter(room => { // Filter Archives
                if (isViewingArchive) {
                    return room.isArchived;
                } else {
                    return !room.isArchived;
                }
            });

            filteredRooms.sort((a, b) => (b.isPinned ? 1 : 0) - (a.isPinned ? 1 : 0)); // Show pinned first

            filteredRooms.forEach(room => {
                createChatroomItemHTML(room.id, room.name, room.type, room.profilePicture, room.isPinned);
            });

            let itemToActivate = null;
            if (selectTargetId && selectType) {
                itemToActivate = chatroomListContainer.querySelector(
                    `.chatroom-item[data-target-id="${selectTargetId}"][data-type="${selectType}"]`
                );
            } else if (preserveActive && activeChat.targetId) {
                itemToActivate = chatroomListContainer.querySelector(
                    `.chatroom-item[data-target-id="${activeChat.targetId}"][data-type="${activeChat.type}"]`
                );
            }

            if (itemToActivate) {
                document.querySelectorAll('.chatroom-item').forEach(i => i.classList.remove('active'));
                itemToActivate.classList.add('active');
                const chatName = itemToActivate.querySelector('.room-name').textContent;
                currentRoomName.textContent = chatName;
                activeChat.type = itemToActivate.getAttribute('data-type');
                activeChat.targetId = itemToActivate.getAttribute('data-target-id');
                const avatarEl = itemToActivate.querySelector('.room-avatar');
                currentRoomAvatar.innerHTML = avatarEl.innerHTML;
                if (appContainer) appContainer.classList.add('chat-open');

                if (!preserveActive || selectTargetId) {
                    loadMessages();
                    setupAutoUpdate();
                }
            } else if (filteredRooms.length === 0 && isViewingArchive) {
                chatroomListContainer.innerHTML += `
                    <div style="padding: 20px; text-align: center; opacity: 0.5; font-size: 14px;">
                        No archived chats found
                    </div>
                `;
            } else if (!chatroomListContainer.querySelector('.chatroom-item') && !isViewingArchive) {
                messagesContainer.innerHTML = `
                    <div class="chat-empty-state">
                        <div class="empty-state-bubble">
                            <i class="fas fa-comments"></i>
                            <h4>There Are No Chats</h4>
                        </div>
                    </div>
                `;
            }

        } catch (err) {
            console.error("Initialization error:", err);
        }
    }

    // =============( Render Archive Folder Row Button )=============
    function createArchiveFolderItemHTML() {
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('chatroom-item', 'archive-folder-row');
        itemDiv.style.borderBottom = '1px dashed var(--border-light)';
        itemDiv.style.marginBottom = '12px';
        
        itemDiv.innerHTML = `
            <div class="room-info">
                <div class="room-avatar" style="background: linear-gradient(135deg, #64748b, #475569)">
                    <i class="fas fa-archive" style="color: white; font-size: 16px;"></i>
                </div>
                <span class="room-name" style="font-weight: 600;">Archived Chats</span>
            </div>
            <i class="fas fa-chevron-right" style="color: var(--icon-mute); font-size: 12px; padding-right: 5px;"></i>
        `;
        
        itemDiv.addEventListener('click', (e) => {
            e.stopPropagation();
            isViewingArchive = true;
            archivedFolderHeader.style.display = 'flex';
            LoadChatrooms();
        });
        
        chatroomListContainer.appendChild(itemDiv);
    }

    // =============( Create Chatroom )=============
    function createChatroomItemHTML(id, name, type, avatarBase64 = null, isPinned = false) {
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('chatroom-item');
        itemDiv.setAttribute('data-type', type);
        itemDiv.setAttribute('data-target-id', id);
        const pinIconHTML = isPinned ? `<i class="fas fa-thumbtack pin-icon" style="color: var(--cyan-aqua); font-size: 12px; margin-left: 8px;"></i>` : '';
        
        itemDiv.innerHTML = `
            <div class="room-info">
                <div class="room-avatar">${getAvatarHTML(name, avatarBase64)}</div>
                <span class="room-name">${name}</span>
            </div>
            <div class="item-actions-wrapper" style="display: flex; align-items: center; gap: 8px;">
                ${pinIconHTML}
                <i class="fas fa-ellipsis-v options-icon"></i>
            </div>
        `;
        
        itemDiv.addEventListener('click', () => { 
            document.querySelectorAll('.chatroom-item').forEach(i => i.classList.remove('active'));
            itemDiv.classList.add('active');
            currentRoomName.textContent = name;
            currentRoomAvatar.innerHTML = getAvatarHTML(name, avatarBase64);
            appContainer.classList.add('chat-open');
            activeChat.type = type;
            activeChat.targetId = id;
            loadMessages();
            setupAutoUpdate();
        });
        chatroomListContainer.appendChild(itemDiv);
    }

    // =============( Load Messages )=============
    async function loadMessages() {
        if (!activeChat.targetId) return;
        let url = '';
        if (activeChat.type === 'pv') {
            url = `${API_BASE_URL}/pv?user1=${CURRENT_USER_ID}&user2=${activeChat.targetId}`;
        } else if (activeChat.type === 'group') {
            url = `${API_BASE_URL}/group?groupId=${activeChat.targetId}`;
        }
        try {
            const response = await fetch(url);
            if (!response.ok) return;
            const messages = await response.json();
            messagesContainer.innerHTML = '';
            if (messages.length === 0) {
                messagesContainer.innerHTML = `
                    <div class="chat-empty-state">
                        <div class="empty-state-bubble">
                            <i class="fas fa-comments"></i>
                            <h4>No Messages Yet</h4>
                        </div>
                    </div>
                `;
                return;
            }
            messages.forEach(msg => {
                const messageDiv = document.createElement('div');
                messageDiv.setAttribute('data-msg-id', msg.id);
                messageDiv.addEventListener('contextmenu', (e) => {
                    if (messageDiv.classList.contains('incoming')) return;
                    e.preventDefault();
                    selectedMessageId = msg.id;
                    contextMenu.style.display = 'block'; 
                    const menuWidth = contextMenu.offsetWidth || 120;
                    contextMenu.style.left = `${e.pageX - menuWidth}px`;
                    contextMenu.style.top = `${e.pageY}px`;
                });
                messageDiv.classList.add('message');
                if (msg.senderId === CURRENT_USER_ID) {
                    messageDiv.classList.add('outgoing');
                } else {
                    messageDiv.classList.add('incoming');
                }
                messageDiv.innerHTML = `<div class="message-bubble">${msg.text}</div>`;
                messagesContainer.appendChild(messageDiv);
            });
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        } catch (error) {
            console.error('Error loading messages:', error);
        }
    }

    // =============( Live Search Chats )=============
    sidebarSearch.addEventListener('input', async () => {
        const searchInput = sidebarSearch.value.trim();
        if (searchInput.length < 4) {
            LoadChatrooms({ preserveActive: true });
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/user/search?searchInput=${searchInput}`);
            if (!response.ok) return;
            const data = await response.json();
            const searchResults = data.results;
            chatroomListContainer.innerHTML = '';
            if (searchResults.length === 0) {
                chatroomListContainer.innerHTML = '<div style="padding:15px; text-align:center; opacity:0.6;">No users found</div>';
                return;
            }
            const resolvedResults = await Promise.all(
                searchResults.map(async (user) => ({
                    ...user,
                    displayName: await resolvePvDisplayName(user.id, user.username)
                }))
            );
            resolvedResults.forEach(user => {
                createSearchItemHTML(user.id, user.displayName, user.profilePicture || null);
            });
        } catch (error) {
            console.error('Error during search:', error);
        }
    });

    function createSearchItemHTML(id, name, avatarBase64 = null) {
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('chatroom-item');
        itemDiv.setAttribute('data-type', 'pv');
        itemDiv.setAttribute('data-target-id', id);
        itemDiv.innerHTML = `
            <div class="room-info">
                <div class="room-avatar">${getAvatarHTML(name, avatarBase64)}</div>
                <span class="room-name">${name}</span>
            </div>
        `;
        itemDiv.addEventListener('click', () => {
            sidebarSearch.value = '';
            activeChat.type = 'pv';
            activeChat.targetId = id;
            currentRoomName.textContent = name;
            currentRoomAvatar.innerHTML = getAvatarHTML(name, avatarBase64);
            if (appContainer) appContainer.classList.add('chat-open');
            loadMessages();
            setupAutoUpdate();
            LoadChatrooms({ preserveActive: true });
        });

        chatroomListContainer.appendChild(itemDiv);
    }

    // =============( Send Message )=============
    async function sendMessage() {
        const text = messageInput.value.trim();
        if (text === '' || !activeChat.targetId) return;
        let url = `${API_BASE_URL}/${activeChat.type}`;
        let bodyData = activeChat.type === 'pv' ? {
            senderId: CURRENT_USER_ID,
            receiverId: activeChat.targetId,
            text: text
        } : {
            action: "send",
            groupId: activeChat.targetId,
            senderId: CURRENT_USER_ID,
            text: text
        };
        try {
            const response = await fetch(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(bodyData)
            });
            if (response.ok) {
                messageInput.value = '';
                await loadMessages();
                await LoadChatrooms({ preserveActive: true });
            }
        } catch (error) {
            console.error('Error sending message:', error);
        }
    }
    sendBtn.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    // =============( New Chat Modal )=============
    const contactsModal = document.getElementById('contactsModal');
    const openContactsBtn = document.getElementById('openContactsBtn');
    const closeContactsBtn = document.getElementById('closeContactsBtn');
    const modalContactsList = document.getElementById('modalContactsList');
    const createGroupModalBtn = document.getElementById('createGroupModalBtn');
    const groupCreationForm = document.getElementById('groupCreationForm');
    const groupNameInput = document.getElementById('groupNameInput');
    const submitGroupBtn = document.getElementById('submitGroupBtn');
    const cancelGroupBtn = document.getElementById('cancelGroupBtn');
    if (openContactsBtn) {
        openContactsBtn.addEventListener('click', () => {
            contactsModal.classList.add('active');
            if(groupCreationForm) groupCreationForm.style.display = 'none';
            if(createGroupModalBtn) createGroupModalBtn.style.display = 'flex';
            fetchContacts(); 
        });
    }
    if (closeContactsBtn) {
        closeContactsBtn.addEventListener('click', () => {
            contactsModal.classList.remove('active');
        });
    }
    if (createGroupModalBtn) {
        createGroupModalBtn.addEventListener('click', () => {
            createGroupModalBtn.style.display = 'none';
            groupCreationForm.style.display = 'flex';
            groupNameInput.value = '';
            groupNameInput.focus();
        });
    }
    if (cancelGroupBtn) {
        cancelGroupBtn.addEventListener('click', () => {
            groupCreationForm.style.display = 'none';
            createGroupModalBtn.style.display = 'flex';
        });
    }
    if (submitGroupBtn) {
        submitGroupBtn.addEventListener('click', async () => {
            const groupName = groupNameInput.value.trim();
            if (groupName === '') return;
            const bodyData = {action: "create", ownerId: CURRENT_USER_ID, name: groupName};
            try {
                const response = await fetch(`${API_BASE_URL}/group`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(bodyData)
                });
                if (response.ok) {
                    const result = await response.json();
                    alert(`Group "${groupName}" created successfully!`);
                    groupCreationForm.style.display = 'none';
                    createGroupModalBtn.style.display = 'flex';
                    contactsModal.classList.remove('active');
                    activeChat.type = 'group';
                    activeChat.targetId = result.groupId;
                    await LoadChatrooms({ selectTargetId: result.groupId, selectType: 'group' });
                } else {
                    console.error('Failed to create group.');
                }
            } catch (error) {
                console.error('Error creating group:', error);
            }
        });
    }

    // =============( Profile Settings Modal )=============
    if (mainUserAvatar) {
        mainUserAvatar.style.cursor = 'pointer';
        mainUserAvatar.addEventListener('click', async () => {
            if (profileSettingsModal) {
                profileSettingsModal.classList.add('active');
            }
            try {
                const response = await fetch(`${API_BASE_URL}/user/avatar?userId=${CURRENT_USER_ID}`);
                if (response.ok) {
                    const data = await response.json();
                    const currentName = getFirstName(
                        settingsFullName?.value,
                        settingsUsername?.value || "Me"
                    );
                    if (data.profilePicture && data.profilePicture.trim() !== "") {
                        if (profileModalAvatar) {
                            profileModalAvatar.innerHTML = `<img src="${data.profilePicture}" style="width:100%; height:100%; object-fit:cover; border-radius:50%;">`;
                        }
                    } else {
                        if (profileModalAvatar) profileModalAvatar.innerHTML = getAvatarHTML(currentName, "");
                    }
                }
            } catch (err) {
                console.error("Error loading profile picture into modal:", err);
            }
        });
    }

    if (closeProfileSettingsBtn) {
        closeProfileSettingsBtn.addEventListener('click', () => {
            profileSettingsModal.classList.remove('active');
        });
    }

    if (avatarFileInput) {
        avatarFileInput.addEventListener('change', function() {
            const file = this.files[0];
            if (!file) return;
            if (file.size > 1024 * 1024) {
                alert("Image is too large! Please select an image under 1MB.");
                return;
            }
            const reader = new FileReader();
            reader.onload = async function(e) {
                const base64String = e.target.result;
                const bodyData = {
                    userId: CURRENT_USER_ID,
                    profilePicture: base64String
                };
                try {
                    const response = await fetch(`${API_BASE_URL}/user/avatar`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(bodyData)
                    });
                    if (response.ok) {
                        alert('Avatar uploaded successfully!');
                        const currentName = getFirstName(
                            settingsFullName?.value,
                            settingsUsername?.value || "Me"
                        );
                        updateMainUserAvatar(currentName, base64String);
                    } else {
                        alert('Failed to upload avatar. Status: ' + response.status);
                    }
                } catch (error) {
                    console.error('Error uploading avatar:', error);
                }
            };
            reader.readAsDataURL(file);
        });
    }

    if (deleteAvatarBtn) {
        deleteAvatarBtn.addEventListener('click', async (e) => {
            e.stopPropagation();
            if (!confirm("Are you sure you want to delete your profile picture?")) return;

            const bodyData = {
                userId: CURRENT_USER_ID,
                profilePicture: ""
            };

            try {
                const response = await fetch(`${API_BASE_URL}/user/avatar`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(bodyData)
                });

                if (response.ok) {
                    alert('Avatar deleted successfully!');
                    const currentName = getFirstName(
                        settingsFullName?.value,
                        settingsUsername?.value || "Me"
                    );
                    updateMainUserAvatar(currentName, "");
                } else {
                    alert('Failed to delete avatar.');
                }
            } catch (error) {
                console.error('Error deleting avatar:', error);
            }
        });
    }

    if (saveProfileBtn) {
        saveProfileBtn.addEventListener('click', () => {
            alert('Other information saved locally! Global update API integration coming soon.');
            if (profileSettingsModal) profileSettingsModal.classList.remove('active');
        });
    }

    //=============( Message Modal )=============
    document.addEventListener('click', () => {
        if (contextMenu) contextMenu.style.display = 'none';
    });

    if (ctxDeleteBtn) { //Delete
        ctxDeleteBtn.addEventListener('click', async () => {
            if (!selectedMessageId) return;
            if (!confirm("Delete this message?")) return;
            const bodyData = {
                chatType: activeChat.type,
                messageId: selectedMessageId.toString(),
                user1: CURRENT_USER_ID,
                user2: activeChat.targetId,
                groupId: activeChat.targetId
            };
            try {
                const response = await fetch(`${API_BASE_URL}/message`, {
                    method: 'DELETE',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(bodyData)
                });
                if (response.ok) {
                    await loadMessages();
                }
            }
            catch (error) {
                console.error('Error deleting message:', error);
            }
        });
    }

    if (ctxEditBtn) { //Edit
        ctxEditBtn.addEventListener('click', async () => {
            if (!selectedMessageId) return;
            const newText = prompt("Edit your message:");
            if (newText === null || newText.trim() === "") return;
            const bodyData = {
                chatType: activeChat.type,
                messageId: selectedMessageId.toString(),
                user1: CURRENT_USER_ID,
                user2: activeChat.targetId,
                groupId: activeChat.targetId,
                newText: newText.trim()
            };
            try {
                const response = await fetch(`${API_BASE_URL}/message`, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(bodyData)
                });
                if (response.ok) {
                    await loadMessages();
                }
            }
            catch (error) {
                console.error('Error editing message:', error);
            }
        });
    }

    // =============( Settings modal )=============
    if (gearIcon) {
        gearIcon.addEventListener('click', async (e) => {
            e.stopPropagation();
            if (!activeChat.targetId) return;
            const currentItem = chatroomListContainer.querySelector( // Current State
                `.chatroom-item[data-target-id="${activeChat.targetId}"][data-type="${activeChat.type}"]`
            );
            const isPinned = currentItem ? !!currentItem.querySelector('.pin-icon') : false;
            const isArchived = isViewingArchive; 

            const dynamicActionsContainer = document.getElementById('dynamicHeaderSettingsActions');
            if (dynamicActionsContainer) {
                dynamicActionsContainer.innerHTML = '';
                const pinBtn = document.createElement('button'); // Pin | Archive Buttons
                pinBtn.innerHTML = isPinned 
                    ? `<i class="fas fa-thumbtack"></i> Unpin Chat` 
                    : `<i class="fas fa-thumbtack"></i> Pin Chat`;
                pinBtn.addEventListener('click', async () => {
                    const action = isPinned ? 'unpin' : 'pin';
                    await handleChatStateChange(activeChat.targetId, activeChat.type, action);
                });
                dynamicActionsContainer.appendChild(pinBtn);
                const archiveBtn = document.createElement('button');
                archiveBtn.innerHTML = isArchived 
                    ? `<i class="fas fa-file-archive"></i> Remove from Archive` 
                    : `<i class="fas fa-archive"></i> Add to Archive`;
                archiveBtn.addEventListener('click', async () => {
                    const action = isArchived ? 'unarchive' : 'archive';
                    await handleChatStateChange(activeChat.targetId, activeChat.type, action);
                });
                dynamicActionsContainer.appendChild(archiveBtn);

                if (activeChat.type === 'pv') { // Contact Buttons
                    const contactBtn = document.createElement('button');
                    const isContact = await checkIsContact(activeChat.targetId);

                    contactBtn.innerHTML = isContact 
                        ? `<i class="fas fa-user-minus"></i> Remove from Contact` 
                        : `<i class="fas fa-user-plus"></i> Add to Contact`;
                    
                    contactBtn.addEventListener('click', async () => {
                        const action = isContact ? 'remove' : 'add';
                        await handleContactChange(activeChat.targetId, action);
                    });
                    dynamicActionsContainer.appendChild(contactBtn);
                }
            }
            headerSettingsMenu.style.display = 'block';
            const menuWidth = headerSettingsMenu.offsetWidth || 160;
            headerSettingsMenu.style.left = `${e.pageX - menuWidth}px`;
            headerSettingsMenu.style.top = `${e.pageY + 10}px`;
        });
    }

    async function handleChatStateChange(chatId, type, action) {
        try {
            const response = await fetch(`${API_BASE_URL}/chatroom/state`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    userId: CURRENT_USER_ID,
                    chatId: chatId,
                    type: type,
                    action: action
                })
            });
            if (response.ok) {
                if (headerSettingsMenu) headerSettingsMenu.style.display = 'none';
                await LoadChatrooms({ preserveActive: true });
            } else {
                console.error("Failed to update chat room setting state workflow.");
            }
        } catch (error) {
            console.error("Error setting dynamic room parameters:", error);
        }
    }

    async function checkIsContact(targetContactId) {
        try {
            const response = await fetch(`${API_BASE_URL}/contacts?userId=${CURRENT_USER_ID}`);
            if (response.ok) {
                const contacts = await response.json();
                return contacts.some(contact => contact.userId === targetContactId);
            }
        } catch (err) {
            console.error("Error fetching contact verification metrics:", err);
        }
        return false;
    }

    async function handleContactChange(contactId, action) {
        try {
            const response = await fetch(`${API_BASE_URL}/contacts`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    action: action,
                    userId: CURRENT_USER_ID,
                    contactId: contactId
                })
            });
            if (response.ok) {
                alert(`Contact successfully updated! Action: ${action}`);
                if (headerSettingsMenu) headerSettingsMenu.style.display = 'none';
            } else {
                const errorData = await response.json();
                alert(`Error: ${errorData.error || 'Failed to modify contacts list.'}`);
            }
        } catch (error) {
            console.error("Network context error modifying contact parameters:", error);
        }
    }

    document.addEventListener('click', () => {
        if (headerSettingsMenu) headerSettingsMenu.style.display = 'none';
    });

    // =============( Profiles modal )=============
    if (headerViewProfileBtn) {
        headerViewProfileBtn.addEventListener('click', async () => {
            if (!activeChat.targetId) return;
            let url = `${API_BASE_URL}/details?`;
            if (activeChat.type === 'pv') {
                url += `userId=${activeChat.targetId}`;
            } else {
                url += `groupId=${activeChat.targetId}`;
            }
            try {
                const response = await fetch(url);
                if (!response.ok) throw new Error("Failed to fetch details");
                const data = await response.json();
                if (saveProfileBtn) saveProfileBtn.style.display = 'none';
                if (deleteAvatarBtn) deleteAvatarBtn.style.display = 'none';
                settingsFullName.readOnly = true;
                settingsBio.readOnly = true;
                if (settingsPhone) settingsPhone.parentNode.style.display = 'none';

                if (activeChat.type === 'pv') {
                    document.getElementById('modalTitle').textContent = "User Profile"; 
                    settingsUsername.value = data.userName || "";
                    settingsFullName.value = data.fullName || "";
                    settingsBio.value = data.bio || "No bio available.";
                    try {
                        const avatarRes = await fetch(`${API_BASE_URL}/user/avatar?userId=${activeChat.targetId}`);
                        if (avatarRes.ok) {
                            const avatarData = await avatarRes.json();
                            if (avatarData && avatarData.profilePicture) {
                                let base64Image = avatarData.profilePicture;
                                if (!base64Image.startsWith('data:image')) {
                                    base64Image = `data:image/jpeg;base64,${base64Image}`;
                                }

                                profileModalAvatar.innerHTML = `<img src="${base64Image}" style="width:100%; height:100%; border-radius:50%; object-fit:cover;">`;
                            } else {
                                const letter = (data.fullName || data.userName || "?").charAt(0).toUpperCase();
                                profileModalAvatar.innerHTML = `<div class="modal-contact-avatar" style="width:100%; height:100%; display:flex; align-items:center; justify-content:center; font-size:24px; background:var(--dark-blue); color:white; border-radius:50%;">${letter}</div>`;
                            }
                        } else {
                            throw new Error("Avatar not found");
                        }
                    } catch (err) {
                        const letter = (data.fullName || data.userName || "?").charAt(0).toUpperCase();
                        profileModalAvatar.innerHTML = `<div class="modal-contact-avatar" style="width:100%; height:100%; display:flex; align-items:center; justify-content:center; font-size:24px; background:var(--dark-blue); color:white; border-radius:50%;">${letter}</div>`;
                    }
                } else {
                    document.getElementById('modalTitle').textContent = "Group Info";
                    settingsUsername.value = `Owner: ${data.owner}`;
                    settingsFullName.value = data.name || "";
                    settingsBio.value = `Members Count: ${data.membersCount} users`;
                    const letter = (data.name || "G").charAt(0).toUpperCase();
                    profileModalAvatar.innerHTML = `<div class="modal-contact-avatar" style="width:100%; height:100%; display:flex; align-items:center; justify-content:center; font-size:24px; background:var(--dark-blue); color:white; border-radius:50%;">${letter}</div>`;
                }

                if (profileSettingsModal) profileSettingsModal.classList.add('active');

            } catch (error) {
                console.error("Error loading chat details:", error);
                alert("Could not load profile details.");
            }
        });
    }

    if (closeProfileSettingsBtn) {
        closeProfileSettingsBtn.addEventListener('click', () => {
            if (profileSettingsModal) profileSettingsModal.classList.remove('active');
            if (saveProfileBtn) saveProfileBtn.style.display = 'block';
            if (deleteAvatarBtn) deleteAvatarBtn.style.display = 'flex';
            settingsFullName.readOnly = false;
            settingsBio.readOnly = false;
            if (settingsPhone) settingsPhone.parentNode.style.display = 'block';
            document.getElementById('modalTitle').textContent = "Profile Settings"; 
        });
    }

    // =============( Fetch Contacts )=============
    async function fetchContacts() {
        modalContactsList.innerHTML = '<div class="modal-loading"><i class="fas fa-spinner fa-spin"></i> Loading contacts...</div>';
        try {
            const response = await fetch(`${API_BASE_URL}/contacts?userId=${CURRENT_USER_ID}`);
            if (!response.ok) throw new Error('Failed to fetch contacts');
            
            const contacts = await response.json();
            modalContactsList.innerHTML = '';

            if (contacts.length === 0) {
                modalContactsList.innerHTML = '<div class="modal-loading">No contacts found.</div>';
                return;
            }

            contacts.forEach(contact => {
                const contactName = contact.userName || 'Unknown';
                const contactId = contact.userId;
                const firstLetter = contactName.trim().charAt(0).toUpperCase();
                
                const item = document.createElement('div');
                item.className = 'modal-contact-item';
                item.innerHTML = `
                    <div class="modal-contact-avatar">${firstLetter}</div>
                    <div class="modal-contact-info">
                        <span class="modal-contact-name">${contactName}</span>
                        <span class="modal-contact-id">ID: ${contactId}</span>
                    </div>
                `;
                
                item.addEventListener('click', async () => {
                    activeChat.type = 'pv';
                    activeChat.targetId = contactId;
                    currentRoomName.textContent = contactName;
                    currentRoomAvatar.textContent = firstLetter;
                    contactsModal.classList.remove('active');
                    await loadMessages();
                    if (appContainer) appContainer.classList.add('chat-open');
                });
                
                modalContactsList.appendChild(item);
            });
        } catch (error) {
            console.error('Error getting contacts:', error);
            modalContactsList.innerHTML = '<div class="modal-loading" style="color:#ef4444;">Error loading contacts!</div>';
        }
    }

    // =============( Update Messages )=============
    function setupAutoUpdate() {
        if (updateIntervalId) clearInterval(updateIntervalId);
        updateIntervalId = setInterval(loadMessages, 2000);
    }

    // =============( Initialize App )=============
    LoadChatrooms();
});
