document.addEventListener("DOMContentLoaded", () => {
    console.log("welcome.js loaded");

    const modal = document.getElementById("authModal");
    const openLoginModal = document.getElementById("openLoginModal");
    const openSignupModal = document.getElementById("openSignupModal");
    const closeAuthModal = document.getElementById("closeAuthModal");

    const loginForm = document.getElementById("loginForm");
    const signupForm = document.getElementById("signupForm");

    const switchToSignup = document.getElementById("switchToSignup");
    const switchToLogin = document.getElementById("switchToLogin");

    const recoverForm = document.getElementById("recoverForm");
    const switchToRecover = document.getElementById("switchToRecover");
    const recoverToLogin = document.getElementById("recoverToLogin");

    const API_BASE_URL = 'http://localhost:8081/api';

    if (!modal || !openLoginModal || !openSignupModal || !closeAuthModal || !loginForm || !signupForm || !switchToSignup || !switchToLogin || !recoverForm || !switchToRecover || !recoverToLogin) {
        console.error("Some modal elements were not found.");
        return;
    }

    function showLogin() {
        modal.classList.add("active");
        loginForm.classList.add("active");
        signupForm.classList.remove("active");
        if (recoverForm) recoverForm.classList.remove("active");
    }

    function showSignup() {
        modal.classList.add("active");
        signupForm.classList.add("active");
        loginForm.classList.remove("active");
        recoverForm.classList.remove("active");
    }

    function showRecover() {
        modal.classList.add("active");
        recoverForm.classList.add("active");
        loginForm.classList.remove("active");
        signupForm.classList.remove("active");
    }

    function closeModal() {
        modal.classList.remove("active");
    }

    switchToRecover.addEventListener("click", (event) => {
        event.preventDefault();
        showRecover();
    });

    recoverToLogin.addEventListener("click", (event) => {
        event.preventDefault();
        showLogin();
    });

    openLoginModal.addEventListener("click", (event) => {
        event.preventDefault();
        showLogin();
    });

    openSignupModal.addEventListener("click", (event) => {
        event.preventDefault();
        showSignup();
    });

    closeAuthModal.addEventListener("click", closeModal);
    switchToSignup.addEventListener("click", showSignup);
    switchToRecover.addEventListener("click", showRecover);
    switchToLogin.addEventListener("click", showLogin);

    modal.addEventListener("click", (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const username = document.getElementById("loginUserId").value.trim();
        const password = document.getElementById("loginPassword").value.trim();

        if (!username || !password) {
            alert("Please fill in all fields.");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    id: "",        
                    user: username,
                    fullName: username,
                    pass: password
                })
            });

            const data = await response.json();

            if (response.status === 200) {
                localStorage.setItem("CURRENT_USER_ID", data.id);
                window.location.href = "chat.html";
            } else {
                alert(data.error || "Login failed");
            }
        } catch (error) {
            console.error("Error during login:", error);
            alert("Could not connect to server.");
        }
    });

    signupForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const fullname = document.getElementById("signupFullname").value.trim();
        const username = document.getElementById("signupUserId").value.trim();
        const password = document.getElementById("signupPassword").value.trim();
        const repeatPassword = document.getElementById("signupRepeatPassword").value.trim();

        if (!fullname || !username || !password || !repeatPassword) {
            alert("Please fill in all fields.");
            return;
        }

        if (password !== repeatPassword) {
            alert("Passwords do not match!");
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    id: "", 
                    user: username,
                    fullName: fullname,
                    pass: password
                })
            });

            const data = await response.json();

            if (response.status === 201) {
                localStorage.setItem("CURRENT_USER_ID", data.id);
                window.location.href = "chat.html";
            } else {
                alert(data.error || "Sign up failed");
                console.error(data.error || "Sign up failed");
            }
        } catch (error) {
            console.error("Error during sign up:", error);
        }
    });

    recoverForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const username = document.getElementById("recoverUserId").value.trim();
        const newPassword = document.getElementById("recoverNewPassword").value.trim();
        if (!username || !newPassword) {
            alert("Please fill in all fields.");
            return;
        }
        try {
            const response = await fetch(`${API_BASE_URL}/user/recover`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    user: username,
                    newPass: newPassword
                })
            });
            if (response.status === 200) {
                alert("Password updated successfully! You can now login.");
                showLogin();
            } else {
                const data = await response.json();
                alert(data.error || "Failed to update password.");
            }
        } catch (error) {
            console.error("Error during password recovery:", error);
            alert("Could not connect to server.");
        }
    });
});