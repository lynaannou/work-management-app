

   /* -------------------------------
      LOGOUT
   -------------------------------- */
console.log("profile.js loaded");

window.addEventListener("load", () => {
    console.log("profile page fully loaded");

    const logoutBtn = document.getElementById("logoutBtn");
    if (!logoutBtn) {
        console.error("Logout button not found");
        return;
    }

    logoutBtn.onclick = () => {
        console.log("Logout clicked");

        if (!window.javaBridge) {
            console.error("Java bridge missing");
            return;
        }

        const result = window.javaBridge.logout();
        console.log("Logout result:", result);
    };
});

window.addEventListener("load", () => {
    console.log("profile page loaded");

    // 🔙 Return button
    const returnBtn = document.getElementById("returnBtn");
    if (!returnBtn) {
        console.error("Return button not found");
        return;
    }

    returnBtn.onclick = () => {
        console.log("Return to workspace clicked");

        if (!window.javaBridge) {
            console.error("Java bridge missing");
            return;
        }

        // Java handles page loading
        window.javaBridge.navigateTo("workspace.html");
    };
});


/* -------------------------------
   UI STATE
-------------------------------- */
let editMode = {
    profile: false,
    password: false
};

let originalProfileData = {
    firstName: '',
    lastName: '',
    email: ''
};

/* -------------------------------
   PAGE INIT
-------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    waitForJavaBridge();
});

function waitForJavaBridge(retries = 20) {
    if (window.javaBridge) {
        console.log("✅ Java bridge detected");
        loadProfileFromJava();
        return;
    }

    if (retries === 0) {
        console.error("❌ Java bridge not available after waiting");
        showToast("Failed to connect to application", "error");
        return;
    }

    console.log("⏳ Waiting for Java bridge...");
    setTimeout(() => waitForJavaBridge(retries - 1), 100);
}


function loadProfileFromJava() {
    console.log("=== LOADING PROFILE FROM JAVA ===");

    if (!window.javaBridge) {
        console.error("❌ Java bridge not available");
        showToast("Failed to connect to application", "error");
        return;
    }

    console.log("✓ Java bridge is available");

    try {
        // ✅ FIX: Call getProfile() - this is the Java method name
        console.log("Calling window.javaBridge.getProfile()...");
        const responseStr = window.javaBridge.getProfile();
        console.log("✓ Response received:", responseStr);

        const response = JSON.parse(responseStr);

        if (!response.success) {
            console.error("❌ Failed to load profile:", response.error);
            showToast("Failed to load profile: " + response.error, "error");
            return;
        }

        const user = response.user;
        console.log("✓ User data loaded:", user);

        // Update form fields
        document.getElementById("firstName").value = user.firstName;
        document.getElementById("lastName").value = user.lastName;
        document.getElementById("emailAddress").value = user.email;

        // Save original values (used for cancel)
        originalProfileData = {
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email
        };

        console.log("✅ Profile loaded successfully");
    } catch (error) {
        console.error("❌ Error loading profile:", error);
        showToast("Failed to load profile", "error");
    }
}

/* -------------------------------
   TOGGLE EDIT MODE
-------------------------------- */
function toggleEdit(section) {
    if (section === "profile") {
        editMode.profile = !editMode.profile;

        document.getElementById("firstName").disabled = !editMode.profile;
        document.getElementById("lastName").disabled = !editMode.profile;
        document.getElementById("profileActions").style.display =
            editMode.profile ? "flex" : "none";

        if (editMode.profile) {
            document.getElementById("firstName").focus();
        }
    }

    if (section === "password") {
        editMode.password = !editMode.password;
        document.getElementById("passwordFields").style.display =
            editMode.password ? "block" : "none";
    }
}

