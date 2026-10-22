
async function sendRequest(url, data) {
    const response = await fetch(url, {
        method: "POST",
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });

    const responseText = await response.text();

    console.log("Request URL:", url);
    console.log("HTTP status:", response.status);
    console.log("Response body:", responseText);
    console.log("Submitted field names:", Object.keys(data));

    let result = {};

    try {
        result = responseText ? JSON.parse(responseText) : {};
    } catch {
        result = { message: responseText };
    }

    if (!response.ok) {
        throw new Error(
            result.message ||
            result.detail ||
            result.error ||
            `Request failed with status ${response.status}.`
        );
    }

    return result;
}

const messageElement = document.getElementById("message");

function displayMessage(message, type = "error") {
    if (!messageElement) return;

    messageElement.textContent = message;
    messageElement.className = "form-message";

    if (type === "success") {
        messageElement.classList.add("success-message");
    } else {
        messageElement.classList.add("error-message");
    }
}

// STUDENT REGISTRATION
const registerForm = document.getElementById("registerForm");

if (registerForm) {
    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const button = registerForm.querySelector('button[type="submit"]');
        button.disabled = true;
        displayMessage("Creating your account...", "success");

        const data = {
            fullName: document.getElementById("fullName").value.trim(),
            email: document.getElementById("email").value.trim(),
            phone: document.getElementById("phone").value.trim() || null,
            password: document.getElementById("password").value
        };

        try {
            const result = await sendRequest("/api/auth/register", data);

            displayMessage(
                result.message || "Registration successful!",
                "success"
            );

            registerForm.reset();

            setTimeout(() => {
                window.location.href = "/login.html";
            }, 1200);

        } catch (error) {
            console.error("Registration failed:", error);
            displayMessage(error.message);

        } finally {
            button.disabled = false;
        }
    });
}

// STUDENT LOGIN
const loginForm = document.getElementById("loginForm");

if (loginForm) {
    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        const button = loginForm.querySelector('button[type="submit"]');
        button.disabled = true;
        displayMessage("Logging in...", "success");

        const data = {
            email: document.getElementById("email").value.trim(),
            password: document.getElementById("password").value
        };

        try {
            const result = await sendRequest("/api/auth/login", data);

            displayMessage(
                result.message || "Login successful!",
                "success"
            );

            setTimeout(() => {
                window.location.href = "/";
            }, 500);

        } catch (error) {
            console.error("Login failed:", error);
            displayMessage(error.message);

        } finally {
            button.disabled = false;
        }
    });
}
