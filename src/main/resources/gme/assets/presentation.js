(function () {
    var controls = [
        {
            icon: '<i class="fa fa-pen-square"></i>',
            title: 'Toggle chalkboard (B)',
            action: 'RevealChalkboard.toggleChalkboard();'
        },
        {
            icon: '<i class="fa fa-pen"></i>',
            title: 'Toggle notes canvas (C)',
            action: 'RevealChalkboard.toggleNotesCanvas();'
        },
        {
            icon: '<i class="fa-solid fa-print"></i>',
            title: 'PDF Export',
            action: 'togglePrintPDF();'
        }
    ];
    if (location.search === "?print-pdf") {
        controls = [
            {
                icon: '<i class="fa-solid fa-person-chalkboard"></i>',
                title: 'Presentation',
                action: 'togglePrintPDF();'
            }
        ];
    }
    
    const markdownSection = document.getElementById('markdown-section');
    fetch(markdownUrl)
        .then(res => {
            if (res.ok) {
                return res.text();
            }
        })
        .then(data => {
            const matterRegex = /^---[\r\n]+([\s\S]*?)[\r\n]+---[\r\n]+/;
            const match = data.match(matterRegex);

            if (match) {
                const yamlString = match[1]; // ---の間のYAML部分
                markdownSection.textContent = data.replace(matterRegex, '');
                  try {
                      const metadata = jsyaml.load(yamlString);
                      console.log(metadata);
                  } catch (e) {
                      console.error("Failed to parse YAML", e);
                  }
            } else {
                markdownSection.textContent = data;
            }

            // More info about initialization & config:
            // - https://revealjs.com/initialization/
            // - https://revealjs.com/config/
            Reveal.initialize({
                customcontrols: {
                    controls: controls
                },
                chalkboard: {

                },
                hash: true,
                pdfSeparateFragments: false,

                // Learn about plugins: https://revealjs.com/plugins/
                plugins: [ RevealMarkdown, RevealHighlight, RevealNotes, RevealMath.KaTeX, RevealChalkboard, RevealCustomControls ],
                katex: {
                    local: katexUrl
                },
                markdown: {
                    gfm: true,
                    renderer: gme.renderer,
                    tokenizer: gme.tokenizer
                }
            }).then(gme.revealReady);
        });
})();