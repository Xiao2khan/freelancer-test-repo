// HTMX Configuration
htmx.config.globalViewTransitions = true;

// Show loading states
document.addEventListener('htmx:beforeRequest', function (evt) {
    const target = evt.target;
    if (target.tagName === 'WA-BUTTON') {
        target.loading = true;
    }
});

document.addEventListener('htmx:afterRequest', function (evt) {
    const target = evt.target;
    if (target.tagName === 'WA-BUTTON') {
        target.loading = false;
    }
});

// Auto-hide success alerts after 5 seconds
setTimeout(function () {
    const alerts = document.querySelectorAll('wa-alert[variant="success"]');
    alerts.forEach(function (alert) {
        alert.style.transition = 'opacity 0.5s';
        alert.style.opacity = '0';
        setTimeout(() => alert.remove(), 500);
    });
}, 5000);

// Handle navigation
document.addEventListener('DOMContentLoaded', function () {
    // Close drawer on navigation link click (mobile)
    const navigationLinks = document.querySelectorAll("[slot='navigation'] a[href]:not([href='#'])");
    navigationLinks.forEach(link => {
        link.addEventListener('click', function () {
            // Close mobile navigation drawer
            const page = document.querySelector('wa-page');
            if (page && page.getAttribute('view') === 'mobile') {
                page.setAttribute('drawer', 'closed');
            }
        });
    });

    // Handle section anchors
    const sectionAnchors = document.querySelectorAll("[slot*='navigation'] a[href*='#']");
    sectionAnchors.forEach(sectionAnchor => sectionAnchor.setAttribute('data-drawer', 'close'));

    document.addEventListener("keydown", handleShortcutEvent);
});

// Handle clickable callouts
document.addEventListener('click', function(e) {
    const callout = e.target.closest('wa-callout[data-link]');
    if (callout) {
        const link = callout.getAttribute('data-link');
        if (link) {
            window.location.href = link;
        }
    }
});

function handleShortcutEvent(event) {
    if (shortcutAction && shortcutAction.length > 0) {
        const combo = normalizeKeyCombination(event);

        const matchedShortcut = shortcutAction.find(
            s => s.keyCombination.toLowerCase() === combo
        );

        if (matchedShortcut) {
            event.preventDefault();
            const action = shortcutMap[matchedShortcut.actionId];
            if (action) {
                action();
            } else {
                console.warn("No handler for actionId:", matchedShortcut.actionId);
            }
        }
    }
}

function normalizeKeyCombination(event) {
    const keys = [];
    if (event.ctrlKey) keys.push("ctrl");
    if (event.altKey) keys.push("alt");
    if (event.shiftKey) keys.push("shift");
    const key = event.key.toLowerCase();
    keys.push(key);
    return keys.join("+");
}

document.addEventListener('htmx:configRequest', e => {
    const spinner = document.getElementById('loading-indicator');
    if (spinner) {
        spinner.style.display = 'inline-block';
    }
});

document.addEventListener('htmx:afterRequest', e => {
    const spinner = document.getElementById('loading-indicator');
    if (spinner) {
        spinner.style.display = 'none';
    }
});

function activateSplit() {
    const splitPanel = document.getElementById('main-split-panel');
    const closeButton = document.getElementById('close-split-btn');
    if (splitPanel) {
        const isMobile = window.innerWidth < 920;
        splitPanel.position = isMobile ? 0 : 60;
        splitPanel.disabled = false;
        splitPanel.classList.remove('r-split-panel-disabled');
        if (closeButton) {
            closeButton.style.display = 'block';
        }
    }
}

function disableSplit() {
    const splitPanel = document.getElementById('main-split-panel');
    const closeButton = document.getElementById('close-split-btn');
    if (splitPanel) {
        splitPanel.position = 100;
        splitPanel.disabled = true;
        splitPanel.classList.add('r-split-panel-disabled');
        if (closeButton) {
            closeButton.style.display = 'none';
        }
    }
}