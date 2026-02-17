(function () {
    let markdownbody = document.querySelector(".markdown-body");
    let slideWidth = Math.trunc(markdownbody.clientWidth / 100) * 100;
    let slideHeight = Math.trunc(slideWidth * 9 / 16);
    document.querySelectorAll('.reveal').forEach(async (n, i) => {
        n.style.width = `${slideWidth}px`;
        n.style.height = `${slideHeight}px`;
        n.style.border = "1px solid gray";
        let deck = new Reveal(n, {
            embedded: true,
            controls: false,
            plugins: [RevealMarkdown, RevealHighlight, RevealMath.KaTeX],
            katex: {
                local: katexUrl
            },
            markdown: {
                gfm: true,
                renderer: gme.renderer,
                tokenizer: gme.tokenizer
            }
        });
        deck.initialize().then(gme.revealReady);
    });
})();