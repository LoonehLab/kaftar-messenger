document.addEventListener('DOMContentLoaded', () => {
    const chatroomItems = document.querySelectorAll('.chatroom-item');
    const currentRoomName = document.querySelector('.current-room-name');
    const sendBtn = document.querySelector('.outer-send-btn');
    const messageInput = document.querySelector('.input-container input');
    const messagesContainer = document.querySelector('.messages-container');

    // Mobile Chatroom Switch
    const appContainer = document.querySelector('.app-container');
    chatroomItems.forEach(item => {
        item.addEventListener('click', () => {
            chatroomItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');
            const selectedRoomName = item.querySelector('.room-name').textContent;
            currentRoomName.textContent = selectedRoomName;
            appContainer.classList.add('chat-open');
        });
    });

    // Mobile Back Button
    const backBtn = document.querySelector('.back-btn');
    if (backBtn) {
        backBtn.addEventListener('click', () => {
            appContainer.classList.remove('chat-open');
        });
    }

    // Message Send Functionality
    function sendMessage() {
        const text = messageInput.value.trim();
        if (text !== '') {
            const messageDiv = document.createElement('div');
            messageDiv.classList.add('message', 'outgoing');
            messageDiv.innerHTML = `
                <div class="message-bubble">
                    ${text}
                </div>
            `;
            messagesContainer.appendChild(messageDiv);
            messageInput.value = '';
            messagesContainer.scrollTop = messagesContainer.scrollHeight;
        }
    }
    sendBtn.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
});