// =============( Profile Settings Modal Update Workflow )=============
async function updateGroupMetadata(groupId, targetName) {
    if(!targetName.trim()) return alert("Group name can't be empty");
    try {
        const response = await fetch(`${API_BASE_URL}/group/update`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ groupId: groupId, name: targetName })
        });
        if (response.ok) {
            alert("Group configuration modified successfully!");
            if (profileSettingsModal) profileSettingsModal.classList.remove('active');
            LoadChatrooms({ preserveActive: true });
        }
    }
    catch(err) {
        console.error("Error processing text update request:", err);
    }
}

function setupGroupAvatarUpload(groupId) {
    if (!avatarFileInput) return;
    const baseNewInput = avatarFileInput.cloneNode(true);
    avatarFileInput.parentNode.replaceChild(baseNewInput, avatarFileInput);
    baseNewInput.addEventListener('change', function() {
        const selectedFile = this.files[0];
        if (!selectedFile) return;
        const fileBlobReader = new FileReader();
        fileBlobReader.onload = async function(event) {
            const encodedBase64 = event.target.result;
            try {
                const response = await fetch(`${API_BASE_URL}/group/avatar`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ groupId: groupId, groupPicture: encodedBase64 })
                });
                if(response.ok) {
                    alert("Group profile image updated successfully!");
                    profileModalAvatar.innerHTML = `<img src="${encodedBase64}">`;
                    LoadChatrooms({ preserveActive: true });
                }
            }
            catch(e) {
                console.error(e);
            }
        };
        fileBlobReader.readAsDataURL(selectedFile);
    });
}

async function renderGroupMembers(groupId) {
    const section = document.getElementById('modalGroupMembersSection');
    const displayList = document.getElementById('modalGroupMembersList');
    if (!section || !displayList) return;
    section.style.display = 'flex';
    displayList.innerHTML = '<span class="archive-empty-state-text">Loading room members...</span>';
    try {
        const res = await fetch(`${API_BASE_URL}/details?groupId=${groupId}`);
        if (res.ok) {
            const data = await res.json();
            displayList.innerHTML = ''; 
            const members = data.membersList ? data.membersList.split(',') : [];
            const groupOwnerName = data.owner;
            const addMemberHeaderBtn = document.createElement('button');
            addMemberHeaderBtn.className = "add-member-trigger-btn";
            addMemberHeaderBtn.innerHTML = '<i class="fas fa-plus"></i> Add Member from Contacts';
            addMemberHeaderBtn.addEventListener('click', () => {
                openGroupAddMemberModal(groupId);
            });
            displayList.appendChild(addMemberHeaderBtn);
            if (members.length === 0) {
                displayList.innerHTML += '<span class="archive-empty-state-text">No members found.</span>';
                return;
            }
            members.forEach(username => {
                const isThisUserOwner = (username === groupOwnerName);
                const rowItem = document.createElement('div');
                rowItem.className = 'group-member-row';
                const leftSpan = document.createElement('div');
                leftSpan.className = 'left-side';
                const iconColor = isThisUserOwner ? 'var(--cyan-aqua)' : 'rgba(255,255,255,0.4)';
                leftSpan.innerHTML = `
                    <i class="fas fa-user" style="color: ${iconColor};"></i> 
                    ${username}
                `;
                rowItem.appendChild(leftSpan);
                const rightContainer = document.createElement('div');
                rightContainer.className = 'right-side';
                if (isThisUserOwner) {
                    rightContainer.innerHTML = '<span class="owner-badge">Owner</span>';
                }
                else {
                    const memberLabel = document.createElement('span');
                    memberLabel.className = 'member-badge';
                    memberLabel.textContent = "Member";
                    rightContainer.appendChild(memberLabel);
                    const removeBtn = document.createElement('button');
                    removeBtn.className = 'remove-member-btn';
                    removeBtn.innerHTML = '<i class="fas fa-minus"></i>';
                    removeBtn.title = `Remove ${username}`;
                    removeBtn.addEventListener('click', async (e) => {
                        e.stopPropagation();
                        if (!confirm(`Are you sure you want to remove ${username} from this group?`)) return;
                        try {
                            const response = await fetch(`${API_BASE_URL}/group`, {
                                method: 'POST',
                                headers: { 'Content-Type': 'application/json' },
                                body: JSON.stringify({
                                    action: "removeMember",
                                    groupId: groupId,
                                    memberId: username
                                })
                            });
                            if (response.ok) {
                                alert(`${username} removed successfully!`);
                                renderGroupMembers(groupId);
                            }
                            else {
                                const errData = await response.json();
                                alert(`Error: ${errData.error || 'Failed to remove member'}`);
                            }
                        }
                        catch (error) {
                            console.error('Error removing member:', error);
                            alert('Network error occurred.');
                        }
                    });
                    rightContainer.appendChild(removeBtn);
                }
                rowItem.appendChild(rightContainer);
                displayList.appendChild(rowItem);
            });
        }
    }
    catch(err) {
        console.error("Error displaying members list view context:", err);
        displayList.innerHTML = '<span class="archive-empty-state-text" style="color:#ef4444;">Error parsing items.</span>';
    }
}