/* -------------------------------
   SAVE PROFILE (CALL JAVA)
-------------------------------- */
function saveProfile(event) {
    event.preventDefault();
    console.log("=== SAVE PROFILE CALLED ===");

    const firstName = document.getElementById("firstName").value.trim();
    const lastName = document.getElementById("lastName").value.trim();

    console.log("First Name:", firstName);
    console.log("Last Name:", lastName);

    if (!firstName || !lastName) {
        showToast("First and last name are required", "error");
        return;
    }

    const btn = event.currentTarget;
    btn.classList.add("loading");
    btn.disabled = true;

    // Call Java
    console.log("Calling window.javaBridge.updateProfile()...");
    const result = window.javaBridge.updateProfile(firstName, lastName);
    console.log("Update result:", result);

    btn.classList.remove("loading");
    btn.disabled = false;

    if (result.startsWith("ERROR")) {
        showToast(result.replace("ERROR:", "").trim(), "error");
        return;
    }

    // Success → update local copy and reload
    originalProfileData.firstName = firstName;
    originalProfileData.lastName = lastName;

    toggleEdit("profile");
    showToast("Profile updated successfully", "success");

    // Reload profile data after a short delay
    setTimeout(() => {
        console.log("Reloading profile data...");
        loadProfileFromJava();
    }, 500);
}

/* -------------------------------
   CANCEL EDIT
-------------------------------- */
function cancelEdit(section) {
    if (section === "profile") {
        document.getElementById("firstName").value = originalProfileData.firstName;
        document.getElementById("lastName").value = originalProfileData.lastName;
        toggleEdit("profile");
    }

    if (section === "password") {
        clearPasswordFields();
        toggleEdit("password");
    }
}

/* -------------------------------
   CHANGE PASSWORD
-------------------------------- */
function savePassword(event) {
    event.preventDefault();
    console.log("=== SAVE PASSWORD CALLED ===");

    const current = document.getElementById("currentPassword").value;
    const next = document.getElementById("newPassword").value;
    const confirm = document.getElementById("confirmPassword").value;

    if (!current || !next || !confirm) {
        showToast("All password fields are required", "error");
        return;
    }

    if (next.length < 8) {
        showToast("Password must be at least 8 characters", "error");
        return;
    }

    if (next !== confirm) {
        showToast("New passwords do not match", "error");
        return;
    }

    console.log("Calling window.javaBridge.changePassword()...");
    const result = window.javaBridge.changePassword(current, next, confirm);
    console.log("Password change result:", result);

    if (result.startsWith("ERROR")) {
        showToast(result.replace("ERROR:", "").trim(), "error");
        return;
    }

    clearPasswordFields();
    toggleEdit("password");
    showToast("Password updated successfully", "success");
}

/* -------------------------------
   PASSWORD VISIBILITY
-------------------------------- */
function togglePasswordVisibility(inputId, event) {
    event.preventDefault();

    const input = document.getElementById(inputId);
    input.type = input.type === "password" ? "text" : "password";
}



/* -------------------------------
   HELPERS
-------------------------------- */
function clearPasswordFields() {
    ["currentPassword", "newPassword", "confirmPassword"].forEach(id => {
        document.getElementById(id).value = "";
    });
}

function showToast(message, type = "info") {
    const toast = document.getElementById("toast");
    const toastMessage = document.getElementById("toastMessage");

    if (toast && toastMessage) {
        toastMessage.textContent = message;
        toast.className = `toast show ${type}`;
        setTimeout(() => toast.classList.remove("show"), 3000);
    } else {
        console.error("Toast elements not found");
        alert(message); // Fallback to alert
    }
}

// Debug helper - call this from browser console if needed
window.debugProfile = function() {
    console.log("=== PROFILE DEBUG ===");
    console.log("Java bridge available:", !!window.javaBridge);
    console.log("Original data:", originalProfileData);
    console.log("Form values:", {
        firstName: document.getElementById("firstName")?.value,
        lastName: document.getElementById("lastName")?.value,
        email: document.getElementById("emailAddress")?.value
    });

    if (window.javaBridge) {
        console.log("Testing getProfile()...");
        try {
            const result = window.javaBridge.getProfile();
            console.log("getProfile() result:", result);
        } catch (e) {
            console.error("getProfile() error:", e);
        }
    }
};