// Theme toggle: light / dark / system
// Saves choice to localStorage, applies to <body> immediately on page load.

(function () {
    const STORAGE_KEY = 'hopeconnect-theme';

    function getStoredTheme() {
        return localStorage.getItem(STORAGE_KEY) || 'system';
    }

    function systemPrefersDark() {
        return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
    }

    function applyTheme(theme) {
        const isDark = theme === 'dark' || (theme === 'system' && systemPrefersDark());
        document.documentElement.classList.toggle('dark', isDark);
        if (document.body) {
            document.body.classList.toggle('dark', isDark);
        }
    }

    // Apply immediately to avoid flash
    applyTheme(getStoredTheme());

    // Re-apply once DOM is ready (body exists)
    document.addEventListener('DOMContentLoaded', function () {
        applyTheme(getStoredTheme());
    });

    // Watch for system changes if "system" is selected
    if (window.matchMedia) {
        window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function () {
            if (getStoredTheme() === 'system') applyTheme('system');
        });
    }

    // Public API for the settings page
    window.HopeConnectTheme = {
        get: getStoredTheme,
        set: function (theme) {
            localStorage.setItem(STORAGE_KEY, theme);
            applyTheme(theme);
        }
    };
})();
