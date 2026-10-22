
/* ==================================
   MY APPLICATIONS PAGE
================================== */

const applicationsList =
    document.getElementById("applicationsList");

const applicationsMessage =
    document.getElementById("applicationsMessage");

const totalApplications =
    document.getElementById("totalApplications");

const pendingApplications =
    document.getElementById("pendingApplications");

const acceptedApplications =
    document.getElementById("acceptedApplications");

const logoutButton =
    document.getElementById("logoutButton");


/* ==================================
   FORMAT APPLICATION DATE
================================== */

function formatApplicationDate(dateValue) {
    if (!dateValue) {
        return "Date not available";
    }

    const date = new Date(dateValue.replace(" ", "T"));

    if (Number.isNaN(date.getTime())) {
        return dateValue;
    }

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });
}


/* ==================================
   GET STATUS CLASS
================================== */

function getStatusClass(status) {
    switch ((status || "").toLowerCase()) {
        case "accepted":
            return "status-accepted";

        case "rejected":
            return "status-rejected";

        case "pending":
            return "status-pending";

        case "submitted":
            return "status-submitted";

        default:
            return "status-default";
    }
}


/* ==================================
   CREATE AN APPLICATION CARD
================================== */

function createApplicationCard(application) {
    const card = document.createElement("article");
    card.className = "application-card";

    const topRow = document.createElement("div");
    topRow.className = "application-card-top";

    const title = document.createElement("h3");
    title.textContent =
        application.internshipTitle || "Internship";

    const status = document.createElement("span");
    status.className =
        "application-status " +
        getStatusClass(application.status);

    status.textContent = application.status || "Submitted";

    topRow.append(title, status);

    const company = document.createElement("p");
    company.className = "application-company";
    company.textContent =
        application.company || "Company not available";

    const appliedDate = document.createElement("p");
    appliedDate.className = "application-date";
    appliedDate.textContent =
        "Applied on: " +
        formatApplicationDate(application.appliedAt);

    const applicationId = document.createElement("p");
    applicationId.className = "application-id";
    applicationId.textContent =
        "Application ID: " + application.id;

    card.append(
        topRow,
        company,
        appliedDate,
        applicationId
    );

    return card;
}


/* ==================================
   LOAD MY APPLICATIONS
================================== */

async function loadMyApplications() {
    applicationsList.replaceChildren();

    applicationsMessage.textContent =
        "Loading your applications...";

    applicationsMessage.className =
        "applications-message";

    try {
        const response = await fetch("/api/applications/my", {
            method: "GET",
            credentials: "same-origin",
            headers: {
                "Accept": "application/json"
            }
        });

        if (response.status === 401) {
            applicationsMessage.textContent =
                "Please log in to view your applications.";

            window.location.replace("/login.html");
            return;
        }

        if (!response.ok) {
            throw new Error(
                "Unable to retrieve applications. HTTP " +
                response.status
            );
        }

        const applications = await response.json();

        totalApplications.textContent = applications.length;

        pendingApplications.textContent =
            applications.filter(application =>
                (application.status || "").toLowerCase() === "pending" ||
                (application.status || "").toLowerCase() === "submitted"
            ).length;

        acceptedApplications.textContent =
            applications.filter(application =>
                (application.status || "").toLowerCase() === "accepted"
            ).length;

        if (applications.length === 0) {
            applicationsMessage.textContent =
                "You haven't applied for any internships yet. " +
                "Explore available opportunities and apply today.";

            applicationsMessage.className =
                "applications-message empty-message";

            return;
        }

        applicationsMessage.textContent =
            applications.length +
            (applications.length === 1
                ? " application found."
                : " applications found.");

        applications.forEach(application => {
            const card = createApplicationCard(application);
            applicationsList.appendChild(card);
        });

    } catch (error) {
        console.error("Error loading applications:", error);

        applicationsMessage.textContent =
            "We couldn't load your applications. " +
            "Please check your connection and try again.";

        applicationsMessage.className =
            "applications-message error-message";
    }
}


/* ==================================
   LOGOUT
================================== */

logoutButton.addEventListener("click", async () => {
    logoutButton.disabled = true;
    logoutButton.textContent = "Logging out...";

    try {
        const response = await fetch("/api/auth/logout", {
            method: "POST",
            credentials: "same-origin"
        });

        if (!response.ok) {
            throw new Error("Logout failed");
        }

        window.location.replace("/login.html");

    } catch (error) {
        console.error("Logout error:", error);

        applicationsMessage.textContent =
            "Could not log out. Please try again.";

        applicationsMessage.className =
            "applications-message error-message";

        logoutButton.disabled = false;
        logoutButton.textContent = "Logout";
    }
});


/* ==================================
   START
================================== */

loadMyApplications();
