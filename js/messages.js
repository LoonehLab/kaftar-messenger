// =============( Load Messages )=============
async function loadMessages() {
    if (!activeChat.targetId) return;
    const container = messagesContainer;
    if (!container) return;
    const currentChatId = `${activeChat.type}-${activeChat.targetId}`;
    const isChatChanged = (lastActiveChatId !== currentChatId);
    const threshold = 150;
    const isAtBottom = isChatChanged ? true : (container.scrollHeight - container.scrollTop - container.clientHeight) < threshold;
    const previousScrollTop = container.scrollTop;
    if (isChatChanged) lastActiveChatId = currentChatId;
    let url = '';
    if (activeChat.type === 'pv')
        url = `${API_BASE_URL}/pv?user1=${CURRENT_USER_ID}&user2=${activeChat.targetId}`;
    else if (activeChat.type === 'group')
        url = `${API_BASE_URL}/group?groupId=${activeChat.targetId}&userId=${CURRENT_USER_ID}`;
    try {
        const response = await fetch(url);
        if (!response.ok) return;
        const messages = await response.json();
        container.innerHTML = '';
        if (messages.length === 0) {
            container.innerHTML = `
                <div class="chat-empty-state">
                    <div class="empty-state-bubble">
                        <i class="fas fa-comments"></i>
                        <h4>No Messages Yet</h4>
                    </div>
                </div>
            `;
            return;
        }
        const avatarCache = new Map();
        for (const msg of messages) {
            const messageDiv = document.createElement('div');
            messageDiv.setAttribute('data-msg-id', msg.id);
            messageDiv.addEventListener('contextmenu', (e) => {
                e.preventDefault();
                selectedMessageId = msg.id;
                if (messageDiv.classList.contains('incoming')) {
                    if (document.getElementById('ctxEditBtn')) document.getElementById('ctxEditBtn').style.display = 'none';
                    if (document.getElementById('ctxDeleteBtn')) document.getElementById('ctxDeleteBtn').style.display = 'none';
                    if (document.getElementById('ctxReportBtn')) document.getElementById('ctxReportBtn').style.display = 'flex';
                }
                else {
                    if (document.getElementById('ctxEditBtn')) document.getElementById('ctxEditBtn').style.display = 'flex';
                    if (document.getElementById('ctxDeleteBtn')) document.getElementById('ctxDeleteBtn').style.display = 'flex';
                    if (document.getElementById('ctxReportBtn')) document.getElementById('ctxReportBtn').style.display = 'none';
                }
                contextMenu.style.display = 'block'; 
                const menuWidth = contextMenu.offsetWidth || 140;
                contextMenu.style.left = `${e.pageX - menuWidth}px`;
                contextMenu.style.top = `${e.pageY}px`;
            });
            messageDiv.classList.add('message');
            if (msg.senderId === CURRENT_USER_ID) {
                messageDiv.classList.add('outgoing');
                messageDiv.innerHTML = getMessageBubbleHTML(msg.text);
            }
            else {
                messageDiv.classList.add('incoming');
                if (activeChat.type === 'group') {
                    const avatarContainerId = `avatar-${msg.id}`;
                    messageDiv.innerHTML = `
                        <div class="message-sender-avatar" id="${avatarContainerId}">
                            <div class="msg-avatar-fallback">?</div>
                        </div>
                        ${getMessageBubbleHTML(msg.text)}
                    `;
                    (async () => {
                        let avatarBase64 = "";
                        if (avatarCache.has(msg.senderId))
                            avatarBase64 = avatarCache.get(msg.senderId);
                        else {
                            try {
                                const avatarRes = await fetch(`${API_BASE_URL}/user/avatar?userId=${msg.senderId}`);
                                if (avatarRes.ok) {
                                    const avatarData = await avatarRes.json();
                                    avatarBase64 = avatarData.profilePicture || "";
                                    avatarCache.set(msg.senderId, avatarBase64);
                                }
                            }
                            catch (err) {
                                console.error("Error fetching sender avatar:", err);
                            }
                        }
                        const containerElement = document.getElementById(avatarContainerId);
                        if (containerElement) {
                            if (avatarBase64 && avatarBase64.trim() !== "")
                                containerElement.innerHTML = `<img src="${avatarBase64}" class="msg-sender-img">`;
                            else containerElement.innerHTML = `<div class="msg-avatar-fallback">${msg.senderId.charAt(0).toUpperCase()}</div>`;
                        }
                    })();
                }
                else messageDiv.innerHTML = getMessageBubbleHTML(msg.text);
            }
            container.appendChild(messageDiv);
        }
        if (isChatChanged || isAtBottom) container.scrollTop = container.scrollHeight;
        else container.scrollTop = previousScrollTop;
    }
    catch (error) {
        console.error('Error loading messages:', error);
    }
}

// =============( Send Message )=============
async function sendMessage() {
    const text = messageInput.value.trim();
    if (text === '' || !activeChat.targetId) return;
    const now = Date.now();
    messageTimestamps = messageTimestamps.filter(timestamp => now - timestamp < 1000);
    if (messageTimestamps.length >= 2) {
        alert("Do NOT Spam!");
        return;
    }
    messageTimestamps.push(now);
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
        else {
            if (response.status === 403) {
                const errorData = await response.json();
                alert(errorData.error || "You are blocked by this user. Cannot send message.");
            }
            else {
                const errorData = await response.json().catch(() => ({}));
                alert(`Error: ${errorData.error || 'Failed to send message.'}`);
            }
        }
    } catch (error) {
        console.error('Error sending message:', error);
    }
}

