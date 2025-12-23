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
    const firstName = document.getElementById('firstName').value;
    const lastName  = document.getElementById('lastName').value;
    const email     = document.getElementById('register-email').value;
    const password  = document.getElementById('register-password').value;

    const result = window.javaAuth.register(email, firstName, lastName, password);
    if (result === 'SUCCESS') {
        alert('Registration successful!');
        // redirect to login page
    document.getElementById('register-form').style.display = 'none';
    document.getElementById('login-form').style.display = 'flex';
    } else {
        alert('Registration failed: ' + result);
    }
}
function login() {
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;

    // call Java method via WebView bridge
    const result = window.javaAuth.login(email, password);
    if (result === 'SUCCESS') {
        alert('Login successful!');
        window.javaAuth.loadPage("workspace.html");
    } else {
        alert('Login failed: ' + result);
    }
}
// Form submissions
document.getElementById('registerForm').addEventListener('submit', function(e) {
    e.preventDefault();
    register()
});

document.getElementById('loginForm').addEventListener('submit', function(e) {
    e.preventDefault();
    login()

});