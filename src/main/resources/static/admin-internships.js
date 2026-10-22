
const form = document.getElementById("internshipForm");
const formTitle = document.getElementById("formTitle");
const formMessage = document.getElementById("formMessage");
const listMessage = document.getElementById("listMessage");
const internshipList = document.getElementById("internshipList");
const saveButton = document.getElementById("saveButton");
const cancelButton = document.getElementById("cancelButton");

const fields = [
    "title",
    "company",
    "location",
    "internshipType",
    "workMode",
    "duration",
    "stipend",
    "deadline",
    "skills",
    "description"
];

function getValue(id) {
    return document.getElementById(id).value.trim();
}

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

    const text = await response.text();
    let data = {};

    try {
        data = text ? JSON.parse(text) : {};
    } catch {
        data = {};
    }

    if (response.status === 401) {
        window.location.replace("/admin-login.html");
        throw new Error("Please log in as an administrator.");
    }

    if (!response.ok) {
        throw new Error(data.message || "Request failed.");
    }

    return data;
}

function resetForm() {
    form.reset();
    document.getElementById("internshipId").value = "";
    formTitle.textContent = "Add New Internship";
    saveButton.textContent = "Add Internship";
    cancelButton.hidden = true;
    formMessage.textContent = "";
}

function getInternshipData() {
    const data = {};

    for (const field of fields) {
        data[field] = getValue(field);
    }

    return data;
}

form.addEventListener("submit", async event => {
    event.preventDefault();

    saveButton.disabled = true;
    formMessage.textContent = "";

    const id = document.getElementById("internshipId").value;
    const data = getInternshipData();

    try {
        const url = id
            ? `/api/admin/internships/${id}`
            : "/api/admin/internships";

        await apiRequest(url, {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(data)
        });

        alert(id
            ? "Internship updated successfully."
            : "Internship added successfully.");

        resetForm();
        await loadInternships();

    } catch (error) {
        formMessage.textContent = error.message;
    } finally {
        saveButton.disabled = false;
    }
});

function editInternship(internship) {
    document.getElementById("internshipId").value = internship.id;

    const values = {
        title: internship.title,
        company: internship.company,
        location: internship.location,
        internshipType: internship.internship_type,
        workMode: internship.work_mode,
        duration: internship.duration,
        stipend: internship.stipend,
        deadline: internship.deadline,
        skills: internship.skills,
        description: internship.description
    };

    for (const [id, value] of Object.entries(values)) {
    const element = document.getElementById(id);
    element.value = value == null
        ? ""
        : (id === "deadline"
            ? String(value).slice(0, 10)
            : String(value));
}

    formTitle.textContent = "Edit Internship";
    saveButton.textContent = "Save Changes";
    cancelButton.hidden = false;
    formMessage.textContent = "";

    window.scrollTo({ top: 0, behavior: "smooth" });
}

async function deleteInternship(internship) {
    const confirmed = confirm(
        `Delete "${internship.title}" at "${internship.company}"?\n\n` +
        "Important: existing applications may also be deleted by the database."
    );

    if (!confirmed) return;

    listMessage.textContent = "";

    try {
        await apiRequest(`/api/admin/internships/${internship.id}`, {
            method: "DELETE"
        });

        listMessage.style.color = "#15803d";
        listMessage.textContent = "Internship deleted successfully.";
        await loadInternships();

    } catch (error) {
        listMessage.style.color = "#b91c1c";
        listMessage.textContent = error.message;
    }
}

async function loadInternships() {
    listMessage.style.color = "#64748b";
    listMessage.textContent = "Loading internships...";
    internshipList.replaceChildren();

    try {
        const internships = await apiRequest("/api/admin/internships");

        listMessage.textContent = `${internships.length} internship(s) found.`;

        if (internships.length === 0) {
            internshipList.textContent = "No internships found.";
            return;
        }

        internshipList.innerHTML = internships.map(item => `
            <article class="internship-item">
                <h3>${escapeHtml(item.title)}</h3>
                <p><strong>Company:</strong> ${escapeHtml(item.company)}</p>
                <p class="internship-meta">
                    <strong>Location:</strong> ${escapeHtml(item.location || "Not specified")}
                    &nbsp;|&nbsp;
                    <strong>Mode:</strong> ${escapeHtml(item.work_mode || "Not specified")}
                </p>
                <p><strong>Type:</strong> ${escapeHtml(item.internship_type || "Not specified")}</p>
                <p><strong>Duration:</strong> ${escapeHtml(item.duration || "Not specified")}</p>
                <p><strong>Stipend:</strong> ${escapeHtml(item.stipend || "Not specified")}</p>
                <p><strong>Skills:</strong> ${escapeHtml(item.skills || "Not specified")}</p>
                <p><strong>Deadline:</strong> ${escapeHtml(item.deadline || "Not specified")}</p>
                <p>${escapeHtml(item.description || "No description provided.")}</p>
                <div class="item-actions">
                    <button type="button" class="edit-button"
                        data-action="edit" data-id="${Number(item.id)}">
                        Edit
                    </button>
                    <button type="button" class="delete-button"
                        data-action="delete" data-id="${Number(item.id)}">
                        Delete
                    </button>
                </div>
            </article>
        `).join("");

        internshipList.querySelectorAll("button[data-action]").forEach(button => {
            button.addEventListener("click", () => {
                const internship = internships.find(
                    item => Number(item.id) === Number(button.dataset.id)
                );

                if (!internship) return;

                if (button.dataset.action === "edit") {
                    editInternship(internship);
                } else {
                    deleteInternship(internship);
                }
            });
        });

    } catch (error) {
        listMessage.style.color = "#b91c1c";
        listMessage.textContent = error.message;
    }
}

cancelButton.addEventListener("click", resetForm);

document.getElementById("refreshButton").addEventListener(
    "click", loadInternships
);

document.getElementById("logoutButton").addEventListener("click", async () => {
    try {
        await apiRequest("/api/admin/auth/logout", { method: "POST" });
        window.location.replace("/admin-login.html");
    } catch (error) {
        listMessage.style.color = "#b91c1c";
        listMessage.textContent = error.message;
    }
});

loadInternships();