if (attachBtn && imageFileInput) {
    attachBtn.addEventListener('click', () => {imageFileInput.click();});
    imageFileInput.addEventListener('change', function() {
        const file = this.files[0];
        if (!file) return;
        if (file.size > 1024*1024) {
            alert("Please select a smaller image.");
            return;
        }
        const reader = new FileReader();
        reader.onload = async function(e) {
            const base64String = e.target.result;
            await sendImageMessage(base64String);
        };
        reader.readAsDataURL(file);
        this.value = '';
    });
}

async function sendImageMessage(base64Data) {
    if (!activeChat.targetId) return;
    const textWithPrefix = `[IMAGE]${base64Data}`;
    let url = `${API_BASE_URL}/${activeChat.type}`;
    let bodyData = activeChat.type === 'pv' ? {
        senderId: CURRENT_USER_ID,
        receiverId: activeChat.targetId,
        text: textWithPrefix
    } : {
        action: "send",
        groupId: activeChat.targetId,
        senderId: CURRENT_USER_ID,
        text: textWithPrefix
    };
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bodyData)
        });
        if (response.ok) {
            await loadMessages();
            await LoadChatrooms({ preserveActive: true });
        }
        else alert('Error in sending image.');
    }
    catch (error) {
        console.error('Error sending image message:', error);
    }
}

async function sendStickerRaw(stickerText) {
    if (!activeChat.targetId) return;
    let url = `${API_BASE_URL}/${activeChat.type}`;
    let bodyData = activeChat.type === 'pv' ? {
        senderId: CURRENT_USER_ID,
        receiverId: activeChat.targetId,
        text: stickerText
    } : {
        action: "send",
        groupId: activeChat.targetId,
        senderId: CURRENT_USER_ID,
        text: stickerText
    };
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(bodyData)
        });
        if (response.ok) {
            await loadMessages();
            await LoadChatrooms({ preserveActive: true });
        }
        else alert('Failed to send sticker.');
    }
    catch (error) {
        console.error('Error sending sticker:', error);
    }
}

function getMessageBubbleHTML(text) {
    if (text && text.startsWith('[IMAGE]')) {
        const base64Image = text.replace('[IMAGE]', '');
        return `<div class="message-bubble message-image-bubble">
                    <img src="${base64Image}" style="max-width: 250px; border-radius: 8px; cursor: pointer;" onclick="window.open('${base64Image}')">
                </div>`;
    }
    if (text && text.startsWith('[STICKER]')) {
        const stickerName = text.replace('[STICKER]', '');
        return `<div class="message-bubble message-sticker-bubble">
                    <img src="assets/stickers/${stickerName}" style="max-width: 120px; display: block;">
                </div>`;
    }
    return `<div class="message-bubble">${text}</div>`;
}

async function updateLastOnlineStatus() {
    if (activeChat.type !== "pv" || !activeChat.targetId) return;
    const lastOnline = document.querySelector(".last-online");
    if (!lastOnline) return;
    if (activeChat.targetId == CURRENT_USER_ID) {
        lastOnline.textContent = "";
        return;
    }
    try {
        const res = await fetch(`${API_BASE_URL}/user/info?userId=${activeChat.targetId}`);
        const data = await res.json();
        if (!data.lastSeen) {
            lastOnline.textContent = "";
            return;
        }
        const lastSeenDate = new Date(data.lastSeen);
        const diffSeconds = Math.abs(Date.now() - lastSeenDate.getTime()) / 1000;
        if (data.isOnline === true || diffSeconds <= 10) lastOnline.textContent = "Online";
        else {
            const hour = String(lastSeenDate.getHours()).padStart(2, "0");
            const minute = String(lastSeenDate.getMinutes()).padStart(2, "0");
            lastOnline.textContent = `Last seen at ${hour}:${minute}`;
        }
    }
    catch (err) {
        console.error(err);
    }
}

// =============( Block Management )=============
async function checkIsBlocked(targetUserId) {
    try {
        const response = await fetch(`${API_BASE_URL}/block?userId=${CURRENT_USER_ID}&targetId=${targetUserId}`);
        if (response.ok) {
            const data = await response.json();
            return data.isBlocked;
        }
    }
    catch (err) {
        console.error("Error checking block status:", err);
    }
    return false;
}

async function handleBlockChange(targetUserId, action) {
    try {
        const response = await fetch(`${API_BASE_URL}/block`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                action: action, // block/unblock
                userId: CURRENT_USER_ID,
                targetId: targetUserId
            })
        });
        if (response.ok) {
            alert(`User successfully ${action === 'block' ? 'blocked' : 'unblocked'}!`);
            if (headerSettingsMenu) headerSettingsMenu.style.display = 'none';
        }
        else {
            const errorData = await response.json();
            alert(`Error: ${errorData.error || 'Failed to change block status.'}`);
        }
    }
    catch (error) {
        console.error("Error sending block/unblock request:", error);
    }
}

// =============( Update Messages )=============
function setupAutoUpdate() {
    if (updateIntervalId) clearInterval(updateIntervalId);
    updateIntervalId = setInterval(() => {
        loadMessages();
        if (activeChat.type === "pv") {
            updateLastOnlineStatus();
        }
    }, 2000);
}

// =============( Member Count )=============
async function loadGroupMembersCount(groupId) {
    try {
        const res = await fetch(
            `${API_BASE_URL}/group?action=members&groupId=${groupId}`
        );
        if (!res.ok) {
            console.log("Member count error:", res.status);
            return;
        }
        const data = await res.json();
        document.querySelector(".last-online").textContent =
            `${data.count} members`;
    }
    catch (err) {
        console.error("Member count failed:", err);
    }
}