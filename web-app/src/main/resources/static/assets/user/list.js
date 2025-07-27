document.body.addEventListener('htmx:afterSwap', function(event) {
    // Close any open dialogs when the user table is updated (after successful form submission)
    if (event.detail.target.id === 'user-table-container') {
        console.log('User table updated, closing dialogs');
        // Find all open dialogs and close them
        document.querySelectorAll('wa-dialog[open]').forEach(dialog => {
            console.log('Closing dialog:', dialog);
            dialog.open = false;
        });
        document.getElementById("modal-container").innerHTML = "";
    }

    // Open the dialog when it's loaded into the modal container
    if (event.detail.target.id === 'modal-container') {
        console.log('Modal container updated, opening dialog');
        // Find the dialog in the modal container and open it
        const dialog = event.detail.target.querySelector('wa-dialog');
        if (dialog) {
            console.log('Opening dialog:', dialog);
            dialog.open = true;
        }
    }
});
