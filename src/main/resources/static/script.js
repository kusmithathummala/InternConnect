
/* ================================
   HTML ELEMENTS
================================ */

const internshipList = document.getElementById("internshipList");
const statusText = document.getElementById("status");
const count = document.getElementById("count");
const searchForm = document.getElementById("searchForm");
const keywordInput = document.getElementById("keyword");

const locationFilter = document.getElementById("locationFilter");
const modeFilter = document.getElementById("modeFilter");
const durationFilter = document.getElementById("durationFilter");
const stipendFilter = document.getElementById("stipendFilter");

const applyButton = document.getElementById("applyFilters");
const clearButton = document.getElementById("clearFilters");

const detailsModal = document.getElementById("detailsModal");
const detailsContent = document.getElementById("detailsContent");
const closeModalButton = document.getElementById("closeModal");

let allInternships = [];



/* ================================
   APPLY FOR AN INTERNSHIP
   WITH REQUIRED RESUME UPLOAD
================================ */

function applyForInternship(internshipId, button) {
    if (!internshipId) {
        alert("Internship ID is missing. Please refresh the page.");
        return;
    }

    // Create a file picker for the student's resume.
    const fileInput = document.createElement("input");
    fileInput.type = "file";
    fileInput.accept = ".pdf,application/pdf";
    fileInput.style.display = "none";

    document.body.appendChild(fileInput);

    fileInput.addEventListener("change", async () => {
        const resume = fileInput.files[0];

        // The student cancelled or did not select a file.
        if (!resume) {
            fileInput.remove();
            return;
        }

        // Validate the file extension.
        if (!resume.name.toLowerCase().endsWith(".pdf")) {
            alert("Please select your resume in PDF format.");
            fileInput.remove();
            return;
        }

        // Validate the file size (maximum 5 MB).
        const maxSize = 5 * 1024 * 1024;

        if (resume.size > maxSize) {
            alert("Your resume must not exceed 5 MB.");
            fileInput.remove();
            return;
        }

        if (resume.size === 0) {
            alert("The selected resume is empty. Please choose another file.");
            fileInput.remove();
            return;
        }

        const originalText = button.textContent;
        button.disabled = true;
        button.textContent = "Submitting...";

        try {
            const formData = new FormData();
            formData.append("resume", resume);

            const response = await fetch(
                `/api/applications/${internshipId}`,
                {
                    method: "POST",
                    credentials: "same-origin",
                    body: formData
                }
            );
            

            const data = await response.json();

            if (response.status === 401) {
                alert("Please log in before applying.");
                window.location.href = "/login.html";
                return;
            }

            if (response.status === 201) {
                alert(
                    data.message ||
                    "Application submitted successfully!"
                );
                button.textContent = "Applied";
                return;
            }

            if (response.status === 409) {
                alert(
                    data.message ||
                    "You have already applied for this internship."
                );
                button.textContent = "Already Applied";
                return;
            }

            console.error("Application failed:", response.status, data);

alert(
    `Application failed (${response.status}): ` +
    (data.message || data.error || JSON.stringify(data))
);

        } catch (error) {
            console.error("Application error:", error);

            alert(
                "Could not connect to the server. Please try again."
            );
        } finally {
            fileInput.remove();

            if (
                button.textContent !== "Applied" &&
                button.textContent !== "Already Applied"
            ) {
                button.disabled = false;
                button.textContent = originalText;
            }
        }
    }, { once: true });

    fileInput.click();
}

/* ================================
   CREATE APPLY BUTTON
================================ */

function createApplyButton(internship) {

    const button = document.createElement("button");

    button.type = "button";
    button.className = "details-button apply-button";
    button.textContent = "Apply Now";

    button.addEventListener("click", () => {
        applyForInternship(internship.id, button);
    });

    return button;
}


/* ================================
   LOAD INTERNSHIPS FROM JAVA API
================================ */

async function loadInternships() {

    internshipList.replaceChildren();

    statusText.textContent = "Loading internships...";
    count.textContent = "";

    try {

        const response = await fetch("/api/internships");

        if (!response.ok) {
            throw new Error("Unable to load internships");
        }

        allInternships = await response.json();

        populateFilters();
        filterInternships();

    } catch (error) {

        console.error(error);

        statusText.textContent =
            "Could not load internships. Check that the Java server is running.";

        count.textContent = "";

    }
}


/* ================================
   POPULATE FILTER OPTIONS
================================ */

function populateFilters() {

    const locations = [
        ...new Set(
            allInternships
                .map(item => item.location)
                .filter(Boolean)
        )
    ].sort();

    const durations = [
        ...new Set(
            allInternships
                .map(item => item.duration)
                .filter(Boolean)
        )
    ].sort();

    locationFilter.replaceChildren(
        new Option("All Locations", "")
    );

    durationFilter.replaceChildren(
        new Option("All Durations", "")
    );

    locations.forEach(location => {
        locationFilter.add(new Option(location, location));
    });

    durations.forEach(duration => {
        durationFilter.add(new Option(duration, duration));
    });

}


/* ================================
   PAID / UNPAID DETECTION
================================ */

function isPaid(stipend) {

    if (!stipend) {
        return false;
    }

    const value = stipend.toLowerCase().trim();

    if (
        value.includes("unpaid") ||
        value.includes("no stipend")
    ) {
        return false;
    }

    const amounts = value.match(/\d[\d,]*/g);

    return amounts !== null &&
        amounts.some(amount =>
            Number(amount.replaceAll(",", "")) > 0
        );

}


/* ================================
   DISPLAY INTERNSHIP CARDS
================================ */

