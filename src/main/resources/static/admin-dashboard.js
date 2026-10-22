
const dashboardMessage = document.getElementById("dashboardMessage");
const logoutButton = document.getElementById("logoutButton");

async function loadDashboard() {
    try {
        const response = await fetch("/api/admin/dashboard/stats", {
            method: "GET",
            credentials: "same-origin"
        });

        const data = await response.json();

        if (response.status === 401) {
            window.location.replace("/admin-login.html");
            return;
        }

        if (!response.ok) {
            throw new Error(
                data.message || "Unable to load dashboard statistics."
            );
        }

        document.getElementById("totalInternships").textContent =
            data.totalInternships;

        document.getElementById("totalStudents").textContent =
            data.totalStudents;

        document.getElementById("totalApplications").textContent =
            data.totalApplications;

    } catch (error) {
        dashboardMessage.textContent =
            error.message || "Unable to connect to the server.";
    }
}

logoutButton.addEventListener("click", async function () {
    logoutButton.disabled = true;

    try {
        const response = await fetch("/api/admin/auth/logout", {
            method: "POST",
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Logout failed. Please try again.");
        }

        window.location.replace("/admin-login.html");

    } catch (error) {
        dashboardMessage.textContent = error.message;
        logoutButton.disabled = false;
    }
});

loadDashboard();
