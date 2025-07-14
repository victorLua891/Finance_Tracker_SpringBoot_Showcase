document.addEventListener('DOMContentLoaded', function() {
    const addBudgetBtn = document.getElementById('addBudgetBtn');
    const addBudgetModal = document.getElementById('addBudgetModal');
    const closeButton = document.querySelector('.close-button');
    const overlay = document.getElementById('overlay');

    // Function to open the modal
    function openModal() {
        addBudgetModal.style.display = 'block';
        overlay.style.display = 'block';
    }

    // Function to close the modal
    function closeModal() {
        addBudgetModal.style.display = 'none';
        overlay.style.display = 'none';
    }

    // Event listeners
    addBudgetBtn.addEventListener('click', openModal);
    closeButton.addEventListener('click', closeModal);
    overlay.addEventListener('click', closeModal); // Close modal when clicking outside

    // Close the modal if the user presses the Escape key
    window.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            closeModal();
        }
    });
});