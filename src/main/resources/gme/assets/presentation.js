(function () {
    mermaid.initialize({ startOnLoad: false });
    var renderer = new marked.Renderer();
    var vegaNumber = 1;
    var pumlNumber = 1;
    var krokiNumber = 0;

    // match an optional line number offset and highlight line numbers
    // [<line numbers>] or [<offset>: <line numbers>]
    // from https://github.com/hakimel/reveal.js/blob/33bfe3b233f1a840cd70e834b609ec6f04494a40/plugin/markdown/plugin.js#L17
    const CODE_LINE_NUMBER_REGEX = /\[\s*((\d*):)?\s*([\s\d,|-]*)\]/;

    const HTML_ESCAPE_MAP = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    };

    // from https://github.com/hakimel/reveal.js/blob/33bfe3b233f1a840cd70e834b609ec6f04494a40/plugin/markdown/plugin.js#L412
    const escapeForHTML = ( input ) => {
	  return input.replace( /([&<>'"])/g, char => HTML_ESCAPE_MAP[char] );
	}

    /**
     * Process code blocks
     *
     * @param {*} code code inside code block
     * @param {*} language Language the code is written in
     * @returns Formatted code block
     */
    renderer.code = (code, language) => {
        var html = "";

        // check kroki
        var krokiPatterns = [/(.*)\s*\{.*kroki\s*=\s*true.*\}/, /.*\{.*kroki\s*=\s*"(.*)".*\}/];
        var isKroki = false;

        krokiPatterns.forEach((pattern) =>{
            var match = pattern.exec(language);
            if (match) {
                krokiNumber++;
                isKroki = true;
                html = `<div id="kroki-${krokiNumber}"></div>
                    <script type="kroki" class="kroki" data-type="${match[1].trim()}" data-target="#kroki-${krokiNumber}">
                    ${code}
                    </script>
                `;
            }
        })

        if (isKroki) {
            return html;
        }

        // other code block
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
                // from https://github.com/hakimel/reveal.js/blob/33bfe3b233f1a840cd70e834b609ec6f04494a40/plugin/markdown/plugin.js#L436
                // Off by default
                let lineNumberOffset = '';
                let lineNumbers = '';

                // Users can opt in to show line numbers and highlight
                // specific lines.
                // ```javascript []        show line numbers
                // ```javascript [1,4-8]   highlights lines 1 and 4-8
                // optional line number offset:
                // ```javascript [25: 1,4-8]   start line numbering at 25,
                //                             highlights lines 1 (numbered as 25) and 4-8 (numbered as 28-32)
                if( CODE_LINE_NUMBER_REGEX.test( language ) ) {
                    let lineNumberOffsetMatch =  language.match( CODE_LINE_NUMBER_REGEX )[2];
                    if (lineNumberOffsetMatch){
                        lineNumberOffset =  `data-ln-start-from="${lineNumberOffsetMatch.trim()}"`;
                    }

                    lineNumbers = language.match( CODE_LINE_NUMBER_REGEX )[3].trim();
                    lineNumbers = `data-line-numbers="${lineNumbers}"`;
                    language = language.replace( CODE_LINE_NUMBER_REGEX, '' ).trim();
                }

                // Escape before this gets injected into the DOM to
                // avoid having the HTML parser alter our code before
                // highlight.js is able to read it
                code = escapeForHTML( code );

                var outlang = language;
                if (language.includes(":") ) {
                    outlang = language.split(":")[0];
                }

                html = `<pre><code ${lineNumbers} ${lineNumberOffset} class="${outlang}">${code}</code></pre>`
        }

        return html;
    };

    /**
     * Process inline text
     * 
     * @param {*} token inline text
     * @returns converted iniline text
     */
    renderer.text = (token) => {
        console.log(token);

        emojiPattern = /:([^:]+):/g; 

        var matches = [...token.matchAll(emojiPattern)];
        matches.forEach((match) => {
            if (emoji[match[1]]) {
                token = token.replace(match[0], emoji[match[1]]);
            }
        });

        token = token.replaceAll(/~~([^~]+)~~/g, "<del>$1</del>");
        token = token.replaceAll(/~([^~]+)~/g, "<sub>$1</sub>");
        token = token.replaceAll(/==([^=]+)==/g, "<mark>$1</mark>");
        if (!token.includes('$')) {
            token = token.replaceAll(/\^([^\^]+)\^/g, "<sup>$1</sup>");
        }

        return token;
    };

    const tokenizer = new marked.Tokenizer();

    /**
     * Disable default strikethrough in Marked 
     */
    tokenizer.del = (src) => {
        return false;
    };

    /**
     * Process relative paths
     *
     * @returns Promise
     */
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

    /**
     * Convert Mermaid notation in the current slide to diagrams
     *
     * @param {*} currentSlide Reference to the current slide
     */
    const processMermaid = function (currentSlide) {
        let nodes = currentSlide.querySelectorAll('.mermaid');
        if (nodes.length > 0) {
            mermaid.run({nodes: nodes}).then(() => Reveal.layout());
        }
    };

    /**
     * Converts PlantUML syntax into diagrams
     *
     * @param {*} content PlantUML syntax
     * @returns Promise
     */
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

    /**
     * Convert Kroki.io notation to diagrams
     *
     * @returns Promise
     */
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

    /**
     * Convert vega and vega-lite notation to diagrams
     *
     * @returns Promise
     */
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

    /**
     * Process current slide
     *
     * @param {*} currentSlide Reference to the current slide
     */
    const processCurrentSlide = (currentSlide) => {
        processMermaid(currentSlide);
    };

    /**
     * Event handler for capturing slide transitions
     * 
     * @param {*} e slide transitions event
     */
    const slidechanged = function (e) {
        processCurrentSlide(e.currentSlide);
    };

    /**
     * Event handler to capture the reveal.js "ready" event
     *  
     * @param {*} e the reveal.js "ready" event
     */
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
        pdfSeparateFragments: false,

        // Learn about plugins: https://revealjs.com/plugins/
        plugins: [ RevealMarkdown, RevealHighlight, RevealNotes, RevealMath.KaTeX ],
        katex: {
            local: katexUrl
        },
        markdown: {
            gfm: true,
            renderer: renderer,
            tokenizer: tokenizer
        }
    }).then(revealReady);
})();