(function () {
    console.log("gme.js loaded.");

    var updateCellStyle = function() {
        ['center', 'right'].forEach(function(align) {
            [ `td[align=${align}]`, `th[align=${align}]` ].forEach(function(selector) {
                document.querySelectorAll(selector).forEach(function(elem) {
                    elem.style.textAlign = align;
                });
            });
        });
    };

    var renderKatex = function() {
        var mathElems = document.getElementsByClassName("katex");
        var elems = [];
        for (const i in mathElems) {
            if (mathElems.hasOwnProperty(i)) elems.push(mathElems[i]);
        }

        elems.forEach(elem => {
            katex.render(elem.textContent, elem, { throwOnError: false, displayMode: elem.nodeName !== 'SPAN', });
        });
    };


    document.addEventListener("DOMContentLoaded", function(){
        var preview = document.querySelector("#preview");
        if (preview) {
            const config = { attributes: false, childList: true, subtree: false };

            const observer = new MutationObserver((mutations) => {
                mutations.forEach(async (mutation) => {
                    if (mutation.addedNodes.length == 1) {
                        observer.disconnect();
                        renderKatex();
                        await mermaid.run();
                        WaveDrom.ProcessAll();
                        updateCellStyle();
                        observer.observe(preview, config);
                    }
                });
            });

            observer.observe(preview, config);
        } else if (document.location.toString().includes("/issues/") ||
                   document.location.toString().includes("/pull/")  ||
                   (document.location.toString().includes("/wiki/") && document.location.toString().includes("/_edit"))) {
            const config = { attributes: false, childList: true, subtree: true };

            const observer = new MutationObserver(async(mutations) => {
                let found = false
                mutations.forEach(async (mutation) => {
                    if (mutation.type == "childList") {
                        found = true;
                    }
                });
                if (found) {
                    observer.disconnect();
                    renderKatex();
                    await mermaid.run();
                    WaveDrom.ProcessAll();
                    updateCellStyle();
                    observer.observe(document.body, config);
                }
            });

            observer.observe(document.body, config);
        }

        renderKatex();
        WaveDrom.ProcessAll();
        updateCellStyle();
    });
})();