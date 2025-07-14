document.addEventListener('DOMContentLoaded', () => {
    const userSelect = document.getElementById('userSelect');
    const viewUserAccountsBtn = document.getElementById('viewUserAccountsBtn');
    const selectedUserNameSpan = document.getElementById('selectedUserName');
    const userAccountsDisplaySection = document.querySelector('.user-accounts-display-section');
    const userAccountsTableBody = document.querySelector('#userAccountsTable tbody');
    const noAccountsMessage = document.getElementById('noAccountsMessage');

    const addAccountForm = document.getElementById('addAccountForm');
    const addAccountNameInput = document.getElementById('addAccountName');
    const addAccountBalanceInput = document.getElementById('addAccountBalance');
    const addAccountCurrencyInput = document.getElementById('addAccountCurrency');
    const addAccountMessage = document.getElementById('addAccountMessage');

    let selectedUserId = null; // To store the ID of the currently selected user

    // --- Function to fetch all users (for the dropdown) ---
    async function fetchUsers() {
        try {
            const token = localStorage.getItem('jwtToken'); // Get JWT from localStorage
            if (!token) {
                alert('Authentication token not found. Please log in.');
                window.location.href = '/admin/login'; // Redirect to admin login if no token
                return;
            }

            const response = await fetch('/api/admin/users', { // Backend endpoint to get all users
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const users = await response.json();
                userSelect.innerHTML = '<option value="">-- Select User --</option>'; // Clear existing options
                users.forEach(user => {
                    const option = document.createElement('option');
                    option.value = user.id;
                    option.textContent = `${user.username} (ID: ${user.id})`;
                    userSelect.appendChild(option);
                });
            } else if (response.status === 403) {
                alert('Access denied. You do not have permission to view users.');
            } else {
                const errorData = await response.json();
                alert(`Failed to fetch users: ${errorData.message || response.statusText}`);
            }
        } catch (error) {
            console.error('Error fetching users:', error);
            alert('An error occurred while fetching users.');
        }
    }

    // --- Function to fetch accounts for a selected user ---
    async function fetchUserAccounts(userId) {
        if (!userId) {
            userAccountsDisplaySection.style.display = 'none';
            return;
        }

        try {
            const token = localStorage.getItem('jwtToken'); // Get JWT from localStorage
            const response = await fetch(`/api/admin/accounts/user/${userId}`, { // Backend endpoint to get accounts by user ID
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            if (response.ok) {
                const accounts = await response.json();
                userAccountsDisplaySection.style.display = 'block';
                userAccountsTableBody.innerHTML = ''; // Clear previous accounts

                if (accounts.length === 0) {
                    noAccountsMessage.style.display = 'block';
                    userAccountsTable.style.display = 'none';
                } else {
                    noAccountsMessage.style.display = 'none';
                    userAccountsTable.style.display = 'table';
                    accounts.forEach(account => {
                        const row = userAccountsTableBody.insertRow();
                        row.insertCell(0).textContent = account.id;
                        row.insertCell(1).textContent = account.name;
                        row.insertCell(2).textContent = account.balance ? parseFloat(account.balance).toFixed(2) : '0.00';
                        row.insertCell(3).textContent = account.currency;
                        // Add more cells for other account properties if needed
                    });
                }
            } else if (response.status === 403) {
                alert('Access denied. You do not have permission to view these accounts.');
            } else {
                const errorData = await response.json();
                alert(`Failed to fetch user accounts: ${errorData.message || response.statusText}`);
            }
        } catch (error) {
            console.error('Error fetching user accounts:', error);
            alert('An error occurred while fetching user accounts.');
        }
    }

    // --- Event Listener for View User Accounts Button ---
    viewUserAccountsBtn.addEventListener('click', () => {
        selectedUserId = userSelect.value;
        const selectedOption = userSelect.options[userSelect.selectedIndex];
        if (selectedUserId) {
            selectedUserNameSpan.textContent = selectedOption.textContent.split('(')[0].trim(); // Extract username
            fetchUserAccounts(selectedUserId);
        } else {
            alert('Please select a user first.');
            userAccountsDisplaySection.style.display = 'none';
        }
    });

    // --- Event Listener for Add Account Form Submission ---
    addAccountForm.addEventListener('submit', async (event) => {
        event.preventDefault(); // Prevent default form submission

        if (!selectedUserId) {
            alert('Please select a user before adding an account.');
            return;
        }

        const name = addAccountNameInput.value;
        const balance = parseFloat(addAccountBalanceInput.value);
        const currency = addAccountCurrencyInput.value;

        if (isNaN(balance) || balance < 0) {
            alert('Please enter a valid positive initial balance.');
            return;
        }
        if (!name || !currency) {
            alert('Account name and currency are required.');
            return;
        }

        try {
            const token = localStorage.getItem('jwtToken');
            const response = await fetch(`/api/admin/accounts/user/${selectedUserId}`, { // Backend endpoint to add account for user
                method: 'POST',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ name, balance, currency })
            });

            if (response.ok) {
                const newAccount = await response.json();
                addAccountMessage.textContent = `Account "${newAccount.name}" created successfully!`;
                addAccountMessage.style.color = 'green';
                addAccountMessage.style.display = 'block';
                addAccountForm.reset(); // Clear form fields
                fetchUserAccounts(selectedUserId); // Refresh the list of accounts for the user
            } else if (response.status === 403) {
                addAccountMessage.textContent = 'Access denied. You do not have permission to add accounts.';
                addAccountMessage.style.color = 'red';
                addAccountMessage.style.display = 'block';
            }
            else {
                const errorData = await response.json();
                addAccountMessage.textContent = `Failed to add account: ${errorData.message || response.statusText}`;
                addAccountMessage.style.color = 'red';
                addAccountMessage.style.display = 'block';
            }
        } catch (error) {
            console.error('Error adding account:', error);
            addAccountMessage.textContent = 'An error occurred while adding the account.';
            addAccountMessage.style.color = 'red';
            addAccountMessage.style.display = 'block';
        }
    });

    // Initial fetch of users when the page loads
    fetchUsers();
});