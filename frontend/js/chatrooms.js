// =============( User Data and Chats )=============
async function LoadChatrooms(options = {}) {
    try {
        const res = await fetch(`${API_BASE_URL}/user/init?userId=${CURRENT_USER_ID}`);
        const userData = await res.json();
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

        const allRooms = await Promise.all(
            (userData.chats || []).map(async (chat) => {
                if (chat.type === 'pv') { // PV's
                    return {
                        id: chat.targetId,
                        name: await resolvePvDisplayName(chat.targetId, chat.targetName),
                        type: 'pv',
                        profilePicture: chat.profilePicture || null,
                        isArchived: chat.state === 'archived', 
                        isPinned: chat.state === 'pinned',
                        unreadCount: chat.unreadCount || 0
                    };
                }
                else { // Groups
                    let picture = chat.groupPicture || chat.avatar || null;
                    if (!picture && chat.groupId) {
                        try {
                            const avatarRes = await fetch(`${API_BASE_URL}/group/avatar?groupId=${chat.groupId}`);
                            if (avatarRes.ok) {
                                const avatarData = await avatarRes.json();
                                picture = avatarData.groupPicture || null;
                            }
                        }
                        catch (err) {
                            console.error("Failed to fetch individual group avatar:", err);
                        }
                    }
                    return {
                        id: chat.groupId,
                        name: chat.groupName,
                        type: 'group',
                        profilePicture: picture,
                        isArchived: chat.state === 'archived',
                        isPinned: chat.state === 'pinned',
                        unreadCount: chat.unreadCount || 0
                    };
                }
            })
        );

        if (!isViewingArchive) { 
            createArchiveFolderItemHTML();
        }

        let savedMessagesRoom = allRooms.find(room => room.id === CURRENT_USER_ID && room.type === 'pv');

        if (!savedMessagesRoom && !isViewingArchive) {
            savedMessagesRoom = {
                id: CURRENT_USER_ID,
                name: "Saved Messages",
                type: 'pv',
                profilePicture: 'saved_messages_icon',
                isArchived: false,
                isPinned: false,
                unreadCount: 0
            };
        }

        const filteredRooms = allRooms.filter(room => { 
            if (room.id === CURRENT_USER_ID && room.type === 'pv') return false; 
            const isArchived = !!room.isArchived;
            return isViewingArchive ? isArchived : !isArchived;
        });

        const pinnedRooms = filteredRooms.filter(room => room.isPinned);
        const unpinnedRooms = filteredRooms.filter(room => !room.isPinned);
        pinnedRooms.forEach(room => {
            createChatroomItemHTML(room.id, room.name, room.type, room.profilePicture, room.isPinned, room.unreadCount);
        });
        if (savedMessagesRoom && !isViewingArchive) {
            createChatroomItemHTML(
                savedMessagesRoom.id, 
                "Saved Messages", 
                savedMessagesRoom.type, 
                "saved_messages_icon", 
                savedMessagesRoom.isPinned,
                savedMessagesRoom.unreadCount
            );
        }
        unpinnedRooms.forEach(room => {
            createChatroomItemHTML(room.id, room.name, room.type, room.profilePicture, room.isPinned, room.unreadCount);
        });

        let itemToActivate = null;
        if (options.selectTargetId && options.selectType) {
            itemToActivate = chatroomListContainer.querySelector(
                `.chatroom-item[data-target-id="${options.selectTargetId}"][data-type="${options.selectType}"]`
            );
        }
        else if (options.preserveActive && activeChat.targetId) {
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
            if (!options.preserveActive || options.selectTargetId) {
                loadMessages();
                setupAutoUpdate();
            }
        }
        else if (filteredRooms.length === 0 && isViewingArchive) {
            chatroomListContainer.innerHTML += `
                <div class="archive-empty-state-text">
                    No archived chats found
                </div>
            `;
        }
        else if (!chatroomListContainer.querySelector('.chatroom-item') && !isViewingArchive) {
            messagesContainer.innerHTML = `
                <div class="chat-empty-state">
                    <div class="empty-state-bubble">
                        <i class="fas fa-comments"></i>
                        <h4>There Are No Chats</h4>
                    </div>
                </div>
            `;
        }
    }
    catch (err) {
        console.error("Initialization error:", err);
    }
}

// =============( Render Archive Folder Row Button )=============
function createArchiveFolderItemHTML() {
    const itemDiv = document.createElement('div');
    itemDiv.classList.add('chatroom-item', 'archive-folder-row');
    itemDiv.innerHTML = `
        <div class="room-info">
            <div class="room-avatar">
                <i class="fas fa-archive"></i>
            </div>
            <span class="room-name">Archived Chats</span>
        </div>
        <i class="fas fa-chevron-right"></i>
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
function createChatroomItemHTML(id, name, type, avatarBase64 = null, isPinned = false, unreadCount = 0) {
    const itemDiv = document.createElement('div');
    itemDiv.classList.add('chatroom-item');
    itemDiv.setAttribute('data-type', type);
    itemDiv.setAttribute('data-target-id', id);
    const pinIconHTML = isPinned ? `<i class="fas fa-thumbtack pin-icon pinned-chat-indicator"></i>` : '';
    const unreadHTML = unreadCount > 0 ? `<span class="unread-badge">${unreadCount}</span>` : '';
    
    itemDiv.innerHTML = `
        <div class="room-info">
            <div class="room-avatar-wrapper" style="position: relative; display: inline-block;">
                <div class="room-avatar">${getAvatarHTML(name, avatarBase64)}</div>
                ${unreadHTML}
            </div>
            <span class="room-name">${name}</span>
        </div>
        <div class="chatroom-item-actions">
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
        LoadChatrooms({ preserveActive: true }); 
        if (activeChat.type === "group") loadGroupMembersCount(activeChat.targetId);
        else {
            const lastOnline = document.querySelector(".last-online");
            if (headerSearchBox) {
                headerSearchBox.style.display = 'flex';
                headerMsgSearch.value = '';
            }
            // Saved Messages
            if (activeChat.targetId == CURRENT_USER_ID) {
                lastOnline.textContent = "";
                return;
            }
            fetch(`${API_BASE_URL}/user/info?userId=${activeChat.targetId}`)
            .then(res => res.json())
            .then(data => {
                if (!data.lastSeen) {
                    lastOnline.textContent = "";
                    return;
                }
                const lastSeenDate = new Date(data.lastSeen);
                const diffSeconds = Math.abs(Date.now() - lastSeenDate.getTime()) / 1000;
                if (data.isOnline === true || diffSeconds <= 10) {
                    lastOnline.textContent = "Online";
                    return;
                }
                const hour = String(lastSeenDate.getHours()).padStart(2, "0");
                const minute = String(lastSeenDate.getMinutes()).padStart(2, "0");
                lastOnline.textContent = `Last seen at ${hour}:${minute}`;
            })
            .catch(err => {
                console.error("Failed to load last seen:", err);
                lastOnline.textContent = "";
            });
        }
        setupAutoUpdate();
    });
    chatroomListContainer.appendChild(itemDiv);
}