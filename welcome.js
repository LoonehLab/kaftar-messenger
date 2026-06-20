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

    if (
        !modal ||
        !openLoginModal ||
        !openSignupModal ||
        !closeAuthModal ||
        !loginForm ||
        !signupForm ||
        !switchToSignup ||
        !switchToLogin
    ) {
        console.error("Some modal elements were not found.");
        return;
    }

    function showLogin() {
        modal.classList.add("active");
        loginForm.classList.add("active");
        signupForm.classList.remove("active");
    }

    function showSignup() {
        modal.classList.add("active");
        signupForm.classList.add("active");
        loginForm.classList.remove("active");
    }

    function closeModal() {
        modal.classList.remove("active");
    }

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

    switchToLogin.addEventListener("click", showLogin);

    modal.addEventListener("click", (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });

    loginForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const username = document.getElementById("loginUsername").value.trim();
        const password = document.getElementById("loginPassword").value.trim();

        if (!username || !password) {
            alert("Please enter username and password.");
            return;
        }

        console.log("Login:", username, password);

        window.location.href = "chat.html";
    });

    signupForm.addEventListener("submit", (event) => {
        event.preventDefault();

        const username = document.getElementById("signupUsername").value.trim();
        const password = document.getElementById("signupPassword").value.trim();

        if (!username || !password) {
            alert("Please enter username and password.");
            return;
        }

        console.log("Signup:", username, password);

        window.location.href = "chat.html";
    });
});

