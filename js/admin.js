const API_BASE_URL = 'http://localhost:8081/api';

let usersData = [];
let groupsData = [];
let reportsData = [];
let currentGroupContextId = null;

document.addEventListener("DOMContentLoaded", () => {
    checkAdminSession();
    const loginForm = document.getElementById('adminLoginForm');
    if (loginForm) loginForm.addEventListener('submit', handleAdminLogin);

    const logoutBtn = document.getElementById('adminLogoutBtn');
    if (logoutBtn) logoutBtn.addEventListener('click', handleAdminLogout);

    const menuItems = document.querySelectorAll('.chatroom-item');
    const sections = document.querySelectorAll('.admin-section');
    const headerTitle = document.getElementById('headerTitle');
    const headerSubtitle = document.getElementById('headerSubtitle');
    const headerIcon = document.getElementById('headerIcon');

    menuItems.forEach(item => {
        item.addEventListener('click', () => {
            menuItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            const target = item.getAttribute('data-target');
            sections.forEach(sec => {
                if(sec.id === target)  sec.style.display = "block";
                else  sec.style.display = "none";
            });
            if (target === "usersSection") {
                headerTitle.textContent = "Manage Users";
                headerSubtitle.textContent = "Operations for Kaftar Users";
                headerIcon.innerHTML = `<i class="fa-solid fa-users"></i>`;
            }
            else if (target === "groupsSection") {
                headerTitle.textContent = "Manage Groups";
                headerSubtitle.textContent = "Group Members Management";
                headerIcon.innerHTML = `<i class="fa-solid fa-user-group"></i>`;
            }
            else if (target === "reportsSection") {
                headerTitle.textContent = "Reported Messages";
                headerSubtitle.textContent = "Review Message Reports";
                headerIcon.innerHTML = `<i class="fa-solid fa-triangle-exclamation"></i>`;
            }
        });
    });

    document.getElementById('userSearchInput').addEventListener('input', (e) => {
        const query = e.target.value.toLowerCase();
        const rows = document.querySelectorAll('#usersTableBody tr');
        rows.forEach(row => {
            const userCol = row.cells[1].textContent.toLowerCase();
            row.style.display = userCol.includes(query) ? "" : "none";
        });
    });

    const themeBtn = document.getElementById('themeToggleBtn');
    themeBtn.addEventListener('click', () => {
        const root = document.documentElement;
        const currentTheme = root.getAttribute('data-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        root.setAttribute('data-theme', newTheme);
        themeBtn.innerHTML = newTheme === 'dark' ? `<i class="fa-solid fa-moon"></i>` : `<i class="fa-solid fa-sun"></i>`;
    });
});

function checkAdminSession() {
    const isAdminLoggedIn = localStorage.getItem("adminLoggedIn");
    const loginOverlay = document.getElementById('adminLoginOverlay');
    const mainContainer = document.getElementById('adminMainContainer');
    if (isAdminLoggedIn === "true") {
        loginOverlay.style.display = "none";
        mainContainer.style.display = "flex";
        loadAllData();
    }
    else {
        loginOverlay.style.display = "flex";
        mainContainer.style.display = "none";
    }
}

async function handleAdminLogin(event) {
    event.preventDefault();
    const usernameInput = document.getElementById('adminUsername').value.trim();
    const passwordInput = document.getElementById('adminPassword').value;
    try {
        const response = await fetch(`${API_BASE_URL}/admin/login`, {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify({ username: usernameInput, password: passwordInput })
        });
        const rawText = await response.text();
        let data = {};
        try {
            data = JSON.parse(rawText);
        }
        catch(e) {
            console.error("Response is not JSON:", rawText);
        }
        if (response.ok && (data.status === 'success' || response.status === 200)) {
            alert(data.message || "Welcome Admin!");
            localStorage.setItem("adminLoggedIn", "true");
            localStorage.setItem("adminUsername", usernameInput);
            checkAdminSession();
        } 
        else {
            const errorMsg = data.error || data.message || "Invalid credentials!";
            alert(`Login Failed: ${errorMsg}`);
        }
    }
    catch (error) {
        console.error("Login failed:", error);
        alert("Connection error!");
    }
}

function handleAdminLogout() {
    if (confirm("Are you sure you want to logout?")) {
        localStorage.removeItem("adminLoggedIn");
        localStorage.removeItem("adminUsername");
        checkAdminSession();
    }
}

async function loadAllData() {
    await Promise.all([
        fetchUsers(),
        fetchGroups(),
        fetchReports()
    ]);
    renderAll();
}

async function fetchUsers() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/users`);
        if (response.ok) usersData = await response.json();
    }
    catch (error) {
        console.error("Error fetching users:", error);
    }
}

async function fetchGroups() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/groups`);
        if (response.ok) groupsData = await response.json();
    }
    catch (error) {
        console.error("Error fetching groups:", error);
    }
}

