document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const messageDisplay = document.getElementById('messageDisplay');

    // Function to display messages
    function showMessage(message, type) {
        messageDisplay.textContent = message;
        messageDisplay.className = `message ${type}`; // Add type class (error/success)
        messageDisplay.style.display = 'block';
    }

    // Event listener for form submission
    loginForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // Prevent default form submission (which would go to /authenticate)

        const username = usernameInput.value;
        const password = passwordInput.value;

        try {
            const response = await fetch('/api/auth/login', { // Target your backend API endpoint
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (response.ok) {
                const data = await response.json();
                const jwtToken = data.token;
                const userRoles = data.roles; // Assuming your JwtResponse includes roles

//                localStorage.setItem('jwtToken', jwtToken); // Store the JWT token

                showMessage('Login successful!', 'success');

                // Redirect based on role
                if (userRoles.includes('ROLE_MASTER_ADMIN')) {
                    window.location.href = '/admin/dashboard'; // Or a specific master admin dashboard
                } else if (userRoles.includes('ROLE_ADMIN')) {
                    window.location.href = '/admin/dashboard'; // Admin dashboard
                } else if (userRoles.includes('ROLE_USER')) {
                    window.location.href = '/dashboard'; // Regular user dashboard
                } else {
                    // Fallback if no recognized role
                    window.location.href = '/'; // Or a default page
                }

            } else {
                const errorData = await response.json();
                showMessage(errorData.message || 'Invalid username or password.', 'error');
            }
        } catch (error) {
            console.error('Login error:', error);
            showMessage('An unexpected error occurred. Please try again.', 'error');
        }
    });

    // Check for any success messages from registration redirect (if applicable)
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.has('registered')) {
        showMessage('Registration successful! Please log in.', 'success');
    }
});