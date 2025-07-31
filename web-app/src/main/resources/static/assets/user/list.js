document.body.addEventListener('htmx:afterSwap', function(event) {
    if (event.detail.target.id === 'user-table-container') {
        document.querySelectorAll('wa-dialog[open]').forEach(dialog => {
            dialog.open = false;
        });
        document.getElementById("modal-container").innerHTML = "";
    }
    if (event.detail.target.id === 'modal-container') {
        const dialog = event.detail.target.querySelector('wa-dialog');
        if (dialog) {
            dialog.open = true;
        }
    }
});
