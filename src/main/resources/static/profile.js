
/* =====================================
   INTERNCONNECT STUDENT PROFILE
===================================== */

const profileForm = document.getElementById("profileForm");
const fullNameInput = document.getElementById("fullName");
const emailInput = document.getElementById("email");
const phoneInput = document.getElementById("phone");

const profileMessage = document.getElementById("profileMessage");
const profileDisplayName = document.getElementById("profileDisplayName");
const profileAvatar = document.getElementById("profileAvatar");

const saveProfileButton =
    document.getElementById("saveProfileButton");

const logoutButton = document.getElementById("logoutButton");


/* =====================================
   SHOW A MESSAGE
===================================== */

function showProfileMessage(message, type = "info") {
    profileMessage.textContent = message;
    profileMessage.className = "profile-message";

    if (type === "success") {
        profileMessage.classList.add("message-success");
    } else if (type === "error") {
        profileMessage.classList.add("message-error");
    }
}


/* =====================================
   UPDATE PROFILE HEADING
===================================== */

function updateProfileHeading(name) {
    const safeName = name || "Student";

    profileDisplayName.textContent = safeName;
    profileAvatar.textContent =
        safeName.trim().charAt(0).toUpperCase() || "S";
}


/* =====================================
   LOAD PROFILE FROM JAVA BACKEND
===================================== */

async function loadProfile() {
    showProfileMessage("Loading your profile...");

    try {
        const response = await fetch("/api/profile", {
            method: "GET",
            credentials: "same-origin",
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            window.location.replace("/login.html");
            return;
        }

        const data = await response.json();

        if (!response.ok) {
            throw new Error(
                data.message || "Could not load your profile."
            );
        }

        fullNameInput.value = data.fullName || "";
        emailInput.value = data.email || "";
        phoneInput.value = data.phone || "";

        updateProfileHeading(data.fullName);

        showProfileMessage("Your profile is up to date.");

    } catch (error) {
        console.error("Profile loading error:", error);

        showProfileMessage(
            error.message || "Unable to load your profile.",
            "error"
        );
    }
}


/* =====================================
   SAVE PROFILE CHANGES
===================================== */

profileForm.addEventListener("submit", async event => {
    event.preventDefault();

    const fullName = fullNameInput.value.trim();
    const phone = phoneInput.value.trim();

    if (!fullName) {
        showProfileMessage("Full name is required.", "error");
        fullNameInput.focus();
        return;
    }

    if (fullName.length > 100) {
        showProfileMessage(
            "Name cannot exceed 100 characters.",
            "error"
        );
        return;
    }

    if (phone && !/^[0-9+() .-]{7,15}$/.test(phone)) {
        showProfileMessage(
            "Enter a valid phone number.",
            "error"
        );
        phoneInput.focus();
        return;
    }

    saveProfileButton.disabled = true;
    saveProfileButton.textContent = "Saving...";

    try {
        const response = await fetch("/api/profile", {
            method: "PUT",
            credentials: "same-origin",
            headers: {
                "Content-Type": "application/json",
                "Accept": "application/json"
            },
            body: JSON.stringify({
                fullName: fullName,
                phone: phone
            })
        });

        const data = await response.json();

        if (response.status === 401) {
            window.location.replace("/login.html");
            return;
        }

        if (!response.ok) {
            throw new Error(
                data.message || "Could not update your profile."
            );
        }

        updateProfileHeading(fullName);

        showProfileMessage(
            data.message || "Profile updated successfully.",
            "success"
        );

    } catch (error) {
        console.error("Profile update error:", error);

        showProfileMessage(
            error.message || "Unable to save your changes.",
            "error"
        );

    } finally {
        saveProfileButton.disabled = false;
        saveProfileButton.textContent = "Save Changes";
    }
});


/* =====================================
   LOGOUT
===================================== */

logoutButton.addEventListener("click", async () => {
    logoutButton.disabled = true;
    logoutButton.textContent = "Logging out...";

    try {
        const response = await fetch("/api/auth/logout", {
            method: "POST",
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Logout failed.");
        }

        window.location.replace("/login.html");

    } catch (error) {
        console.error("Logout error:", error);

        showProfileMessage(
            "Unable to log out. Please try again.",
            "error"
        );

        logoutButton.disabled = false;
        logoutButton.textContent = "Logout";
    }
});


/* =====================================
   START
===================================== */

loadProfile();
