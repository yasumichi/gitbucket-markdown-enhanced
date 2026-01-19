(function () {
    console.log("gme.js loaded.");

    const updateCellStyle = function() {
        ['center', 'right'].forEach(function(align) {
            [ `td[align=${align}]`, `th[align=${align}]` ].forEach(function(selector) {
                document.querySelectorAll(selector).forEach(function(elem) {
                    elem.style.textAlign = align;
                });
            });
        });
    };

    const updateTocStyle = function() {
        const toc = document.querySelector('.toc');
        if (toc) {
            const parent = toc.parentElement;
            const details = document.createElement('details');
            const summary = document.createElement('summary')
            if (!document.location.toString().includes("/wiki/")) {
                toc.style.maxHeight = `${document.documentElement.clientHeight * 0.7}px`
                toc.style.overflowY = "scroll";
            }
            details.classList.add("toc-wrapper");
            details.open = true;
            summary.textContent = "Table of contents"
            summary.classList.add("toc-summary");
            parent.insertBefore(details, toc);
            details.appendChild(summary);
            details.appendChild(toc);
            var parentY = parent.getClientRects()[0].y;
            details.style.position = "fixed";
            if (document.location.toString().includes("/wiki/")) {
                details.style.right = "270px";
            } else {
                details.style.right = "20px";
            }
            if (parentY > 0) {
                details.style.top = `${Math.floor(parentY)+2}px`;
            } else {
                details.style.top = "0px";
            }
            document.addEventListener("scroll", (event) => {
                var parentY = parent.getClientRects()[0].y;
                if (parentY > 0) {
                    details.style.top = `${Math.floor(parentY)+2}px`;
                } else {
                    details.style.top = "0px";
                }
            });
        }
    }

    const renderKatex = function() {
        var mathElems = document.getElementsByClassName("katex");
        var elems = [];
        for (const i in mathElems) {
            if (mathElems.hasOwnProperty(i)) elems.push(mathElems[i]);
        }

        elems.forEach(elem => {
            katex.render(elem.textContent, elem, { throwOnError: false, displayMode: elem.nodeName !== 'SPAN', });
        });
    };

    const renderVega = function() {
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

    const addPresentationButton = function () {
        var readme = document.getElementById('readme'); 
        if (readme != null || document.location.toString().endsWith('.md')) {
            var btnGroup = document.querySelector('.box-header .btn-group');
            if (btnGroup == null) {
                const strong = document.querySelector(".box-header .strong");
                btnGroup = document.createElement("div");
                btnGroup.className = "btn-group pull-right";
                strong.appendChild(btnGroup);
            }
            var anchor = document.createElement("a");
            anchor.className = "btn btn-sm";
            anchor.target = "_blank";
            if (document.location.toString().includes("/blob/")) {
                anchor.href = document.location.toString().replace("/blob/", "/presentation/");
            } else if (!document.location.toString().includes("/tree/")) {
                var branchSelector = document.querySelector('.head .pull-left .strong');
                if (branchSelector != null) {
                    var branchId = branchSelector.textContent.trim();
                    anchor.href = document.location.toString() + `/presentation/${branchId}/README.md`;
                }
            }
            var icon = document.createElement("i");
            icon.className = "oction octicon-zap";
            anchor.appendChild(icon);
            btnGroup.appendChild(anchor);
        }
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
                        renderVega();
                        updateCellStyle();
                        updateTocStyle();
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
                    renderVega();
                    updateCellStyle();
                    observer.observe(document.body, config);
                }
            });

            observer.observe(document.body, config);
        }

        // No further processing is required in edit mode
        if (document.getElementById('editor') != null) {
            console.info("An editor has been detected, so processing will terminate.");
            return;
        }

        // for presentation
        addPresentationButton();

        // for common processing
        renderKatex();
        WaveDrom.ProcessAll();
        renderVega();
        updateCellStyle();
        if (!document.location.toString().includes("/issues/") && !document.location.toString().includes("/pull/")) {
            updateTocStyle();
        }
    });
})();