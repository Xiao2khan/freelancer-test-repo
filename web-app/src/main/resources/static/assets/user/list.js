document.body.addEventListener('htmx:afterSwap', function(event) {
    // Clear UI when the user table is updated (after successful form submission)
    if (event.detail.target.id === 'user-table-container') {
        document.querySelectorAll('wa-dialog[open]').forEach(dialog => {
            dialog.open = false;
        });
        document.getElementById("modal-container").innerHTML = "";
    }

    // Open the dialog when it's loaded into the modal container
    if (event.detail.target.id === 'modal-container') {
        const dialog = event.detail.target.querySelector('wa-dialog');
        if (dialog) {
            dialog.open = true;
        }
    }
});
