document.addEventListener('DOMContentLoaded', () => {
    let CURRENT_USER_ID = localStorage.getItem("CURRENT_USER_ID");
    if (!CURRENT_USER_ID) {
        alert("You must login first!");
        window.location.href = "index.html";
        return;
    }

    // =============( Switch Mode Button )=============
    if (window.themeToggleBtn || document.getElementById('themeToggleBtn')) {
        const btn = window.themeToggleBtn || document.getElementById('themeToggleBtn');
        btn.addEventListener('click', () => {
            const htmlElement = document.documentElement;
            const currentTheme = htmlElement.getAttribute('data-theme');
            const icon = btn.querySelector('i');
            if (currentTheme === 'dark') {
                htmlElement.setAttribute('data-theme', 'light');
                icon.className = 'fas fa-moon';
                localStorage.setItem('theme', 'light');
            }
            else {
                htmlElement.setAttribute('data-theme', 'dark');
                icon.className = 'fas fa-sun';
                localStorage.setItem('theme', 'dark');
            }
        });
        const savedTheme = localStorage.getItem('theme') || 'dark';
        document.documentElement.setAttribute('data-theme', savedTheme);
        const icon = btn.querySelector('i');
        if (savedTheme === 'dark') icon.className = 'fas fa-sun';
        else icon.className = 'fas fa-moon';
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
                chatroomListContainer.innerHTML = '<div class="search-no-results">No users found</div>';
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
        }
        catch (error) {
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

    // =============( Live Message Search )=============
    if (headerMsgSearch) {
        headerMsgSearch.addEventListener('input', async () => {
            const query = headerMsgSearch.value.trim();
            if (query.length <= 3) {
                setupAutoUpdate();
                await loadMessages();
                return;
            }
            if (updateIntervalId) clearInterval(updateIntervalId);
            messagesContainer.innerHTML = `
                <div class="modal-loading">
                    <i class="fas fa-spinner fa-spin"></i> Searching messages...
                </div>
            `;
            let searchApiUrl = "";
            if (activeChat.type === 'pv')
                searchApiUrl = `${API_BASE_URL}/pv?user1=${CURRENT_USER_ID}&user2=${activeChat.targetId}&search=${encodeURIComponent(query)}`;
            else if (activeChat.type === 'group')
                searchApiUrl = `${API_BASE_URL}/group?groupId=${activeChat.targetId}&userId=${CURRENT_USER_ID}&search=${encodeURIComponent(query)}`;
            try {
                const res = await fetch(searchApiUrl);
                if (!res.ok) {
                    messagesContainer.innerHTML = '<div class="search-no-results">Error searching messages</div>';
                    return;
                }
                const filteredMessages = await res.json();
                messagesContainer.innerHTML = '';
                if (filteredMessages.length === 0) {
                    messagesContainer.innerHTML = '<div class="search-no-results">No matching messages found</div>';
                    return;
                }
                filteredMessages.forEach(msg => {
                    const messageDiv = document.createElement('div');
                    messageDiv.setAttribute('data-msg-id', msg.id);
                    messageDiv.classList.add('message');
                    if (msg.senderId === CURRENT_USER_ID) {
                        messageDiv.classList.add('outgoing');
                        messageDiv.innerHTML = `<div class="message-bubble">${msg.text}</div>`;
                    }
                    else {
                        messageDiv.classList.add('incoming');
                        messageDiv.innerHTML = `
                            <div class="message-sender-avatar">
                                <div class="msg-avatar-fallback">${msg.senderId.charAt(0).toUpperCase()}</div>
                            </div>
                            <div class="message-bubble">${msg.text}</div>
                        `;
                    }
                    messagesContainer.appendChild(messageDiv);
                });
                messagesContainer.scrollTop = messagesContainer.scrollHeight;
            }
            catch (err) {
                console.error("Error fetching filtered messages:", err);
                messagesContainer.innerHTML = '<div class="search-no-results">Connection error during search</div>';
            }
        });
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
        mainUserAvatar.addEventListener('click', async () => {
            currentModalContext = { type: 'self', targetId: CURRENT_USER_ID };
            if (profileSettingsModal) {
                profileSettingsModal.classList.add('active');
            }
            try {
                const detailRes = await fetch(`${API_BASE_URL}/details?userId=${CURRENT_USER_ID}`);
                if (detailRes.ok) {
                    const detailData = await detailRes.json();
                    if (settingsFullName) settingsFullName.value = detailData.fullName || "";
                    if (settingsBio) settingsBio.value = detailData.bio || "";
                }
                const response = await fetch(`${API_BASE_URL}/user/avatar?userId=${CURRENT_USER_ID}`);
                if (response.ok) {
                    const data = await response.json();
                    const currentName = getFirstName(
                        settingsFullName?.value,
                        settingsUsername?.value || "Me"
                    );
                    if (data.profilePicture && data.profilePicture.trim() !== "") {
                        if (profileModalAvatar) {
                            profileModalAvatar.innerHTML = `<img src="${data.profilePicture}" class="user-main-img">`;
                        }
                    } else {
                        if (profileModalAvatar) profileModalAvatar.innerHTML = getAvatarHTML(currentName, "");
                    }
                }
            } catch (err) {
                console.error("Error loading profile details into modal:", err);
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
                if (currentModalContext.type === 'group' && currentModalContext.targetId) {
                    const bodyData = {
                        groupId: currentModalContext.targetId,
                        groupPicture: base64String
                    };
                    try {
                        const response = await fetch(`${API_BASE_URL}/group/avatar`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify(bodyData)
                        });
                        if (response.ok) {
                            alert('Group icon updated successfully!');
                            if (closeProfileSettingsBtn) closeProfileSettingsBtn.click();
                            else if (profileSettingsModal) profileSettingsModal.classList.remove('active');
                            LoadChatrooms({ preserveActive: true });
                            return; 
                        }
                    }
                    catch (error) {
                        console.error('Error uploading group avatar:', error);
                    }
                }
                else {
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
                            const currentName = getFirstName(settingsFullName?.value, settingsUsername?.value || "Me");
                            updateMainUserAvatar(currentName, base64String);
                        }
                    }
                    catch (error) {
                        console.error('Error uploading user avatar:', error);
                    }
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
                }
                else alert('Failed to delete avatar.');
            }
            catch (error) {
                console.error('Error deleting avatar:', error);
            }
        });
    }

    if (saveProfileBtn) {
        saveProfileBtn.addEventListener('click', async () => {
            if (currentModalContext.type === 'group' && currentModalContext.targetId) {
                const targetName = settingsFullName.value.trim();
                if (!targetName) return alert("Group name cannot be empty");
                try {
                    const response = await fetch(`${API_BASE_URL}/group/update`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ groupId: currentModalContext.targetId, name: targetName })
                    });
                    if (response.ok) {
                        alert("Group information updated successfully!");
                        if (profileSettingsModal) profileSettingsModal.classList.remove('active');
                        LoadChatrooms({ preserveActive: true });
                    }
                    else alert("Failed to update group information.");
                }
                catch(err) {
                    console.error("Error updating group info:", err);
                }
            }
            else {
                const payload = {
                    userId: CURRENT_USER_ID,
                    fullName: settingsFullName.value.trim(),
                    bio: settingsBio.value.trim()
                };
                try {
                    const response = await fetch(`${API_BASE_URL}/user/update`, {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify(payload)
                    });
                    if (response.ok) {
                        alert("Profile updated successfully!");
                        if (profileSettingsModal) profileSettingsModal.classList.remove('active');
                        LoadChatrooms({ preserveActive: true });
                    }
                    else alert("Failed to update profile.");
                }
                catch(err) {
                    console.error("Error updating profile:", err);
                    alert("Network error updating profile.");
                }
            }
        });
    }

    // =============( Add Member Modal )=============
    if (closeGroupAddMemberBtn) {
        closeGroupAddMemberBtn.addEventListener('click', () => {
            if (groupAddMemberModal) groupAddMemberModal.classList.remove('active');
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
                if (response.ok) await loadMessages();
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
                if (response.ok) await loadMessages();
            }
            catch (error) {
                console.error('Error editing message:', error);
            }
        });
    }

    if (ctxReportBtn) {
        ctxReportBtn.addEventListener('click', async () => {
            if (!selectedMessageId) return;
            if (!confirm("Are you sure you want to report this message?")) return;
            const bodyData = {
                action: "report",
                chatType: activeChat.type,
                messageId: selectedMessageId.toString(),
                user1: CURRENT_USER_ID,
                user2: activeChat.targetId,
                groupId: activeChat.targetId
            };
            try {
                const response = await fetch(`${API_BASE_URL}/message`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(bodyData)
                });
                if (response.ok) alert("Message reported successfully.");
                else {
                    const errorData = await response.json().catch(() => ({}));
                    alert(`Failed to report: ${errorData.error || 'Unknown error'}`);
                }
            }
            catch (error) {
                console.error('Error reporting message:', error);
                alert("Network error. Could not report the message.");
            }
            contextMenu.style.display = 'none';
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
                const deleteActionBtn = document.createElement('button');
                deleteActionBtn.classList.add('delete-item');
                if (activeChat.type === 'pv') {
                    deleteActionBtn.innerHTML = `<i class="fas fa-trash-alt"></i> Delete Chat`;
                    deleteActionBtn.addEventListener('click', async () => {
                        if (!confirm("Are you sure you want to delete this chat history?")) return;
                        try {
                            const response = await fetch(`${API_BASE_URL}/pv`, {
                                method: 'DELETE',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({ user1: CURRENT_USER_ID, user2: activeChat.targetId })
                            });
                            if (response.ok) closeChatAndRefresh();
                        }
                        catch (err) {
                            console.error("Error deleting PV:", err);
                        }
                    });
                }
                else if (activeChat.type === 'group') {
                    let isOwner = false;
                    try {
                        const detailRes = await fetch(`${API_BASE_URL}/details?groupId=${activeChat.targetId}`);
                        if (detailRes.ok) {
                            const data = await detailRes.json();
                            isOwner = (data.ownerId === CURRENT_USER_ID);
                        }
                    }
                    catch (err) {
                        console.error("Error checking group owner:", err);
                    }
                    if (isOwner) {
                        deleteActionBtn.innerHTML = `<i class="fas fa-trash-alt"></i> Delete Group`;
                        deleteActionBtn.addEventListener('click', async () => {
                            if (!confirm("Are you sure you want to completely delete this group?")) return;
                            try {
                                const response = await fetch(`${API_BASE_URL}/group`, {
                                    method: 'DELETE',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({ groupId: activeChat.targetId })
                                });
                                if (response.ok) closeChatAndRefresh();
                            }
                            catch (err) {
                                console.error("Error deleting group:", err);
                            }
                        });
                    }
                    else {
                        deleteActionBtn.innerHTML = `<i class="fas fa-sign-out-alt"></i> Leave Group`;
                        deleteActionBtn.addEventListener('click', async () => {
                            if (!confirm("Are you sure you want to leave this group?")) return;
                            try {
                                const myUsername = settingsUsername ? settingsUsername.value.trim() : CURRENT_USER_ID;
                                const response = await fetch(`${API_BASE_URL}/group`, {
                                    method: 'POST',
                                    headers: { 'Content-Type': 'application/json' },
                                    body: JSON.stringify({
                                        action: "removeMember",
                                        groupId: activeChat.targetId,
                                        memberId: myUsername
                                    })
                                });
                                if (response.ok) closeChatAndRefresh();
                                else alert("Failed to leave group.");
                            }
                            catch (err) {
                                console.error("Error leaving group:", err);
                            }
                        });
                    }
                }
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

                if (activeChat.type === 'pv') { // Contact/Block Buttons
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
                    if (activeChat.targetId !== CURRENT_USER_ID) {
                        const blockBtn = document.createElement('button');
                        const isBlocked = await checkIsBlocked(activeChat.targetId);
                        if (isBlocked)  blockBtn.innerHTML = `<i class="fas fa-user-check"></i> Unblock User`;
                        else {
                            blockBtn.innerHTML = `<i class="fas fa-user-slash" style="color: #ef4444;"></i> Block User`;
                            blockBtn.style.color = '#ef4444';
                        }
                        blockBtn.addEventListener('click', async () => {
                            const action = isBlocked ? 'unblock' : 'block';
                            if (confirm(`Are you sure you want to ${action} this user?`))
                                await handleBlockChange(activeChat.targetId, action);
                        });
                        dynamicActionsContainer.appendChild(blockBtn);
                    }
                }
                dynamicActionsContainer.appendChild(deleteActionBtn);
            }
            headerSettingsMenu.style.display = 'block';
            const menuWidth = headerSettingsMenu.offsetWidth || 160;
            headerSettingsMenu.style.left = `${e.pageX - menuWidth}px`;
            headerSettingsMenu.style.top = `${e.pageY + 10}px`;
        });
    }

    function closeChatAndRefresh() {
        if (headerSettingsMenu) headerSettingsMenu.style.display = 'none';
        if (headerSearchBox) headerSearchBox.style.display = 'none';
        if (appContainer) appContainer.classList.remove('chat-open');
        messagesContainer.innerHTML = '';
        currentRoomName.textContent = 'No Chats Yet';
        if (currentRoomAvatar) currentRoomAvatar.innerHTML = '⚠';
        activeChat = {type: null, targetId: null};
        LoadChatrooms();
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
            }
            else console.error("Failed to update chat room setting state workflow.");
        }
        catch (error) {
            console.error("Error setting dynamic room parameters:", error);
        }
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
            }
            else {
                const errorData = await response.json();
                alert(`Error: ${errorData.error || 'Failed to modify contacts list.'}`);
            }
        }
        catch (error) {
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
            if (activeChat.type === 'pv') url += `userId=${activeChat.targetId}`;
            else url += `groupId=${activeChat.targetId}`;
            try {
                const response = await fetch(url);
                if (!response.ok) throw new Error("Failed to fetch details");
                const data = await response.json();
                currentModalContext.type = activeChat.type;
                currentModalContext.targetId = activeChat.targetId;
                const avatarWrapper = document.querySelector('.profile-avatar-wrapper');
                const usernameGroup = settingsUsername.parentNode;
                const fullNameGroup = settingsFullName.parentNode;
                const bioGroup = settingsBio.parentNode;
                const phoneGroup = settingsPhone ? settingsPhone.parentNode : null;
                const membersSection = document.getElementById('modalGroupMembersSection');
                if (activeChat.type === 'pv') {
                    document.getElementById('modalTitle').textContent = "User Profile"; 
                    const oldHistoryBtn = document.getElementById('openGroupLogsBtn');
                    if (oldHistoryBtn) oldHistoryBtn.remove();
                    avatarWrapper.setAttribute('data-mode', 'view-only'); // Locks avatar updates
                    usernameGroup.style.display = 'flex';
                    fullNameGroup.style.display = 'flex';
                    bioGroup.style.display = 'flex';
                    if (phoneGroup) phoneGroup.style.display = 'none';
                    if (membersSection) membersSection.style.display = 'none'; // Hidden on user profiles
                    if (saveProfileBtn) saveProfileBtn.style.display = 'none';
                    if (deleteAvatarBtn) deleteAvatarBtn.style.display = 'none';
                    settingsUsername.value = data.userName || "";
                    settingsFullName.value = data.fullName || "";
                    settingsBio.value = data.bio || "No bio available.";
                    settingsFullName.readOnly = true;
                    settingsBio.readOnly = true;
                    try {
                        const avatarRes = await fetch(`${API_BASE_URL}/user/avatar?userId=${activeChat.targetId}`);
                        if (avatarRes.ok) {
                            const avatarData = await avatarRes.json();
                            if (avatarData && avatarData.profilePicture) {
                                let base64Image = avatarData.profilePicture;
                                if (!base64Image.startsWith('data:image'))
                                    base64Image = `data:image/jpeg;base64,${base64Image}`;
                                profileModalAvatar.innerHTML = `<img src="${base64Image}">`;
                            }
                            else {
                                const letter = (data.fullName || data.userName || "?").charAt(0).toUpperCase();
                                profileModalAvatar.innerHTML = `<div class="modal-contact-avatar">${letter}</div>`;
                            }
                        }
                    }
                    catch (err) {
                        profileModalAvatar.innerHTML = `?`;
                    }
                }
                else {
                    document.getElementById('modalTitle').textContent = "Group Info";
                    usernameGroup.style.display = 'none';
                    bioGroup.style.display = 'none';
                    if (phoneGroup) phoneGroup.style.display = 'none';
                    fullNameGroup.style.display = 'flex';
                    fullNameGroup.querySelector('label').textContent = "Group Name";
                    settingsFullName.value = data.name || "";
                    const isOwner = (data.ownerId === CURRENT_USER_ID);
                    try {
                        const groupAvatarRes = await fetch(`${API_BASE_URL}/group/avatar?groupId=${activeChat.targetId}`);
                        if (groupAvatarRes.ok) {
                            const gAvatarData = await groupAvatarRes.json();
                            if (gAvatarData && gAvatarData.groupPicture) {
                                profileModalAvatar.innerHTML = `<img src="${gAvatarData.groupPicture}">`;
                            }
                            else {
                                const letter = (data.name || "G").charAt(0).toUpperCase();
                                profileModalAvatar.innerHTML = `<div class="modal-contact-avatar">${letter}</div>`;
                            }
                        }
                    }
                    catch(e) {
                        profileModalAvatar.innerHTML = `G`;
                    }
                    
                    if (isOwner) {
                        avatarWrapper.removeAttribute('data-mode'); // Upload overlay
                        settingsFullName.readOnly = false;          // Group name
                        if (saveProfileBtn) saveProfileBtn.style.display = 'flex';
                        if (deleteAvatarBtn) deleteAvatarBtn.style.display = 'flex';
                        const oldHistoryBtn = document.getElementById('openGroupLogsBtn');
                        if (oldHistoryBtn) oldHistoryBtn.remove();
                        const historyBtn = document.createElement('button');
                        historyBtn.id = 'openGroupLogsBtn';
                        historyBtn.className = 'add-member-trigger-btn';
                        historyBtn.style.marginTop = '10px';
                        historyBtn.style.border = '1px dashed #eab308';
                        historyBtn.style.backgroundColor = 'rgba(234, 179, 8, 0.1)';
                        historyBtn.style.color = '#f59e0b';
                        historyBtn.innerHTML = '<i class="fas fa-history"></i> Check Group History (Logs)';
                        historyBtn.addEventListener('click', (e) => {
                            e.preventDefault();
                            openGroupLogsWorkflow(activeChat.targetId);
                        });
                        const profileForm = document.querySelector('.profile-settings-form');
                        profileForm.insertBefore(historyBtn, document.getElementById('modalGroupMembersSection'));
                    }
                    else {
                        avatarWrapper.setAttribute('data-mode', 'view-only'); // Hides upload overlay
                        settingsFullName.readOnly = true;
                        if (saveProfileBtn) saveProfileBtn.style.display = 'none';
                        if (deleteAvatarBtn) deleteAvatarBtn.style.display = 'none';
                        const oldHistoryBtn = document.getElementById('openGroupLogsBtn');
                        if (oldHistoryBtn) oldHistoryBtn.remove();
                    }
                    renderGroupMembers(activeChat.targetId);
                }
                if (profileSettingsModal) profileSettingsModal.classList.add('active');
            }
            catch (error) {
                console.error("Error loading profile layout details:", error);
                alert("Could not load details.");
            }
        });
    }

    if (closeProfileSettingsBtn) {
        closeProfileSettingsBtn.addEventListener('click', () => {
            if (profileSettingsModal) profileSettingsModal.classList.remove('active');
            const oldHistoryBtn = document.getElementById('openGroupLogsBtn');
            if (oldHistoryBtn) oldHistoryBtn.remove();
            if (groupAddMemberModal) groupAddMemberModal.classList.remove('active');
            const avatarWrapper = document.querySelector('.profile-avatar-wrapper');
            if (avatarWrapper) avatarWrapper.removeAttribute('data-mode');
            settingsUsername.parentNode.style.display = 'flex';
            settingsFullName.parentNode.style.display = 'flex';
            settingsFullName.parentNode.querySelector('label').textContent = "Full Name";
            settingsBio.parentNode.style.display = 'flex';
            if (settingsPhone) settingsPhone.parentNode.style.display = 'block';
            
            settingsFullName.readOnly = false;
            settingsBio.readOnly = false;
            
            if (saveProfileBtn) saveProfileBtn.style.display = 'block';
            if (deleteAvatarBtn) deleteAvatarBtn.style.display = 'flex';
            
            document.getElementById('modalTitle').textContent = "Profile Settings"; 
            
            const membersSection = document.getElementById('modalGroupMembersSection');
            if (membersSection) membersSection.style.display = 'none';
            
            currentModalContext = { type: null, targetId: null };
        });
    }

    if (closeGroupLogsBtn) {
        closeGroupLogsBtn.addEventListener('click', () => {
            groupLogsModal.classList.remove('active');
        });
    }
    
    if (logEditedTabBtn && logDeletedTabBtn) {
        logEditedTabBtn.addEventListener('click', () => {
            if (currentLogTab === 'edited') return;
            currentLogTab = 'edited';
            logEditedTabBtn.classList.add('active');
            logDeletedTabBtn.classList.remove('active');
            fetchAndRenderGroupLogs(activeChat.targetId);
        });
        logDeletedTabBtn.addEventListener('click', () => {
            if (currentLogTab === 'deleted') return;
            currentLogTab = 'deleted';
            logDeletedTabBtn.classList.add('active');
            logEditedTabBtn.classList.remove('active');
            fetchAndRenderGroupLogs(activeChat.targetId);
        });
    }

    // =============( Initialize App )=============
    LoadChatrooms();
});