async function openGroupAddMemberModal(groupId) {
    if (!groupAddMemberModal || !groupAddMemberContactsList) return;
    groupAddMemberModal.style.zIndex = "2001";
    groupAddMemberModal.classList.add('active');
    groupAddMemberContactsList.innerHTML = '<div class="modal-loading"><i class="fas fa-spinner fa-spin"></i> Loading contacts...</div>';
    try {
        const groupDetailsRes = await fetch(`${API_BASE_URL}/details?groupId=${groupId}`);
        let currentMembers = [];
        if (groupDetailsRes.ok) {
            const groupData = await groupDetailsRes.json();
            currentMembers = groupData.membersList ? groupData.membersList.split(',') : [];
        }
        const response = await fetch(`${API_BASE_URL}/contacts?userId=${CURRENT_USER_ID}`);
        if (!response.ok) throw new Error('Failed to fetch contacts');
        const contacts = await response.json();
        groupAddMemberContactsList.innerHTML = '';
        if (contacts.length === 0) {
            groupAddMemberContactsList.innerHTML = '<div class="modal-loading">No contacts found.</div>';
            return;
        }
        contacts.forEach(contact => {
            const contactName = contact.userName || 'Unknown';
            const contactId = contact.userId;
            const isAlreadyMember = currentMembers.includes(contactName) || currentMembers.includes(contactId);
            const item = document.createElement('div');
            item.className = 'group-member-item' + (isAlreadyMember ? ' already-member' : '');
            item.innerHTML = `
                <div class="room-info">
                    <div class="member-avatar-fallback">
                        ${contactName.charAt(0).toUpperCase()}
                    </div>
                    <div class="member-info-column">
                        <span class="name">${contactName}</span>
                        <span class="uid">ID: ${contactId}</span>
                    </div>
                </div>
                ${isAlreadyMember 
                    ? '<span class="already-member-badge">Already Member</span>' 
                    : '<i class="fas fa-plus-circle add-member-action-icon"></i>'}
            `;
            if (!isAlreadyMember) {
                item.addEventListener('click', async () => {
                    try {
                        const addRes = await fetch(`${API_BASE_URL}/group`, {
                            method: 'POST',
                            headers: { 'Content-Type': 'application/json' },
                            body: JSON.stringify({
                                action: "addMember",
                                groupId: groupId,
                                memberId: contactId
                            })
                        });
                        if (addRes.ok) {
                            alert(`${contactName} added to the group successfully!`);
                            groupAddMemberModal.classList.remove('active');
                            renderGroupMembers(groupId);
                        }
                        else {
                            const err = await addRes.json();
                            alert(`Error: ${err.error || 'Could not add member'}`);
                        }
                    }
                    catch (err) {
                        console.error("Error adding member through modal:", err);
                        alert("Network error occurred.");
                    }
                });
            }
            groupAddMemberContactsList.appendChild(item);
        });
    }
    catch (error) {
        console.error('Error loading add member contacts list:', error);
        groupAddMemberContactsList.innerHTML = '<div class="modal-loading" style="color:#ef4444;">Error loading contacts.</div>';
    }
}

// =============( Group Logs Workflow )=============
async function openGroupLogsWorkflow(groupId) {
    if (!groupLogsModal) return;
    currentLogTab = 'edited';
    logEditedTabBtn.classList.add('active');
    logDeletedTabBtn.classList.remove('active');
    groupLogsModal.classList.add('active');
    groupLogsModal.style.zIndex = "2010";
    await fetchAndRenderGroupLogs(groupId);
}

async function fetchAndRenderGroupLogs(groupId) {
    if (!groupLogsList) return;
    groupLogsList.innerHTML = '<div class="modal-loading"><i class="fas fa-spinner fa-spin"></i> Loading group logs...</div>';
    const endpoint = currentLogTab === 'edited' ? 'edited' : 'deleted';
    try {
        const response = await fetch(`${API_BASE_URL}/group/${endpoint}?groupId=${groupId}`);
        if (!response.ok) throw new Error("Failed to load logs");
        const logs = await response.json();
        groupLogsList.innerHTML = '';
        if (!logs || logs.length === 0) {
            groupLogsList.innerHTML = `<div class="archive-empty-state-text">No ${currentLogTab} messages found in history.</div>`;
            return;
        }
        logs.forEach(msg => {
            const timestampVal = msg.timestamp || msg.time;
            const date = new Date(timestampVal);
            let formattedTime = "Invalid Date";
            if (!isNaN(date.getTime())) {
                const hour = String(date.getHours()).padStart(2, "0");
                const minute = String(date.getMinutes()).padStart(2, "0");
                formattedTime = `${hour}:${minute}`;
            }
            const logRow = document.createElement('div');
            logRow.className = 'message incoming';
            logRow.style.margin = '10px 0';
            logRow.style.display = 'flex';
            logRow.style.flexDirection = 'column';
            logRow.style.alignItems = 'flex-end';
            logRow.innerHTML = `
                <div style="font-size: 11px; opacity: 0.6; margin-bottom: 2px; padding-right: 8px;">
                    <i class="fas fa-user"></i> <b>${msg.senderId}</b>
                </div>
                <div class="message-bubble" style="max-width: 100%; width: 100%; word-break: break-all;">
                    ${msg.text}
                    <div style="display: flex; justify-content: flex-end; font-size: 10px; opacity: 0.5; margin-top: 4px;">
                        <span>${formattedTime}</span>
                    </div>
                </div>
            `;
            groupLogsList.appendChild(logRow);
        });
    }
    catch (err) {
        console.error("Error loading logs:", err);
        groupLogsList.innerHTML = '<div class="archive-empty-state-text" style="color:#ef4444;">Error fetching group logs.</div>';
    }
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
    }
    catch (error) {
        console.error('Error getting contacts:', error);
        modalContactsList.innerHTML = '<div class="modal-loading" style="color:#ef4444;">Error loading contacts!</div>';
    }
}