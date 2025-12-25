<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>ECLIPSEWORK – Workspace</title>
    <link rel="stylesheet" href="workspace.css" />
</head>

<body>

<!-- =====================================================
     REQUIRED ENTRY POINT FOR JAVA
     DO NOT RENAME
===================================================== -->
<script>
window.loadDashboard = function (data) {
    console.log("✅ loadDashboard CALLED");
    console.log(data);

    if (!data || !data.workspace) {
        console.error("❌ Invalid dashboard payload", data);
        return;
    }

    /* ===============================
       NAVBAR USER (CURRENT USER)
    =============================== */
    const userNameEl = document.querySelector(".user-name");

    if (data.currentUser?.firstName && data.currentUser?.lastName) {
        userNameEl.textContent =
            data.currentUser.firstName + " " + data.currentUser.lastName;
    } else {
        userNameEl.textContent = "User";
    }

    /* ===============================
       EDIT BUTTON (LEAD ONLY)
    =============================== */
    const editBtn = document.querySelector(".edit-btn");

    if (editBtn && data.currentUserRole === "LEAD") {
        editBtn.style.display = "inline-flex";
        console.log("✏️ Edit Project enabled");
    }

    /* ===============================
       TITLE
    =============================== */
    document.querySelector(".workspace-title").textContent =
        "Progression — " + data.workspace.name;

    /* ===============================
       TASKS
    =============================== */
    const tasksList = document.querySelector(".tasks-list");
    tasksList.innerHTML = "";

    if (!data.tasks || data.tasks.length === 0) {
        tasksList.innerHTML = "<p>No tasks available</p>";
        return;
    }

    data.tasks.forEach((task, i) => {
        const row = document.createElement("div");
        row.className = "task-row";

        const bar = document.createElement("div");
        bar.className = "task-bar";
        bar.textContent = task.title || "Untitled task";

        if (data.startPcts && data.endPcts) {
            bar.style.setProperty("--start", (data.startPcts[i] || 0) + "%");
            bar.style.setProperty("--end", (data.endPcts[i] || 0) + "%");
        }

        row.appendChild(bar);
        tasksList.appendChild(row);
    });

    /* ===============================
       TIMELINE
    =============================== */
    const header = document.querySelector(".chart-header");
    const keepStart = header.children[0];
    const keepEnd = header.children[header.children.length - 1];

    while (header.children.length > 2) {
        header.removeChild(header.children[1]);
    }

    if (data.dateLabels?.length) {
        data.dateLabels.forEach(date => {
            const span = document.createElement("span");
            span.textContent = date;
            header.insertBefore(span, keepEnd);
        });
    }
};
</script>

<!-- =========================
     HEADER / NAVBAR
========================= -->
<header>
    <div class="left-zone">
        <h1 class="logo-title">ECLIPSEWORK</h1>

        <nav>
            <a href="#" onclick="goDashboard()">Dashboard</a>
            <a href="#" onclick="goProjects()">Projects</a>
            <a href="#" onclick="goTasks()">Tasks</a>
        </nav>
    </div>

    <div class="user-zone">
        <img src="user.png" class="user-icon" />
        <span class="user-name">—</span>
    </div>
</header>

<!-- =========================
     MAIN CONTENT
========================= -->
<section class="workspace-container">

    <button class="back-btn" onclick="goProjects()">Back to Projects</button>

    <div class="workspace-header">
        <h2 class="workspace-title">Progression</h2>

        <div class="workspace-actions">
            <!-- 🔒 hidden by default -->
            <button
                class="edit-btn"
                style="display:none;"
                onclick="goEditProject()"
            >
                ✎ Edit Project
            </button>
        </div>
    </div>

    <div class="chart-container">
        <div class="chart-header">
            <span>Start Date</span>
            <span>End Date</span>
        </div>
        <div class="tasks-list"></div>
    </div>
</section>

<!-- =========================
     NAVIGATION
========================= -->
<script>
function goProjects() {
    window.java?.goProjects?.();
}

function goDashboard() {
    window.java?.goDashboard?.();
}

function goTasks() {
    window.java?.goTasks?.();
}

/* ✅ THIS IS THE FIX */
function goEditProject() {
    console.log("✏ Edit Project clicked");
    window.java?.editProject?.();
}
</script>

</body>
</html>
