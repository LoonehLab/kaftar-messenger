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

    const messageInput = document.querySelector('.input-container input');
    const messagesContainer = document.querySelector('.messages-container');
    const appContainer = document.querySelector('.app-container');

    const API_BASE_URL = 'http://localhost:8081/api';
    
    let CURRENT_USER_ID = localStorage.getItem("CURRENT_USER_ID");
    if (!CURRENT_USER_ID) {
        alert("You must login first!");
        window.location.href = "index.html";
        return;
    }
    let activeChat = {type: null, targetId: null};
    
    let updateIntervalId = null;

    // =============( Mobile Back Button )=============
    const backBtn = document.querySelector('.back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            appContainer.classList.remove('chat-open'); // بستن باکس چت و برگشت به لیست چت‌ها در موبایل
        });
    }

    // =============( Profile Avatar )=============
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
    async function LoadChatrooms() {
        try {
            const res = await fetch(`${API_BASE_URL}/user/init?userId=${CURRENT_USER_ID}`);
            const userData = await res.json();
            console.log("User data loaded:", userData);
            chatroomListContainer.innerHTML = '';
            const myName = userData.fullName || userData.username || "Me";
            const myAvatar = userData.avatar || userData.profilePicture || "";
            updateMainUserAvatar(myName, myAvatar);
            if (settingsUsername) settingsUsername.value = userData.username || CURRENT_USER_ID;
            if (settingsFullName) settingsFullName.value = userData.fullName || "";
            if (settingsBio) settingsBio.value = userData.bio || "";
            if (settingsPhone) settingsPhone.value = userData.phone || "";

            userData.pvs.forEach(pv => {
                createChatroomItemHTML(pv.targetId, pv.targetName, 'pv', pv.profilePicture || null); 
            });

            userData.groups.forEach(group => {
                createChatroomItemHTML(group.groupId, group.groupName, 'group', null); 
            });
            const firstChat = chatroomListContainer.querySelector('.chatroom-item'); 
            if (firstChat) {
                firstChat.classList.add('active');
                const firstChatName = firstChat.querySelector('.room-name').textContent;
                currentRoomName.textContent = firstChatName;
                activeChat.type = firstChat.getAttribute('data-type');
                activeChat.targetId = firstChat.getAttribute('data-target-id');
                let firstChatAvatar = null;
                if (activeChat.type === 'pv' && userData.pvs) {
                    const foundPv = userData.pvs.find(p => p.targetId === activeChat.targetId);
                    if (foundPv) firstChatAvatar = foundPv.profilePicture;
                }
                currentRoomAvatar.innerHTML = getAvatarHTML(firstChatName, firstChatAvatar);
                loadMessages();
                setupAutoUpdate();
            } else {
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

    // =============( Create Chatroom )=============
    function createChatroomItemHTML(id, name, type, avatarBase64 = null) {
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('chatroom-item');
        itemDiv.setAttribute('data-type', type);
        itemDiv.setAttribute('data-target-id', id);
        itemDiv.innerHTML = `
            <div class="room-info">
                <div class="room-avatar">${getAvatarHTML(name, avatarBase64)}</div>
                <span class="room-name">${name}</span>
            </div>
            <i class="fas fa-ellipsis-v options-icon"></i>
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
            LoadChatrooms();
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
            searchResults.forEach(user => {
                createSearchItemHTML(user.id, user.username, user.profilePicture || null);
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
            LoadChatrooms(); 
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
                await loadChatrooms();
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
                    alert(`Group "${groupName}" created successfully!`);
                    groupCreationForm.style.display = 'none';
                    createGroupModalBtn.style.display = 'flex';
                    contactsModal.classList.remove('active');
                    await loadChatrooms();
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
                    const currentName = (settingsFullName && settingsFullName.value) ? settingsFullName.value : "Me";
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
                        const currentName = (settingsFullName && settingsFullName.value) ? settingsFullName.value : "Me";
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
                    const currentName = (settingsFullName && settingsFullName.value) ? settingsFullName.value : "Me";
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