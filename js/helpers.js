// =============( Profile Avatar )=============
function getFirstName(fullName, fallback = "") {
    if (fullName && fullName.trim()) return fullName.trim().split(/\s+/)[0];
    return fallback;
}

const pvDisplayNameCache = new Map();

async function resolvePvDisplayName(targetId, username) {
    if (pvDisplayNameCache.has(targetId))
        return pvDisplayNameCache.get(targetId);
    try {
        const res = await fetch(`${API_BASE_URL}/details?userId=${targetId}`);
        if (res.ok) {
            const data = await res.json();
            const displayName = data.fullName || username;
            pvDisplayNameCache.set(targetId, displayName);
            return displayName;
        }
    }
    catch (err) {
        console.error("Error resolving display name:", err);
    }
    return username;
}

function getAvatarHTML(name, base64Image) {
    if (base64Image === "saved_messages_icon") {
        return `<div class="room-avatar-saved-messages">
                    <i class="fas fa-bookmark"></i>
                </div>`;
    }
    if (base64Image && base64Image.trim() !== "")
        return `<img src="${base64Image}">`;
    return name ? name.trim().charAt(0).toUpperCase() : '?';
}

function updateMainUserAvatar(fullName, base64Image) {
    let avatarHTML = getAvatarHTML(fullName, base64Image);
    if (base64Image && base64Image.trim() !== "")
        avatarHTML = `<img src="${base64Image}" class="user-main-img">`;
    if (mainUserAvatar) mainUserAvatar.innerHTML = avatarHTML;
    if (profileModalAvatar) profileModalAvatar.innerHTML = avatarHTML;
}