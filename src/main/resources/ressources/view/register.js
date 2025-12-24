// Toggle between login and register forms
document.getElementById('show-login').addEventListener('click', function(e) {
    e.preventDefault();
    document.getElementById('register-form').style.display = 'none';
    document.getElementById('login-form').style.display = 'flex';
});

document.getElementById('show-register').addEventListener('click', function(e) {
    e.preventDefault();
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'flex';
});

// Toggle password visibility
document.querySelectorAll('.toggle-password').forEach(button => {
    button.addEventListener('click', function() {
        const targetId = this.getAttribute('data-target');
        const passwordInput = document.getElementById(targetId);

        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            this.innerHTML = `
                <svg class="eye-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path>
                    <line x1="1" y1="1" x2="23" y2="23"></line>
                </svg>
            `;
        } else {
            passwordInput.type = 'password';
            this.innerHTML = `
                <svg class="eye-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                    <circle cx="12" cy="12" r="3"></circle>
                </svg>
            `;
        }
    });
});

function register() {
    console.log('=================================');
    console.log('Register function called from JavaScript');
    console.log('=================================');

    // ✅ FIXED: Check for javaBridge instead of javaAuth
    if (typeof window.javaBridge === 'undefined') {
        console.error('Java bridge not found!');
        alert('Error: Application bridge not initialized. Please restart the application.');
        return;
    }

    const firstName = document.getElementById('firstName').value;
    const lastName  = document.getElementById('lastName').value;
    const email     = document.getElementById('register-email').value;
    const password  = document.getElementById('register-password').value;
    const confirmPassword = document.getElementById('register-confirm-password').value;

    console.log('Form values:');
    console.log('- First Name:', firstName);
    console.log('- Last Name:', lastName);
    console.log('- Email:', email);
    console.log('- Password:', password ? '[PROVIDED]' : '[EMPTY]');
    console.log('- Confirm Password:', confirmPassword ? '[PROVIDED]' : '[EMPTY]');

    // Validate fields
    if (!firstName || !lastName || !email || !password) {
        alert('Please fill in all fields');
        return;
    }

    // Check password match
    if (password !== confirmPassword) {
        alert('Passwords do not match!');
        return;
    }

    try {
        console.log('Calling Java register method...');
        console.log('Parameters:', email, firstName, lastName, '[PASSWORD]');

        // ✅ FIXED: Use javaBridge instead of javaAuth
        const result = window.javaBridge.register(email, firstName, lastName, password);

        console.log('Register result type:', typeof result);
        console.log('Register result value:', result);
        console.log('=================================');

        if (result === undefined || result === null) {
            console.error('Result is undefined or null!');
            alert('Error: Registration returned no response. Check console for details.');
            return;
        }

        if (result === 'SUCCESS') {
            alert('Registration successful! Please login.');
            // Switch to login form
            document.getElementById('register-form').style.display = 'none';
            document.getElementById('login-form').style.display = 'flex';
        } else {
            // Result contains error message
            alert('Registration failed: ' + result.replace('ERROR: ', ''));
        }
    } catch (error) {
        console.error('JavaScript error during registration:', error);
        alert('Error: ' + error.message);
    }
}

function login() {
    console.log('=== LOGIN CALLED ===');

    clearLoginError(); // ✅ clear old message

    if (typeof window.javaBridge === 'undefined') {
        showLoginError("Application not ready. Please restart.");
        return;
    }

    const email = document.getElementById('login-email').value.trim();
    const password = document.getElementById('login-password').value;

    if (!email || !password) {
        showLoginError("Email and password are required");
        return;
    }

    try {
        const result = window.javaBridge.login(email, password);
        console.log("Login result:", result);

        if (result === "SUCCESS") {
            window.javaBridge.navigateTo("workspace.html");
            return;
        }

        // 🔴 ERROR returned from Java
        if (result.startsWith("ERROR")) {
            showLoginError(result.replace("ERROR:", "").trim());
            return;
        }

        showLoginError("Unknown login error");

    } catch (e) {
        console.error(e);
        showLoginError("Unexpected error occurred");
    }
}
function showLoginError(message) {
    const box = document.getElementById("login-error");
    box.textContent = message;
    box.style.display = "block";
}

function clearLoginError() {
    const box = document.getElementById("login-error");
    box.textContent = "";
    box.style.display = "none";
}

// Form submissions
document.getElementById('registerForm').addEventListener('submit', function(e) {
    e.preventDefault();
    console.log('Register form submitted');
    register();
});

document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    console.log('Login form submitted');
    login();
});

// Check if bridge is loaded on page load
window.addEventListener('load', function() {
    console.log('=================================');
    console.log('Page loaded');
    // ✅ FIXED: Check for javaBridge instead of javaAuth
    console.log('Java bridge available:', typeof window.javaBridge !== 'undefined');
    if (typeof window.javaBridge !== 'undefined') {
        console.log('✅ Bridge is ready!');
        console.log('Available methods:', Object.getOwnPropertyNames(Object.getPrototypeOf(window.javaBridge)));
    }
    console.log('=================================');
});