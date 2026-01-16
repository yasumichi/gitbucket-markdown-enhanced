(function () {
    mermaid.initialize({ startOnLoad: false });
    var renderer = new marked.Renderer();
    var mid = 1;

    renderer.code = (code, language) => {
        var html = "";

        switch (language) {
            case "mermaid":
                html = `<div class="mermaid" id="mermaid-${mid}">` + code + '</div>';
                break;
            default:
                var outlang = language;
                if (language.includes(":") ) {
                    outlang = language.split(":")[0];
                }
                
                html = `<div class="my-custom-code-block ${outlang ? `lang-${outlang}` : ''}">
                    <pre><code class="my-code-snippet">${code}</code></pre>
                    </div>`;
        }

        return html;
    };

    const processMermaid = function (currentSlide) {
        let nodes = currentSlide.querySelectorAll('.mermaid');
        if (nodes.length > 0) {
            mermaid.run({nodes: nodes});
        }
    };

    const slidechanged = function (e) {
        processMermaid(e.currentSlide);
    };

    const revealReady = function (e) {
        processMermaid(e.currentSlide);
        Reveal.on('slidechanged', slidechanged)
    };

    // More info about initialization & config:
    // - https://revealjs.com/initialization/
    // - https://revealjs.com/config/
    Reveal.initialize({
        hash: true,

        // Learn about plugins: https://revealjs.com/plugins/
        plugins: [ RevealMarkdown, RevealHighlight, RevealNotes, RevealMath.KaTeX ],
        markdown: {
            gfm: true,
            renderer: renderer
        }
    }).then(revealReady);
})();