// accessibility.js
document.addEventListener('DOMContentLoaded', () => {

    const htmlEl = document.documentElement;
    const bodyEl = document.body;

    // --- State Management ---
    const accessibilityState = {
        fontSize: parseInt(localStorage.getItem('a11y_fontSize')) || 16,
        dyslexia: localStorage.getItem('a11y_dyslexia') === 'true',
        highContrast: localStorage.getItem('a11y_highContrast') === 'true',
        highlightLinks: localStorage.getItem('a11y_highlightLinks') === 'true',
        hideImages: localStorage.getItem('a11y_hideImages') === 'true',
        bigCursor: localStorage.getItem('a11y_bigCursor') === 'true',
        textSpacing: localStorage.getItem('a11y_textSpacing') === 'true',
        lineHeight: localStorage.getItem('a11y_lineHeight') === 'true',
        tts: localStorage.getItem('a11y_tts') === 'true',
        lightMode: localStorage.getItem('a11y_lightMode') === 'true',
        widgetOpen: localStorage.getItem('a11y_widgetOpen') === 'true'
    };

    // --- Apply state to DOM ---
    const applyState = () => {
        // Font Size
        htmlEl.style.fontSize = accessibilityState.fontSize + 'px';

        // CSS Classes on body
        bodyEl.classList.toggle('a11y-dyslexia', accessibilityState.dyslexia);
        bodyEl.classList.toggle('a11y-high-contrast', accessibilityState.highContrast);
        bodyEl.classList.toggle('a11y-highlight-links', accessibilityState.highlightLinks);
        bodyEl.classList.toggle('a11y-hide-images', accessibilityState.hideImages);
        bodyEl.classList.toggle('a11y-big-cursor', accessibilityState.bigCursor);
        bodyEl.classList.toggle('a11y-text-spacing', accessibilityState.textSpacing);
        bodyEl.classList.toggle('a11y-line-height', accessibilityState.lineHeight);
        bodyEl.classList.toggle('a11y-light-mode', accessibilityState.lightMode);

        // Update widget UI toggles if they exist
        updateWidgetUI();
    };

    const saveState = () => {
        localStorage.setItem('a11y_fontSize', accessibilityState.fontSize);
        localStorage.setItem('a11y_dyslexia', accessibilityState.dyslexia);
        localStorage.setItem('a11y_highContrast', accessibilityState.highContrast);
        localStorage.setItem('a11y_highlightLinks', accessibilityState.highlightLinks);
        localStorage.setItem('a11y_hideImages', accessibilityState.hideImages);
        localStorage.setItem('a11y_bigCursor', accessibilityState.bigCursor);
        localStorage.setItem('a11y_textSpacing', accessibilityState.textSpacing);
        localStorage.setItem('a11y_lineHeight', accessibilityState.lineHeight);
        localStorage.setItem('a11y_tts', accessibilityState.tts);
        localStorage.setItem('a11y_lightMode', accessibilityState.lightMode);
        localStorage.setItem('a11y_widgetOpen', accessibilityState.widgetOpen);
        applyState();
    };

    // --- Actions ---
    window.a11yChangeFontSize = (step) => {
        if (step === 0) {
            accessibilityState.fontSize = 16;
        } else {
            accessibilityState.fontSize += step;
            if (accessibilityState.fontSize < 12) accessibilityState.fontSize = 12;
            if (accessibilityState.fontSize > 24) accessibilityState.fontSize = 24;
        }
        saveState();
    };

    window.a11yToggle = (feature) => {
        if (accessibilityState[feature] !== undefined) {
            accessibilityState[feature] = !accessibilityState[feature];
            saveState();
        }
    };

    window.a11yReset = () => {
        accessibilityState.fontSize = 16;
        accessibilityState.dyslexia = false;
        accessibilityState.highContrast = false;
        accessibilityState.highlightLinks = false;
        accessibilityState.hideImages = false;
        accessibilityState.bigCursor = false;
        accessibilityState.textSpacing = false;
        accessibilityState.lineHeight = false;
        accessibilityState.tts = false;
        accessibilityState.lightMode = false;
        saveState();
    };

    window.a11yToggleWidget = () => {
        accessibilityState.widgetOpen = !accessibilityState.widgetOpen;
        saveState();
        const widget = document.getElementById('a11y-widget-panel');
        if (widget) {
            widget.classList.toggle('hidden', !accessibilityState.widgetOpen);
        }
    };

    // --- Widget UI Update ---
    const updateWidgetUI = () => {
        const toggleBtn = (id, active) => {
            const btn = document.getElementById(id);
            if (btn) {
                if (active) {
                    btn.classList.add('ring-2', 'ring-blue-600', 'bg-blue-50');
                    btn.classList.remove('border-gray-200');
                } else {
                    btn.classList.remove('ring-2', 'ring-blue-600', 'bg-blue-50');
                    btn.classList.add('border-gray-200');
                }
            }
        };

        toggleBtn('btn-a11y-tts', accessibilityState.tts);
        toggleBtn('btn-a11y-dyslexia', accessibilityState.dyslexia);
        toggleBtn('btn-a11y-high-contrast', accessibilityState.highContrast);
        toggleBtn('btn-a11y-highlight-links', accessibilityState.highlightLinks);
        toggleBtn('btn-a11y-hide-images', accessibilityState.hideImages);
        toggleBtn('btn-a11y-big-cursor', accessibilityState.bigCursor);
        toggleBtn('btn-a11y-text-spacing', accessibilityState.textSpacing);
        toggleBtn('btn-a11y-line-height', accessibilityState.lineHeight);

        const panel = document.getElementById('a11y-widget-panel');
        if (panel) {
            panel.classList.toggle('hidden', !accessibilityState.widgetOpen);
        }
    };

    // --- Text To Speech ---
    document.addEventListener('mouseup', () => {
        if (!accessibilityState.tts) return;
        const selectedText = window.getSelection().toString().trim();
        if (selectedText.length > 0) {
            window.speechSynthesis.cancel();
            const utterance = new SpeechSynthesisUtterance(selectedText);
            window.speechSynthesis.speak(utterance);
        }
    });

    // Initialize
    applyState();
});
