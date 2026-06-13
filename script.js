document.addEventListener('DOMContentLoaded', () => {
    const chatroomListContainer = document.querySelector('.chatroom-list');
    const currentRoomName = document.querySelector('.current-room-name');
    const currentRoomAvatar = document.querySelector('.header-avatar');
    const sendBtn = document.querySelector('.outer-send-btn');
    const messageInput = document.querySelector('.input-container input');
    const messagesContainer = document.querySelector('.messages-container');
    const appContainer = document.querySelector('.app-container');

    const API_BASE_URL = 'http://localhost:8081/api';
    
    // Getting user ID (Temperory)
    let CURRENT_USER_ID = prompt("Enter User ID:") || "123";
    
    let activeChat = {
        type: null,     // 'pv' یا 'group'
        targetId: null  // آیدی مخاطب یا آیدی گروه
    };
    
    let updateIntervalId = null;

    // =============( Mobile Back Button )=============
    const backBtn = document.querySelector('.back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            appContainer.classList.remove('chat-open'); // بستن باکس چت و برگشت به لیست چت‌ها در موبایل
        });
    }

    // =============( Profile Avatar )=============
    function getAvatarHTML(id, name) {
        const firstLetter = name ? name.trim().charAt(0).toUpperCase() : '?';
        const avatarUrl = `assets/avatars/${id}.png`;
        return `
            <img src="${avatarUrl}" alt="${name}" onerror="this.style.display='none';">
            <span>${firstLetter}</span>
        `;
    }

    // =============( User Data and Chats )=============
    async function initUserApp() {
        try {
            const res = await fetch(`${API_BASE_URL}/user/init?userId=${CURRENT_USER_ID}`);
            const userData = await res.json();
            console.log("User data loaded:", userData);
            chatroomListContainer.innerHTML = ''; //Cleaning chatrooms
            userData.pvs.forEach(pv => {
                createChatroomItemHTML(pv.targetId, pv.targetName, 'pv'); //Rendering PV's
            });
            userData.groups.forEach(group => {
                createChatroomItemHTML(group.groupId, group.groupName, 'group'); //Rendering groups
            });
            const firstChat = chatroomListContainer.querySelector('.chatroom-item'); //Rendering first
            if (firstChat) {
                firstChat.classList.add('active');
                currentRoomName.textContent = firstChat.querySelector('.room-name').textContent;
                activeChat.type = firstChat.getAttribute('data-type');
                activeChat.targetId = firstChat.getAttribute('data-target-id');
                currentRoomAvatar.innerHTML = getAvatarHTML(activeChat.targetId, currentRoomName.textContent);
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
    function createChatroomItemHTML(id, name, type) {
        const itemDiv = document.createElement('div');
        itemDiv.classList.add('chatroom-item');
        itemDiv.setAttribute('data-type', type);
        itemDiv.setAttribute('data-target-id', id);
        itemDiv.innerHTML = `
            <div class="room-info">
                <div class="room-avatar">${getAvatarHTML(id, name)}</div>
                <span class="room-name">${name}</span>
            </div>
            <i class="fas fa-ellipsis-v options-icon"></i>
        `;
        itemDiv.addEventListener('click', () => { //Mobile Things
            document.querySelectorAll('.chatroom-item').forEach(i => i.classList.remove('active'));
            itemDiv.classList.add('active');
            currentRoomName.textContent = name;
            currentRoomAvatar.innerHTML = getAvatarHTML(id, name);
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
            }
        } catch (error) {
            console.error('Error sending message:', error);
        }
    }
    sendBtn.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    // Update Messages (Temp)
    function setupAutoUpdate() {
        if (updateIntervalId) clearInterval(updateIntervalId);
        updateIntervalId = setInterval(loadMessages, 2000);
    }

    // =============( Initialize App )=============
    initUserApp();
});