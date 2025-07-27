let rowCounter = 0;

function waitForFunctions() {
    return new Promise((resolve) => {
        const checkFunctions = () => {
            if (typeof window.getAccounts === 'function' && 
                typeof window.getVatCodes === 'function') {
                resolve();
            } else {
                setTimeout(checkFunctions, 50);
            }
        };
        checkFunctions();
    });
}

document.addEventListener('DOMContentLoaded', async function () {
    // Wait for functions to be available
    await waitForFunctions();
    
    // Only add a posting line if there are no existing posting lines
    const existingPostings = document.querySelectorAll('.posting-line-row');
    if (existingPostings.length === 0) {
        addPostingLine();
    } else {
        rowCounter = existingPostings.length;
        setTimeout(updateBalance, 200)
    }

    if (typeof customElements !== 'undefined' && customElements.whenDefined) {
        customElements.whenDefined('r-combobox').then(() => {
            initializeExistingPostingComboboxes();
        });
    } else {
        setTimeout(initializeExistingPostingComboboxes, 100);
    }
});

const shortcutMap = {
    'saveVoucher': () => document.getElementById("saveButton")?.click(),
    'addNewRow': () => addPostingLine(),
};

async function initializeExistingPostingComboboxes() {
    try {
        // Check if functions are available
        if (typeof window.getAccounts !== 'function') {
            console.error('getAccounts function is not available');
            return;
        }
        if (typeof window.getVatCodes !== 'function') {
            console.error('getVatCodes function is not available');
            return;
        }

        const accounts = await window.getAccounts();
        const vatCodes = await window.getVatCodes();
        
        if (!accounts || !vatCodes) {
            console.error('Failed to load accounts or vat codes');
            return;
        }
        
        const accountItems = accounts.map(account => ({
            value: account.noAccountNumber,
            title: account.noAccountNumber,
            subtitle: account.accountName,
            displayText: account.noAccountNumber + ' - ' + account.accountName
        }));

        // Setup VAT code comboboxes
        const vatItems = vatCodes.map(vat => ({
            value: vat.code,
            title: vat.code,
            subtitle: '(' + vat.rate + '%) - ' + vat.description,
            displayText: vat.code + ' (' + vat.rate + '%) - ' + vat.description
        }));

        // Initialize all existing comboboxes
        document.querySelectorAll('r-combobox').forEach(combobox => {
            if (combobox.id.includes('account')) {
                combobox.items = accountItems;
            } else if (combobox.id.includes('vat')) {
                combobox.items = vatItems;
            }
            combobox.addEventListener('change', () => updateBalance());
        });
    } catch (error) {
        console.error('Error initializing posting comboboxes:', error);
    }
}

function addPostingLine() {
    const tbody = document.getElementById('postingLines');
    if (!tbody) {
        console.error('Could not find postingLines tbody element');
        return;
    }

    const tenantId = new URLSearchParams(window.location.search).get('tenantId');
    const url = tenantId ?
        `/htmx/voucher/add-posting-line?rowCounter=${rowCounter}&tenantId=${tenantId}` :
        `/htmx/voucher/add-posting-line?rowCounter=${rowCounter}`;


    htmx.ajax('GET', url, {
        target: '#postingLines',
        swap: 'beforeend',
        headers: {
            'HX-Request': 'true'
        }
    }).then(() => {
        rowCounter++;
        updateBalance();
    }).catch(error => {
        console.error('Error adding posting line:', error);
    });
}

function removePostingLine(button, rowIndex) {
    const row = button.closest('tr');
    const rows = document.querySelectorAll('.posting-line-row');

    if (rows.length > 1) {
        row.remove();
        renumberPostingRows();
        updateBalance();
    }
}

function renumberPostingRows() {
    const rows = document.querySelectorAll('.posting-line-row');
    rows.forEach((row, index) => {
        row.id = `posting-line-row-${index}`;

        const hiddenRowNumber = row.querySelector('input[type="hidden"]');
        if (hiddenRowNumber) {
            hiddenRowNumber.name = `postingLines[${index}].rowNumber`;
            hiddenRowNumber.value = index;
        }

        const formFields = row.querySelectorAll('input, select, r-combobox');
        formFields.forEach(field => {
            if (field.name) {
                field.name = field.name.replace(/postingLines\[\d+\]/, `postingLines[${index}]`);
            }
        });

        const tabindexFields = row.querySelectorAll('[tabindex]');
        tabindexFields.forEach(field => {
            const currentTabindex = parseInt(field.getAttribute('tabindex'));
            const baseTabindex = index * 10;
            const offset = currentTabindex % 10;
            field.setAttribute('tabindex', baseTabindex + offset);
        });
    });

    rowCounter = rows.length;
}

function updateBalance() {
    const form = document.getElementById('voucherForm');
    if (!form) return;

    htmx.ajax('POST', '/htmx/voucher/update-balance', {
        source: form,
        target: '#balanceFooter',
        swap: 'innerHTML',
        headers: {
            'HX-Request': 'true'
        }
    }).then(() => {
    }).catch(error => {
        console.error('Error updating balance:', error);
    });
}

function showValidationError(message) {
    const messagesDiv = document.getElementById('form-messages');
    if (messagesDiv) {
        messagesDiv.innerHTML = `<div style="margin-bottom: var(--wa-space-m);">
            <wa-callout variant="danger" open>
                <wa-icon slot="icon" name="circle-exclamation" variant="regular"></wa-icon>
                <span>${message}</span>
            </wa-callout></div>`;
    }
}

document.addEventListener('htmx:responseError', (e) => {
    console.error('HTMX Error:', e.detail);
    showValidationError('An error occurred. Please try again.');
});

document.addEventListener('htmx:sendError', (e) => {
    console.error('HTMX Network Error:', e.detail);
    showValidationError('Network error. Please check your connection.');
});

document.addEventListener('change', (e) => {
    if (e.target.matches('input[type="number"], select, wa-input, wa-select')) {
        setTimeout(updateBalance, 100);
    }
});

document.addEventListener('htmx:configRequest', (e) => {
    const tenantId = new URLSearchParams(window.location.search).get('tenantId');
    if (tenantId && e.detail.path.includes('/htmx/')) {
        e.detail.parameters.tenantId = tenantId;
    }

    if (e.detail.elt && e.detail.elt.tagName === 'FORM') {
        const comboboxes = e.detail.elt.querySelectorAll('r-combobox');
        comboboxes.forEach(combobox => {
            if (combobox.name && combobox.value) {
                e.detail.parameters[combobox.name] = combobox.value;
            }
        });

        const amountInputs = e.detail.elt.querySelectorAll('input[name*="amount"]');
        amountInputs.forEach(input => {
            if (input.value && !isNaN(parseFloat(input.value))) {
                const roundedValue = parseFloat(input.value).toFixed(2);
                e.detail.parameters[input.name] = roundedValue;
            }
        });
    }
});