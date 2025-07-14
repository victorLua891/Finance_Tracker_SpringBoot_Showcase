document.addEventListener('DOMContentLoaded', function() {
    const viewAccountSelect = document.getElementById('viewAccountSelect');
    const addExpenditureButton = document.getElementById('addExpenditureButton');
    const expenditureModal = document.getElementById('expenditureModal');
    const closeButton = expenditureModal.querySelector('.close-button');
    const expenditureForm = document.getElementById('expenditureForm');
    const selectAccountModalDropdown = document.getElementById('selectAccountModal');
    const expenditureMessage = document.getElementById('expenditureMessage');
    const expenditureTransactionsTableBody = document.querySelector('#expenditureTransactionsTable tbody');
    const noExpenditureMessage = document.getElementById('noExpenditureMessage');

    let userAccounts = []; // To store fetched accounts for dropdowns

    // --- Helper Functions ---
    function formatBalance(balance) {
        if (typeof balance === 'number') {
            return balance.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        }
        try {
            return parseFloat(balance).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
        } catch (e) {
            console.error("Could not parse balance:", balance, e);
            return 'N/A';
        }
    }

    function showMessage(element, message, isError = false) {
        element.textContent = message;
        element.style.color = isError ? 'red' : 'green';
        element.style.display = 'block';
        setTimeout(() => {
            element.style.display = 'none';
        }, 5000); // Hide after 5 seconds
    }

    function resetFormAndModal() {
        expenditureForm.reset();
        expenditureMessage.style.display = 'none'; // Hide message
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('expenditureDate').value = today; // Set default date to today
        expenditureModal.style.display = 'none'; // Close modal
    }

    // --- Fetch Accounts from API (for dropdowns) ---
    async function fetchAccountsForDropdowns() {
        viewAccountSelect.innerHTML = '<option value="">Loading accounts...</option>'; // Show loading message
        selectAccountModalDropdown.innerHTML = ''; // Clear modal options
        addExpenditureButton.disabled = true; // Disable button until accounts are loaded

        try {
            const response = await fetch('/api/accounts');
            if (!response.ok) {
                if (response.status === 401) {
                    viewAccountSelect.innerHTML = '<option value="">Login required</option>';
                    showMessage(document.querySelector('.content-wrapper'), 'You are not authorized to view accounts. Please log in.', true);
                } else {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return;
            }

            userAccounts = await response.json();
            console.log("Accounts received for dropdowns:", userAccounts);

            viewAccountSelect.innerHTML = '<option value="">Select an account</option>'; // Reset dropdowns
            selectAccountModalDropdown.innerHTML = ''; // Clear for modal

            if (userAccounts.length === 0) {
                addExpenditureButton.disabled = true; // Disable add expenditure if no accounts
                const messageDiv = document.getElementById('expenditure-records-container');
                if (messageDiv) {
                    messageDiv.innerHTML = '<p class="message-text" style="color: red;">No bank accounts found. Please add an account first to record expenditure.</p>';
                }
                noExpenditureMessage.style.display = 'none'; // Hide default "no expenditure message"

            } else {
                addExpenditureButton.disabled = false; // Enable add expenditure if accounts exist
                userAccounts.forEach(account => {
                    // Populate main view account dropdown
                    const optionView = document.createElement('option');
                    optionView.value = account.id;
                    optionView.textContent = `${account.name} (${account.accountNumberMasked})`;
                    viewAccountSelect.appendChild(optionView);

                    // Populate modal account dropdown
                    const optionModal = document.createElement('option');
                    optionModal.value = account.id;
                    optionModal.textContent = `${account.name} (${account.accountNumberMasked})`;
                    selectAccountModalDropdown.appendChild(optionModal);
                });

                // Automatically select the first account if available and fetch its transactions
                if (userAccounts.length > 0) {
                    viewAccountSelect.value = userAccounts[0].id;
                    fetchTransactions(userAccounts[0].id);
                }
            }
        } catch (error) {
            console.error('Error fetching accounts for expenditure page:', error);
            viewAccountSelect.innerHTML = '<option value="">Error loading accounts</option>';
            addExpenditureButton.disabled = true;
            showMessage(document.querySelector('.content-wrapper'), 'Error loading bank accounts. Please try again later.', true);
        }
    }

    // --- Fetch Transactions for a Selected Account ---
    async function fetchTransactions(accountId) {
        expenditureTransactionsTableBody.innerHTML = ''; // Clear previous transactions
        noExpenditureMessage.style.display = 'none'; // Hide no transactions message initially

        if (!accountId) {
            expenditureTransactionsTableBody.innerHTML = ''; // Clear if no account selected
            noExpenditureMessage.textContent = 'Please select an account to view its expenditure records.';
            noExpenditureMessage.style.display = 'block';
            return;
        }

        try {
            const response = await fetch(`/api/transactions/account/${accountId}`);
            if (!response.ok) {
                if (response.status === 401) {
                    throw new Error('Not authorized to view transactions for this account.');
                } else if (response.status === 404) {
                    noExpenditureMessage.textContent = 'Account not found or no transactions yet.';
                    noExpenditureMessage.style.display = 'block';
                    return;
                } else {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
            }

            const transactions = await response.json();
            console.log("Transactions received:", transactions);

            // Filter for only EXPENDITURE transactions for this page
            const expenditureTransactions = transactions.filter(t => t.type === 'EXPENDITURE');

            if (expenditureTransactions.length === 0) {
                noExpenditureMessage.textContent = 'No expenditure records found for this account.';
                noExpenditureMessage.style.display = 'block';
            } else {
                expenditureTransactions.forEach(transaction => {
                    const row = expenditureTransactionsTableBody.insertRow();
                    row.insertCell(0).textContent = new Date(transaction.transactionDate).toLocaleDateString(); // Format date
                    row.insertCell(1).textContent = transaction.description || 'N/A';
                    row.insertCell(2).textContent = `${transaction.account.currency} ${formatBalance(transaction.amount)}`;
                    row.insertCell(3).textContent = transaction.account.name; // Display account name for each transaction
                });
            }
        } catch (error) {
            console.error('Error fetching transactions:', error);
            noExpenditureMessage.textContent = `Error loading expenditure records: ${error.message}`;
            noExpenditureMessage.style.display = 'block';
        }
    }

    // --- Event Listeners ---

    // When the user selects a different account to view
    viewAccountSelect.addEventListener('change', function() {
        const selectedAccountId = viewAccountSelect.value;
        fetchTransactions(selectedAccountId);
    });

    addExpenditureButton.addEventListener('click', function() {
        if (userAccounts.length > 0) {
            expenditureModal.style.display = 'block';
            // Set default date to today
            const today = new Date().toISOString().split('T')[0];
            document.getElementById('expenditureDate').value = today;
            expenditureMessage.style.display = 'none'; // Clear any previous messages
        } else {
            showMessage(expenditureMessage, "Please add at least one account before recording expenditure.", true);
        }
    });

    closeButton.addEventListener('click', function() {
        resetFormAndModal();
    });

    window.addEventListener('click', function(event) {
        if (event.target == expenditureModal) {
            resetFormAndModal();
        }
    });

    // --- Form Submission Logic ---
    expenditureForm.addEventListener('submit', async function(event) {
        event.preventDefault(); // Prevent default form submission

        expenditureMessage.style.display = 'none'; // Hide previous messages

        const accountId = selectAccountModalDropdown.value;
        const amount = parseFloat(document.getElementById('expenditureAmount').value);
        const description = document.getElementById('expenditureDescription').value;
        const transactionDate = document.getElementById('expenditureDate').value; // YYYY-MM-DD

        // Basic validation
        if (!accountId || isNaN(amount) || amount <= 0 || !transactionDate) {
            showMessage(expenditureMessage, "Please fill in all required fields correctly (Amount must be positive).", true);
            return;
        }

        // Adjust date to ISO 8601 format (LocalDateTime expects this)
        const fullTransactionDate = `${transactionDate}T00:00:00`; // Assuming start of day for date picker

        const transactionData = {
            accountId: accountId,
            amount: amount,
            type: "EXPENDITURE", // <--- KEY DIFFERENCE: Set type to EXPENDITURE
            description: description,
            transactionDate: fullTransactionDate
        };

        try {
            const response = await fetch('/api/transactions', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(transactionData)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: 'Server error' }));
                throw new Error(errorData.message || `HTTP error! Status: ${response.status}`);
            }

            const newTransaction = await response.json();
            console.log("Expenditure recorded:", newTransaction);
            showMessage(expenditureMessage, "Expenditure recorded successfully!", false);

            // Re-fetch transactions for the currently selected view account
            const currentSelectedAccountId = viewAccountSelect.value;
            if (currentSelectedAccountId) { // Only refresh if an account is currently selected for viewing
                await fetchTransactions(currentSelectedAccountId);
            }
            fetchAccountsForDropdowns(); // Re-fetch accounts to update balances in dropdowns

            resetFormAndModal(); // Close modal and reset form

        } catch (error) {
            console.error('Error recording expenditure:', error);
            showMessage(expenditureMessage, `Failed to record expenditure: ${error.message}`, true);
        }
    });

    // Initial fetch of accounts when the page loads to populate dropdowns
    fetchAccountsForDropdowns();
});