(function () {
    console.log("gme.js loaded.")

    document.addEventListener("DOMContentLoaded", function () {
        console.log("DOMContentLoaded fire")
        var mathElems = document.getElementsByClassName("katex");
        var elems = [];
        for (const i in mathElems) {
            if (mathElems.hasOwnProperty(i)) elems.push(mathElems[i]);
        }

        elems.forEach(elem => {
            katex.render(elem.textContent, elem, { throwOnError: false, displayMode: elem.nodeName !== 'SPAN', });
        });
    });
})();