function setMermaidTheme(theme) {
    mermaid.initialize({
        startOnLoad: true,
        theme: theme
    });
    mermaid.init(undefined, document.querySelectorAll('.mermaid'));
}

document.addEventListener('DOMContentLoaded', function () {
    const themeToggleButton = document.querySelector('[data-md-component="palette"]');
    const palette = JSON.parse(localStorage.getItem('/.__palette'));
    const theme = palette.color.scheme === 'default' ? 'light' : 'dark';

    setMermaidTheme(theme);

    if (themeToggleButton) {
        themeToggleButton.addEventListener('click', function () {
            location.reload();
        });
    }
});