async function fetchReports() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/reports`);
        if (response.ok) reportsData = await response.json();
    }
    catch (error) {
        console.error("Error fetching reports:", error);
    }
}

function renderAll() {
    renderUsers();
    renderGroups();
    renderReports();
}

function renderUsers() {
    const tbody = document.getElementById('usersTableBody');
    tbody.innerHTML = usersData.map(u => `
        <tr>
            <td><span class="owner-badge">${u.id}</span></td>
            <td><strong>${u.username || u.user || 'N/A'}</strong></td>
            <td>${u.fullName || 'N/A'}</td>
            <td>${u.phoneNumber || u.phone || 'N/A'}</td>
            <td>
                <button class="remove-member-btn" onclick="deleteUser('${u.id}')" title="Delete User"><i class="fa-solid fa-trash"></i></button>
            </td>
        </tr>
    `).join('');
}

function renderGroups() {
    const container = document.getElementById('groupsContainer');
    container.innerHTML = groupsData.map(g => `
        <div class="group-card">
            <div class="group-card-header">
                <div class="group-card-title">
                    <div class="room-avatar"><i class="fa-solid fa-comments"></i></div>
                    <div>
                        <h4>${g.name}</h4>
                        <span class="member-badge">Owner: ${g.owner}</span>
                    </div>
                </div>
                <button class="remove-member-btn" onclick="deleteGroup('${g.id}')" title="Delete Group"><i class="fa-solid fa-trash"></i></button>
            </div>
            <div class="group-members-list">
                <div class="add-member-trigger-btn" onclick="openAddMemberModal('${g.id}')">
                    <i class="fa-solid fa-plus"></i> Manage Members
                </div>
                ${g.members ? g.members.map(member => `
                    <div class="group-member-row">
                        <div class="left-side"><i class="fa-solid fa-circle-user"></i> ${member.username || member.id || member}</div>
                        <div class="right-side">
                            ${(member.username === g.owner || member === g.owner) ? '<span class="owner-badge">Owner</span>' : `
                                <button class="remove-member-btn" onclick="removeMemberFromGroup('${g.id}', '${member.username || member.id || member}')"><i class="fa-solid fa-xmark"></i></button>
                            `}
                        </div>
                    </div>
                `).join('') : ''}
            </div>
        </div>
    `).join('');
}

function renderReports() {
    const container = document.getElementById('reportsList');
    const badge = document.getElementById('reportsCount');
    badge.textContent = reportsData.length;
    badge.style.display = reportsData.length > 0 ? "flex" : "none";
    if (reportsData.length === 0) {
        container.innerHTML = `<div class="archive-empty-state-text">No active reports. All quiet!</div>`;
        return;
    }
    container.innerHTML = reportsData.map(r => `
        <div class="report-card">
            <div class="report-header">
                <div>
                    <span class="owner-badge">Message ID: ${r.messageId}</span>
                    <span class="member-badge">Sender ID: ${r.senderId}</span>
                </div>
                <div class="report-actions">
                    <!-- دیگر هیچ پارامتری پاس داده نمی‌شود، چون همه‌چیز یکجا پاک خواهد شد -->
                    <button class="btn-danger-outline" onclick="dismissAllReports()" style="padding: 4px 10px; font-size: 12px; margin: 0;">
                        <i class="fa-solid fa-trash"></i> Clear All Reports
                    </button>
                </div>
            </div>
            <div class="report-body">
                <blockquote class="reported-text">"${r.content || r.message || 'No Content'}"</blockquote>
            </div>
        </div>
    `).join('');
}

async function deleteUser(userId) {
    if(confirm(`Are you sure you want to remove user ${userId}?`)) {
        try {
            const response = await fetch(`${API_BASE_URL}/user/delete`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId: userId })
            });
            const data = await response.json();
            if (response.ok && data.status === 'success') {
                alert(data.message || "User deleted successfully");
                await loadAllData();
            }
            else alert(`Error: ${data.message || "Failed to delete user"}`);
        }
        catch (error) {
            console.error("Error deleting user:", error);
            alert("Network error deleting user");
        }
    }
}

async function deleteGroup(groupId) {
    if(confirm(`Remove group ${groupId} for all members?`)) {
        try {
            const response = await fetch(`${API_BASE_URL}/group`, {
                method: 'DELETE',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ groupId: groupId })
            });
            const data = await response.json();
            if (response.ok && data.status === 'success') {
                alert(data.message || "Group deleted successfully");
                await loadAllData();
            }
            else  alert(`Error: ${data.message || "Failed to delete group"}`);
        }
        catch (error) {
            console.error("Error deleting group:", error);
            alert("Network error deleting group");
        }
    }
}

async function dismissAllReports() {
    try {
        const response = await fetch(`${API_BASE_URL}/admin/reports`, {
            method: 'DELETE', 
            headers: { 
                'Content-Type': 'application/json'
            }
        });
        const rawText = await response.text();
        let data = {};
        try { data = JSON.parse(rawText); } catch(e) {}
        if (response.ok && (data.status === 'success' || response.status === 200)) {
            alert(data.message || "All reports cleared successfully");
            await loadAllData(); 
        }
        else alert(`Error: ${data.error || data.message || "Failed to clear reports"}`);
    }
    catch (error) {
        console.error("Error clearing reports:", error);
        alert("Network error clearing reports");
    }
}

function openModal(id) {
    document.getElementById(id).classList.add('active');
}

function closeModal(id) {
    document.getElementById(id).classList.remove('active');
}

function openAddMemberModal(groupId) {
    currentGroupContextId = groupId;
    openModal('manageMembersModal');
}

function validatePassword(password) {
    const minLength = password.length >= 8;
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecialChar = /[!$#@%&^*]/.test(password);
    return minLength && hasUpperCase && hasLowerCase && hasNumber && hasSpecialChar;
}

async function handleFormSubmit(event, type) {
    event.preventDefault();
    if (type === 'user') {
        const username = document.getElementById('newUsername').value.trim();
        const fullName = document.getElementById('newFullName').value.trim();
        const password = document.getElementById('newPassword').value;
        if (!validatePassword(password)) {
            alert("Password is week!");
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ user: username, fullName: fullName, pass: password })
            });
            const data = await response.json();
            if (response.ok && (data.status === 'created' || data.status === 'success')) {
                alert("User registered successfully!");
                closeModal('addUserModal');
                document.getElementById('addUserForm').reset();
                await loadAllData();
            }
            else alert(`Error: ${data.message || "Failed to register user"}`);
        }
        catch (error) {
            console.error("Error registering user:", error);
            alert("Network error registering user");
        }
    }
    else if (type === 'group') {
        const ownerId = document.getElementById('groupOwnerId').value.trim();
        const groupName = document.getElementById('groupNameInput').value.trim();
        try {
            const response = await fetch(`${API_BASE_URL}/group`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ action: "create", ownerId: ownerId, name: groupName })
            });
            const data = await response.json();
            if (response.ok && data.status === 'success') {
                alert("Group created successfully!");
                closeModal('addGroupModal');
                document.getElementById('addGroupForm').reset();
                await loadAllData();
            }
            else alert(`Error: ${data.status || "Failed to create group"}`);
        }
        catch (error) {
            console.error("Error creating group:", error);
            alert("Network error creating group");
        }
    }
}

async function handleAddMemberSubmit(event) {
    event.preventDefault();
    const memberId = document.getElementById('addMemberUserId').value.trim();
    if (!currentGroupContextId || !memberId) return;
    try {
        const response = await fetch(`${API_BASE_URL}/group`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ action: "addMember", groupId: currentGroupContextId, memberId: memberId })
        });
        const data = await response.json();
        if (response.ok && data.status === 'success') {
            alert(data.message || "Member added successfully");
            closeModal('manageMembersModal');
            document.getElementById('addMemberUserId').value = '';
            await loadAllData();
        }
        else alert(`Error: ${data.message || "Failed to add member"}`);
    }
    catch (error) {
        console.error("Error adding member:", error);
        alert("Network error adding member");
    }
}

async function removeMemberFromGroup(groupId, memberUsername) {
    if (confirm(`Are you sure you want to remove ${memberUsername}?`)) {
        try {
            const response = await fetch(`${API_BASE_URL}/group`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ action: "removeMember", groupId: groupId, memberId: memberUsername })
            });
            const data = await response.json();
            if (response.ok && data.status === 'success') {
                alert(data.message || "Member removed successfully");
                await loadAllData();
            }
            else alert(`Error: ${data.message || "Failed to remove member"}`);
        }
        catch (error) {
            console.error("Error removing member:", error);
            alert("Network error removing member");
        }
    }
}