function displayInternships(internships) {

    internshipList.replaceChildren();

    count.textContent =
        `${internships.length} internship(s) found`;

    statusText.textContent = "";

    if (internships.length === 0) {

        statusText.textContent =
            "No internships found. Try changing your search or filters.";

        return;
    }

    internships.forEach(internship => {

        const card = document.createElement("article");
        card.className = "internship-card";

        const title = document.createElement("h3");
        title.textContent = internship.title;

        const company = document.createElement("p");
        company.className = "company";
        company.textContent = internship.company;

        const tags = document.createElement("div");
        tags.className = "tags";

        [
            internship.location,
            internship.workMode,
            internship.duration,
            internship.stipend
        ].filter(Boolean).forEach(value => {

            const tag = document.createElement("span");

            tag.className = "tag";
            tag.textContent = value;

            tags.appendChild(tag);

        });

        const skills = document.createElement("p");
        skills.className = "skills";

        skills.textContent =
            "Skills: " + (internship.skills || "Not specified");

        const description = document.createElement("p");
        description.className = "description";

        description.textContent =
            internship.description || "No description available.";

        const deadline = document.createElement("p");
        deadline.className = "deadline";

        deadline.textContent =
            "Application deadline: " +
            (internship.deadline || "Not specified");

        /* VIEW DETAILS BUTTON */

        const viewButton = document.createElement("button");

        viewButton.type = "button";
        viewButton.className = "details-button";
        viewButton.textContent = "View Details";

        viewButton.addEventListener("click", () => {
            showInternshipDetails(internship);
        });

        /* APPLY NOW BUTTON */

        const applyNowButton = createApplyButton(internship);

        /* ADD ELEMENTS TO CARD */

        card.append(
            title,
            company,
            tags,
            skills,
            description,
            deadline,
            viewButton,
            applyNowButton
        );

        internshipList.appendChild(card);

    });

}


/* ================================
   SEARCH AND FILTER LOGIC
================================ */

function filterInternships() {

    const keyword = keywordInput.value.trim().toLowerCase();

    const location = locationFilter.value;
    const mode = modeFilter.value;
    const duration = durationFilter.value;
    const stipend = stipendFilter.value;

    const filtered = allInternships.filter(internship => {

        const searchableText = [
            internship.title,
            internship.company,
            internship.location,
            internship.skills,
            internship.description
        ]
            .filter(Boolean)
            .join(" ")
            .toLowerCase();

        const matchesKeyword =
            !keyword || searchableText.includes(keyword);

        const matchesLocation =
            !location || internship.location === location;

        const matchesMode =
            !mode || internship.workMode === mode;

        const matchesDuration =
            !duration || internship.duration === duration;

        const matchesStipend =
            !stipend ||
            (stipend === "paid" && isPaid(internship.stipend)) ||
            (stipend === "unpaid" && !isPaid(internship.stipend));

        return (
            matchesKeyword &&
            matchesLocation &&
            matchesMode &&
            matchesDuration &&
            matchesStipend
        );

    });

    displayInternships(filtered);

}


/* ================================
   SHOW INTERNSHIP DETAILS
================================ */

function showInternshipDetails(internship) {

    detailsContent.replaceChildren();

    function addText(tagName, className, text) {

        const element = document.createElement(tagName);

        element.className = className;
        element.textContent = text;

        detailsContent.appendChild(element);

        return element;
    }

    const title = addText("h2", "", internship.title);

    title.id = "modalTitle";

    addText("p", "details-company", internship.company);

    addText(
        "p",
        "details-description",
        "Location: " + (internship.location || "Not specified")
    );

    addText(
        "p",
        "details-description",
        "Work mode: " + (internship.workMode || "Not specified")
    );

    addText(
        "p",
        "details-description",
        "Duration: " + (internship.duration || "Not specified")
    );

    addText(
        "p",
        "details-description",
        "Stipend: " + (internship.stipend || "Not specified")
    );

    addText(
        "p",
        "details-description",
        "Internship type: " +
        (internship.internshipType || "Not specified")
    );

    addText("h3", "details-label", "Required Skills");

    addText(
        "p",
        "details-description",
        internship.skills || "Skills not specified"
    );

    addText("h3", "details-label", "About This Internship");

    addText(
        "p",
        "details-description",
        internship.description || "No description available."
    );

    addText(
        "p",
        "details-description",
        "Application deadline: " +
        (internship.deadline || "Not specified")
    );

    /* APPLY BUTTON INSIDE DETAILS POPUP */

    const modalApplyButton = createApplyButton(internship);

    detailsContent.appendChild(modalApplyButton);

    detailsModal.classList.remove("hidden");

    closeModalButton.focus();

}


/* ================================
   CLOSE DETAILS POPUP
================================ */

function closeInternshipDetails() {

    detailsModal.classList.add("hidden");

}

closeModalButton.addEventListener(
    "click",
    closeInternshipDetails
);

detailsModal.addEventListener("click", event => {

    if (event.target === detailsModal) {
        closeInternshipDetails();
    }

});

document.addEventListener("keydown", event => {

    if (
        event.key === "Escape" &&
        !detailsModal.classList.contains("hidden")
    ) {
        closeInternshipDetails();
    }

});


/* ================================
   SEARCH EVENT
================================ */

searchForm.addEventListener("submit", event => {

    event.preventDefault();

    filterInternships();

});


/* ================================
   APPLY FILTERS
================================ */

applyButton.addEventListener("click", filterInternships);


/* ================================
   CLEAR FILTERS
================================ */

clearButton.addEventListener("click", () => {

    keywordInput.value = "";
    locationFilter.value = "";
    modeFilter.value = "";
    durationFilter.value = "";
    stipendFilter.value = "";

    displayInternships(allInternships);

});


/* ================================
   START APPLICATION
================================ */

loadInternships();
