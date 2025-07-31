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

async function addPostingLine(sourceRow = null) {
    const tbody = document.getElementById('postingLines');
    if (!tbody) {
        console.error('Could not find postingLines tbody element');
        return;
    }

    const accounts = await window.getAccounts();
    const vatCodes = await window.getVatCodes();
    
    const accountItems = accounts.map(account => ({
        value: account.noAccountNumber,
        title: account.noAccountNumber,
        subtitle: account.accountName,
        displayText: account.noAccountNumber + ' - ' + account.accountName
    }));

    const vatItems = vatCodes.map(vat => ({
        value: vat.code,
        title: vat.code,
        subtitle: '(' + vat.rate + '%) - ' + vat.description,
        displayText: vat.code + ' (' + vat.rate + '%) - ' + vat.description
    }));

    // Get current date in YYYY-MM-DD format
    const today = new Date().toISOString().split('T')[0];
    
    // Get default values from source row if duplicating
    const defaultValues = sourceRow ? {
        postingDate: sourceRow.querySelector('[name*="postingDate"]')?.value || today,
        debitAccount: sourceRow.querySelector('[name*="debitAccount"]')?.value || '',
        debitVatCode: sourceRow.querySelector('[name*="debitVatCode"]')?.value || vatCodes[0]?.code || '',
        creditAccount: sourceRow.querySelector('[name*="creditAccount"]')?.value || '',
        creditVatCode: sourceRow.querySelector('[name*="creditVatCode"]')?.value || vatCodes[0]?.code || '',
        amount: sourceRow.querySelector('[name*="amount"]')?.value || '',
        currency: sourceRow.querySelector('[name*="currency"]')?.value || 'NOK',
        description: sourceRow.querySelector('[name*="description"]')?.value || ''
    } : {
        postingDate: today,
        debitAccount: '',
        debitVatCode: vatCodes[0]?.code || '',
        creditAccount: '',
        creditVatCode: vatCodes[0]?.code || '',
        amount: '',
        currency: 'NOK',
        description: ''
    };

    const newRow = document.createElement('tr');
    newRow.className = 'posting-line-row';
    newRow.id = `posting-line-row-${rowCounter}`;
    
    newRow.innerHTML = `
        <input type="hidden" name="postingLines[${rowCounter}].rowNumber" value="${rowCounter}">
        <td>
            <wa-input type="date" style="width: 9rem;" size="small" 
                      tabindex="${rowCounter * 10 + 1}" 
                      value="${defaultValues.postingDate}"
                      name="postingLines[${rowCounter}].postingDate">
            </wa-input>
        </td>
        <td>
            <div style="display: flex; flex-direction: column; gap: 2px;">
                <r-combobox name="postingLines[${rowCounter}].debitAccount"
                            placeholder="Select debit account..." 
                            tabindex="${rowCounter * 10 + 3}"
                            style="font-size: var(--wa-font-size-xs);">
                </r-combobox>
                <r-combobox name="postingLines[${rowCounter}].debitVatCode"
                            placeholder="Select VAT code..." 
                            tabindex="${rowCounter * 10 + 8}"
                            style="font-size: var(--wa-font-size-xs);">
                </r-combobox>
            </div>
        </td>
        <td>
            <div style="display: flex; flex-direction: column; gap: 2px;">
                <r-combobox name="postingLines[${rowCounter}].creditAccount"
                            placeholder="Select credit account..." 
                            tabindex="${rowCounter * 10 + 2}"
                            style="font-size: var(--wa-font-size-xs);">
                </r-combobox>
                <r-combobox name="postingLines[${rowCounter}].creditVatCode"
                            placeholder="Select VAT code..." 
                            tabindex="${rowCounter * 10 + 7}"
                            style="font-size: var(--wa-font-size-xs);">
                </r-combobox>
            </div>
        </td>
        <td>
            <div style="position: relative; width: 7rem;">
                <wa-input type="number" size="small" 
                          tabindex="${rowCounter * 10 + 4}"
                          name="postingLines[${rowCounter}].amount"
                          min="0" step="0.01" placeholder="Amount"
                          value="${defaultValues.amount}">
                </wa-input>
            </div>
        </td>
        <td>
            <wa-select style="width: 5.6rem" size="small"
                       tabindex="${rowCounter * 10 + 5}"
                       name="postingLines[${rowCounter}].currency">
                <wa-option value="NOK" ${defaultValues.currency === 'NOK' ? 'selected' : ''}>NOK</wa-option>
                <wa-option value="USD" ${defaultValues.currency === 'USD' ? 'selected' : ''}>USD</wa-option>
                <wa-option value="EUR" ${defaultValues.currency === 'EUR' ? 'selected' : ''}>EUR</wa-option>
                <wa-option value="GBP" ${defaultValues.currency === 'GBP' ? 'selected' : ''}>GBP</wa-option>
                <wa-option value="SEK" ${defaultValues.currency === 'SEK' ? 'selected' : ''}>SEK</wa-option>
                <wa-option value="DKK" ${defaultValues.currency === 'DKK' ? 'selected' : ''}>DKK</wa-option>
            </wa-select>
        </td>
        <td>
            <wa-input type="text" size="small"
                      tabindex="${rowCounter * 10 + 6}"
                      placeholder="Description"
                      value="${defaultValues.description}"
                      name="postingLines[${rowCounter}].description">
            </wa-input>
        </td>
        <td>
            <wa-button type="button" size="small" variant="neutral" tabindex="-1"
                       onclick="duplicatePostingLine(this)" style="margin-right: 4px;">
                <wa-icon label="Duplicate posting row" name="copy"></wa-icon>
            </wa-button>
            <wa-button type="button" size="small" variant="danger" tabindex="-1"
                       onclick="removePostingLine(this)">
                <wa-icon label="Remove posting row" name="trash"></wa-icon>
            </wa-button>
        </td>
    `;
    
    tbody.appendChild(newRow);
    
    // Initialize comboboxes for the new row
    const debitAccountCombo = newRow.querySelector('[name*="debitAccount"]');
    const debitVatCombo = newRow.querySelector('[name*="debitVatCode"]');
    const creditAccountCombo = newRow.querySelector('[name*="creditAccount"]');
    const creditVatCombo = newRow.querySelector('[name*="creditVatCode"]');
    
    debitAccountCombo.items = accountItems;
    debitVatCombo.items = vatItems;
    creditAccountCombo.items = accountItems;
    creditVatCombo.items = vatItems;
    
    // Set default values
    if (defaultValues.debitAccount) debitAccountCombo.value = defaultValues.debitAccount;
    if (defaultValues.debitVatCode) debitVatCombo.value = defaultValues.debitVatCode;
    if (defaultValues.creditAccount) creditAccountCombo.value = defaultValues.creditAccount;
    if (defaultValues.creditVatCode) creditVatCombo.value = defaultValues.creditVatCode;
    
    // Add change listeners to comboboxes
    [debitAccountCombo, debitVatCombo, creditAccountCombo, creditVatCombo].forEach(combo => {
        combo.addEventListener('change', () => updateBalance());
    });
    
    // Add change listeners to other input fields
    const amountInput = newRow.querySelector('[name*="amount"]');
    const currencySelect = newRow.querySelector('[name*="currency"]');
    const descriptionInput = newRow.querySelector('[name*="description"]');
    const dateInput = newRow.querySelector('[name*="postingDate"]');
    
    [amountInput, currencySelect, descriptionInput, dateInput].forEach(input => {
        if (input) {
            input.addEventListener('change', () => updateBalance());
            input.addEventListener('input', () => updateBalance());
        }
    });
    
        rowCounter++;
    
    // Add slight delay to ensure DOM is updated before calculating balance
    setTimeout(() => {
        updateBalance();
    }, 100);
}

function duplicatePostingLine(button) {
    const sourceRow = button.closest('tr');
    addPostingLine(sourceRow);
}

function removePostingLine(button) {
    const row = button.closest('tr');
    const rows = document.querySelectorAll('.posting-line-row');

    if (rows.length > 1) {
        row.remove();
        renumberPostingRows();
        setTimeout(() => {
            updateBalance();
        }, 100);
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

        const formFields = row.querySelectorAll('input, select, r-combobox, wa-input, wa-select');
        formFields.forEach(field => {
            if (field.name) {
                field.name = field.name.replace(/postingLines\[\d+\]/, `postingLines[${index}]`);

                if (field.tagName === 'WA-INPUT' || field.tagName === 'WA-SELECT') {
                    field.dispatchEvent(new Event('change', { bubbles: true }));
                }
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

document.addEventListener('change', (e) => {
    if (e.target.matches('input[type="number"], select, wa-input, wa-select')) {
        setTimeout(updateBalance, 100);
    }
});

document.addEventListener('htmx:configRequest', (e) => {
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