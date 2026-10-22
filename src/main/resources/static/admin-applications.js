
const applicationsList = document.getElementById("applicationsList");
const pageMessage = document.getElementById("pageMessage");
const statusFilter = document.getElementById("statusFilter");

let allApplications = [];

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

async function apiRequest(url, options = {}) {
    const response = await fetch(url, {
        ...options,
        credentials: "same-origin",
        headers: {
            ...(options.body ? { "Content-Type": "application/json" } : {}),
            ...options.headers
        }
    });

    const responseText = await response.text();
    let data = {};

    try {
        data = responseText ? JSON.parse(responseText) : {};
    } catch {
        data = {};
    }

    if (response.status === 401) {
        window.location.replace("/admin-login.html");
        throw new Error("Please log in as an administrator.");
    }

    if (!response.ok) {
        throw new Error(data.message || "The request failed.");
    }

    return data;
}

function updateSummary() {
    document.getElementById("totalCount").textContent =
        allApplications.length;

    document.getElementById("pendingCount").textContent =
        allApplications.filter(a => a.status === "Pending").length;

    document.getElementById("acceptedCount").textContent =
        allApplications.filter(a => a.status === "Accepted").length;

    document.getElementById("rejectedCount").textContent =
        allApplications.filter(a => a.status === "Rejected").length;
}

function formatDate(value) {
    if (!value) return "Not available";

    const date = new Date(value);

    if (Number.isNaN(date.getTime())) {
        return String(value);
    }

    return date.toLocaleString();
}

function renderApplications() {
    const selectedStatus = statusFilter.value;

    const filteredApplications = selectedStatus === "All"
        ? allApplications
        : allApplications.filter(
            application => application.status === selectedStatus
        );

    applicationsList.replaceChildren();

    if (filteredApplications.length === 0) {
        applicationsList.textContent = "No applications found.";
        return;
    }

    applicationsList.innerHTML = filteredApplications.map(application => {
        const statusClass = String(application.status || "Pending")
            .toLowerCase();

        return `
            <article class="application-card">
                <div class="application-top">
                    <div>
                        <h3>${escapeHtml(application.internshipTitle)}</h3>
                        <p>
                            <strong>Company:</strong>
                            ${escapeHtml(application.company)}
                        </p>
                    </div>

                    <span class="status-badge ${escapeHtml(statusClass)}">
                        ${escapeHtml(application.status)}
                    </span>
                </div>

                <p>
                    <strong>Student:</strong>
                    ${escapeHtml(application.studentName)}
                </p>

                <p>
                    <strong>Email:</strong>
                    ${escapeHtml(application.studentEmail)}
                </p>

                <p>
                    <strong>Applied on:</strong>
                    ${escapeHtml(formatDate(application.appliedAt))}
                </p>

                <div class="status-controls">
                    <label for="status-${Number(application.applicationId)}">
                        Update status
                    </label>

                    <select
                        id="status-${Number(application.applicationId)}"
                        data-status-id="${Number(application.applicationId)}">

                        <option value="Pending"
                            ${application.status === "Pending" ? "selected" : ""}>
                            Pending
                        </option>

                        <option value="Accepted"
                            ${application.status === "Accepted" ? "selected" : ""}>
                            Accepted
                        </option>

                        <option value="Rejected"
                            ${application.status === "Rejected" ? "selected" : ""}>
                            Rejected
                        </option>
                    </select>

                    <button
                        type="button"
                        data-save-id="${Number(application.applicationId)}">
                        Save Status
                    </button>
                </div>
            </article>
        `;
    }).join("");

    applicationsList.querySelectorAll("[data-save-id]").forEach(button => {
        button.addEventListener("click", async () => {
            const id = Number(button.dataset.saveId);
            const select = applicationsList.querySelector(
                `[data-status-id="${id}"]`
            );

            await updateApplicationStatus(id, select.value, button);
        });
    });
}

async function updateApplicationStatus(id, status, button) {
    const previousText = button.textContent;

    button.disabled = true;
    button.textContent = "Saving...";
    pageMessage.textContent = "";

    try {
        const result = await apiRequest(
            `/api/admin/applications/${id}/status`,
            {
                method: "PUT",
                body: JSON.stringify({ status })
            }
        );

        const application = allApplications.find(
            item => Number(item.applicationId) === id
        );

        if (application) {
            application.status = status;
        }

        updateSummary();
        renderApplications();

        pageMessage.style.color = "#15803d";
        pageMessage.textContent =
            result.message || "Application status updated.";

    } catch (error) {
        pageMessage.style.color = "#b91c1c";
        pageMessage.textContent = error.message;
    } finally {
        button.disabled = false;
        button.textContent = previousText;
    }
}

async function loadApplications() {
    pageMessage.style.color = "#64748b";
    pageMessage.textContent = "Loading applications...";

    try {
        const data = await apiRequest("/api/admin/applications");

        if (!Array.isArray(data)) {
            throw new Error("Unexpected response from the server.");
        }

        allApplications = data;
        updateSummary();
        renderApplications();

        pageMessage.style.color = "#15803d";
        pageMessage.textContent =
            `${allApplications.length} application(s) loaded.`;

    } catch (error) {
        pageMessage.style.color = "#b91c1c";
        pageMessage.textContent = error.message;
    }
}

statusFilter.addEventListener("change", renderApplications);

document.getElementById("refreshButton").addEventListener(
    "click",
    loadApplications
);

document.getElementById("logoutButton").addEventListener("click", async () => {
    try {
        await apiRequest("/api/admin/auth/logout", {
            method: "POST"
        });

        window.location.replace("/admin-login.html");
    } catch (error) {
        pageMessage.style.color = "#b91c1c";
        pageMessage.textContent = error.message;
    }
});

loadApplications();
