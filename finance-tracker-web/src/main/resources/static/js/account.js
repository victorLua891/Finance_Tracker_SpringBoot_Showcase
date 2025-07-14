document.addEventListener('DOMContentLoaded', function() {
    fetchAccounts();

    async function fetchAccounts() {
        const accountListContainer = document.getElementById('account-list-container');
        accountListContainer.innerHTML = '<p>Loading your bank accounts...</p>'; // Show loading message

        try {
            const response = await fetch('/api/accounts');

            if (!response.ok) {
                if (response.status === 401) {
                    accountListContainer.innerHTML = '<p>You are not authorized to view accounts. Please log in.</p>';
                } else {
                    throw new Error(`HTTP error! Status: ${response.status}`);
                }
                return;
            }

            const accounts = await response.json();

            // --- ADD THESE CONSOLE.LOG STATEMENTS ---
            console.log("Accounts received from API:", accounts);
            console.log("Number of accounts received:", accounts.length);
            // --- END ADDITIONS ---

            if (accounts.length === 0) {
                accountListContainer.innerHTML = '<p>No bank accounts found.</p>';
            } else {
                accountListContainer.innerHTML = ''; // Clear loading message ONCE
                accounts.forEach(account => {
                    // --- ADD A CONSOLE.LOG FOR EACH ACCOUNT BEING PROCESSED ---
                    console.log("Processing account:", account.name, account.accountNumberMasked);
                    // --- END ADDITION ---

                    const accountCardHtml = `
                        <button type="button" class="account-card-button">
                            <div class="account-card-orange">
                                <h3 class="account-name">${account.name}</h3>
                                <span class="account-number">${account.accountNumberMasked}</span>
                                <div class="account-balance">
                                    <span class="currency">${account.currency}</span>
                                    <span class="amount">${formatBalance(account.balance)}</span>
                                </div>
                            </div>
                        </button>
                    `;
                    // Use insertAdjacentHTML('beforeend', ...) to ADD content
                    accountListContainer.insertAdjacentHTML('beforeend', accountCardHtml);
                });
            }
        } catch (error) {
            console.error('Error fetching accounts:', error);
            accountListContainer.innerHTML = '<p>Error loading bank accounts. Please try again later.</p>';
        }
    }

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
});
