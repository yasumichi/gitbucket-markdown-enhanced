(function () {
    mermaid.initialize({ startOnLoad: false });
    var renderer = new marked.Renderer();
    var vegaNumber = 1;
    var pumlNumber = 1;
    var krokiNumber = 0;

    renderer.code = (code, language) => {
        var html = "";
        var krokitrue = /(.*)\s*\{.*kroki\s*=\s*true.*\}/;
        var krokidia = /.*\{.*kroki\s*=\s*"(.*)".*\}/;

        var match = krokitrue.exec(language);
        if (match) {
            krokiNumber++;
            return `<div id="kroki-${krokiNumber}"></div>
                <script type="kroki" class="kroki" data-type="${match[1].trim()}" data-target="#kroki-${krokiNumber}">
                ${code}
                </script>
            `;
        }
        match = krokidia.exec(language);
        if (match) {
            krokiNumber++;
            return `<div id="kroki-${krokiNumber}"></div>
                <script type="kroki" class="kroki" data-type="${match[1].trim()}" data-target="#kroki-${krokiNumber}">
                ${code}
                </script>
            `;
        }

        switch (language) {
            case "dot":
                html = `<div id="puml-${pumlNumber}"></div>
                <script type="plantuml" class="plantuml" data-target="#puml-${pumlNumber}">
                @startdot
                ${code}
                @enddot
                </script>
                `;
                pumlNumber++;
                break;
            case "math":
                html = '<div>$$' + code + '$$</div>';
                break;
            case "mermaid":
                html = '<div class="mermaid">' + code + '</div>';
                break;
            case "plantuml":
            case "puml":
                html = `<div id="puml-${pumlNumber}"></div>
                <script type="plantuml" class="plantuml" data-target="#puml-${pumlNumber}">${code}</script>
                `;
                pumlNumber++;
                break;
            case "wavedrom":
                html = '<script type="wavedrom">' + code + '</script>';
                break;
            case "vega":
            case "vega-lite":
                html = `<div id="vega-${vegaNumber}"></div>
                <script type="${language}" class="vega" data-target="#vega-${vegaNumber}">${code}</script>
                `;
                vegaNumber++;
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

    renderer.text = (token) => {
        console.log(token);

        token = token.replaceAll(/~~([^~]+)~~/g, "<del>$1</del>");
        token = token.replaceAll(/~([^~]+)~/g, "<sub>$1</sub>");
        token = token.replaceAll(/==([^=]+)==/g, "<mark>$1</mark>");
        if (!token.includes('$')) {
            token = token.replaceAll(/\^([^\^]+)\^/g, "<sup>$1</sup>");
        }

        return token;
    };

    const tokenizer = new marked.Tokenizer();

    tokenizer.del = (src) => {
        return false;
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

    const processPlantUML = (content) => {
        return new Promise((resolve, reject) =>{
            let pumlList = document.querySelectorAll('.plantuml');
            pumlList.forEach((node, index) => {
                let pumlId = node.getAttribute('data-target');
                let target = document.querySelector(pumlId);
                const params = new URLSearchParams();
                params.append("content", node.textContent);
                const options = {
                    method: 'POST',
                    body: params
                };
                const res = fetch(pumlUrl, options)
                    .then(res => {
                        if (res.ok) {
                            return res.text();
                        }
                    })
                    .then(data => {
                        target.innerHTML = data;
                        Reveal.layout();
                    });
            });
            resolve();
        });
    };

    const processKroki = () => {
        return new Promise((resolve, reject) =>{
            let krokiList = document.querySelectorAll('.kroki');
            krokiList.forEach((node, index) => {
                let krokiId = node.getAttribute('data-target');
                let target = document.querySelector(krokiId);
                const params = new URLSearchParams();
                params.append("diagram_source", node.textContent);
                params.append("diagram_type", node.getAttribute('data-type'));
                params.append("output_format", "svg")
                const options = {
                    method: 'POST',
                    body: params
                };
                fetch(krokiUrl, options)
                    .then(res => {
                        if (res.ok) {
                            return res.text();
                        }
                    })
                    .then(data => {
                        target.innerHTML = data;
                        Reveal.layout();
                    });
            });
            resolve();
        });
    };

    var renderVega = function() {
        return new Promise((resolve, reject) => {
            let vegaList = document.querySelectorAll('.vega');
            vegaList.forEach((node, index) => {
                let vegaId = node.getAttribute('data-target');
                try {
                    let vegaData = JSON.parse(node.textContent);
                    vegaEmbed(vegaId, vegaData);
                } catch (error) {
                    let node = document.querySelector(vegaId)
                    if (node) {
                        node.textContent = error.message;
                    }
                }
            });
            resolve();
        });
    }

    const processCurrentSlide = (currentSlide) => {
        processMermaid(currentSlide);
    };

    const slidechanged = function (e) {
        processCurrentSlide(e.currentSlide);
    };

    const revealReady = function (e) {
        processRelativePath();
        processPlantUML();
        processKroki();
        WaveDrom.ProcessAll();
        renderVega();
        if (location.search.includes("print-pdf")) {
            mermaid.run();
        } else {
            processCurrentSlide(e.currentSlide);
        }
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
            renderer: renderer,
            tokenizer: tokenizer
        }
    }).then(revealReady);
})();