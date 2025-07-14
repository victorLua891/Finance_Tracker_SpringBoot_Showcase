document.addEventListener('DOMContentLoaded', function() {
    const viewAccountSelect = document.getElementById('viewAccountSelect');
    const addIncomeButton = document.getElementById('addIncomeButton');
    const incomeModal = document.getElementById('incomeModal');
    const closeButton = incomeModal.querySelector('.close-button');
    const incomeForm = document.getElementById('incomeForm');
    const selectAccountModalDropdown = document.getElementById('selectAccountModal');
    const incomeMessage = document.getElementById('incomeMessage');
    const incomeTransactionsTableBody = document.querySelector('#incomeTransactionsTable tbody');
    const noIncomeMessage = document.getElementById('noIncomeMessage');

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
        incomeForm.reset();
        incomeMessage.style.display = 'none'; // Hide message
        const today = new Date().toISOString().split('T')[0];
        document.getElementById('incomeDate').value = today; // Set default date to today
        incomeModal.style.display = 'none'; // Close modal
    }

    // --- Fetch Accounts from API (for dropdowns) ---
    async function fetchAccountsForDropdowns() {
        viewAccountSelect.innerHTML = '<option value="">Loading accounts...</option>'; // Show loading message
        selectAccountModalDropdown.innerHTML = ''; // Clear modal options
        addIncomeButton.disabled = true; // Disable button until accounts are loaded

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
                addIncomeButton.disabled = true; // Disable add income if no accounts
                const messageDiv = document.getElementById('income-records-container');
                if (messageDiv) {
                    messageDiv.innerHTML = '<p class="message-text" style="color: red;">No bank accounts found. Please add an account first to record income.</p>';
                }
                noIncomeMessage.style.display = 'none'; // Hide default "no income message"

            } else {
                addIncomeButton.disabled = false; // Enable add income if accounts exist
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
            console.error('Error fetching accounts for income page:', error);
            viewAccountSelect.innerHTML = '<option value="">Error loading accounts</option>';
            addIncomeButton.disabled = true;
            showMessage(document.querySelector('.content-wrapper'), 'Error loading bank accounts. Please try again later.', true);
        }
    }

    // --- Fetch Transactions for a Selected Account ---
    async function fetchTransactions(accountId) {
        incomeTransactionsTableBody.innerHTML = ''; // Clear previous transactions
        noIncomeMessage.style.display = 'none'; // Hide no transactions message initially

        if (!accountId) {
            incomeTransactionsTableBody.innerHTML = ''; // Clear if no account selected
            noIncomeMessage.textContent = 'Please select an account to view its income records.';
            noIncomeMessage.style.display = 'block';
            return;
        }

        try {
            const response = await fetch(`/api/transactions/account/${accountId}`);
            if (!response.ok) {
                if (response.status === 401) {
                    throw new Error('Not authorized to view transactions for this account.');
                } else if (response.status === 404) {
                    noIncomeMessage.textContent = 'Account not found or no transactions yet.';
                    noIncomeMessage.style.display = 'block';
                    return;
                } else {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
            }

            const transactions = await response.json();
            console.log("Transactions received:", transactions);

            // Filter for only INCOME transactions for this page
            const incomeTransactions = transactions.filter(t => t.type === 'INCOME');

            if (incomeTransactions.length === 0) {
                noIncomeMessage.textContent = 'No income records found for this account.';
                noIncomeMessage.style.display = 'block';
            } else {
                incomeTransactions.forEach(transaction => {
                    const row = incomeTransactionsTableBody.insertRow();
                    row.insertCell(0).textContent = new Date(transaction.transactionDate).toLocaleDateString(); // Format date
                    row.insertCell(1).textContent = transaction.description || 'N/A';
                    row.insertCell(2).textContent = `${transaction.account.currency} ${formatBalance(transaction.amount)}`;
                    row.insertCell(3).textContent = transaction.account.name; // Display account name for each transaction
                });
            }
        } catch (error) {
            console.error('Error fetching transactions:', error);
            noIncomeMessage.textContent = `Error loading income records: ${error.message}`;
            noIncomeMessage.style.display = 'block';
        }
    }

    // --- Event Listeners ---

    // When the user selects a different account to view
    viewAccountSelect.addEventListener('change', function() {
        const selectedAccountId = viewAccountSelect.value;
        fetchTransactions(selectedAccountId);
    });

    addIncomeButton.addEventListener('click', function() {
        if (userAccounts.length > 0) {
            incomeModal.style.display = 'block';
            // Set default date to today
            const today = new Date().toISOString().split('T')[0];
            document.getElementById('incomeDate').value = today;
            incomeMessage.style.display = 'none'; // Clear any previous messages
        } else {
            showMessage(incomeMessage, "Please add at least one account before recording income.", true);
        }
    });

    closeButton.addEventListener('click', function() {
        resetFormAndModal();
    });

    window.addEventListener('click', function(event) {
        if (event.target == incomeModal) {
            resetFormAndModal();
        }
    });

    // --- Form Submission Logic ---
    incomeForm.addEventListener('submit', async function(event) {
        event.preventDefault(); // Prevent default form submission

        incomeMessage.style.display = 'none'; // Hide previous messages

        const accountId = selectAccountModalDropdown.value;
        const amount = parseFloat(document.getElementById('incomeAmount').value);
        const description = document.getElementById('incomeDescription').value;
        const transactionDate = document.getElementById('incomeDate').value; // YYYY-MM-DD

        // Basic validation
        if (!accountId || isNaN(amount) || amount <= 0 || !transactionDate) {
            showMessage(incomeMessage, "Please fill in all required fields correctly (Amount must be positive).", true);
            return;
        }

        // Adjust date to ISO 8601 format (LocalDateTime expects this)
        const fullTransactionDate = `${transactionDate}T00:00:00`; // Assuming start of day for date picker

        const transactionData = {
            accountId: accountId,
            amount: amount,
            type: "INCOME", // Hardcoded for income
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
            console.log("Income recorded:", newTransaction);
            showMessage(incomeMessage, "Income recorded successfully!", false);

            // Re-fetch transactions for the currently selected view account
            const currentSelectedAccountId = viewAccountSelect.value;
            if (currentSelectedAccountId) { // Only refresh if an account is currently selected for viewing
                await fetchTransactions(currentSelectedAccountId);
            }
            fetchAccountsForDropdowns(); // Re-fetch accounts to update balances in dropdowns (if they showed balance)
                                        // or just to ensure dropdowns are up-to-date.

            resetFormAndModal(); // Close modal and reset form

        } catch (error) {
            console.error('Error recording income:', error);
            showMessage(incomeMessage, `Failed to record income: ${error.message}`, true);
        }
    });

    // Initial fetch of accounts when the page loads to populate dropdowns
    fetchAccountsForDropdowns();
});