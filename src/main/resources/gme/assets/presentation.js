(function () {
    mermaid.initialize({ startOnLoad: false });
    var renderer = new marked.Renderer();

    renderer.code = (code, language) => {
        var html = "";

        switch (language) {
            case "mermaid":
                html = '<div class="mermaid">' + code + '</div>';
                break;
            case "wavedrom":
                html = '<script type="wavedrom">' + code + '</script>';
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

    const processRelativePath = function () {
        return new Promise((resolve, reject) => {
            const imgs = document.querySelectorAll('img'); 
            imgs.forEach((n, i) =>{
                let src = n.getAttribute('src');
                if (!src.startsWith('http://') && !src.startsWith("https://") && !src.startsWith("/") && !src.startsWith("#")) {
                    console.log(relativePathBase);
                    let url = new URL(encodeURIComponent(src), relativePathBase).href;
                    n.src = url;
                }
            });
            resolve();
        });
    };

    const processMermaid = function (currentSlide) {
        let nodes = currentSlide.querySelectorAll('.mermaid');
        if (nodes.length > 0) {
            mermaid.run({nodes: nodes}).then(() => Reveal.layout());
        }
    };

    const processCurrentSlide = (currentSlide) => {
        processMermaid(currentSlide);
    };

    const slidechanged = function (e) {
        processCurrentSlide(e.currentSlide);
    };

    const revealReady = function (e) {
        processRelativePath();
        WaveDrom.ProcessAll();
        processCurrentSlide(e.currentSlide);
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