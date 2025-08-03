# GitBucket Markdown Enhanced Plugin

Powered by [flexmark-java](https://github.com/vsch/flexmark-java)

[TOC]

## The goal

[Visual Studio Code](https://code.visualstudio.com/) extension [Markdown Preview Enhanced](https://shd101wyy.github.io/markdown-preview-enhanced/#/)

- [x] [Basic Syntax](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=syntax-guide)
- [Extended syntax](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=extended-syntax)
  - [ ] [Table](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=table)
  - [x] [Emoji & Font-Awesome](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=emoji-amp-font-awesome)(Emoji only)
  - [x] [Superscript](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=superscript)(0.1.1)
  - [x] [Subscript](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=subscript)(0.1.1)
  - [x] [Footnotes](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=footnotes)
  - [x] [Abbreviation](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=abbreviation)
  - [ ] [Mark](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=mark)
  - [ ] [CriticMarkup](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=criticmarkup)
  - [ ] [Admonition](https://shd101wyy.github.io/markdown-preview-enhanced/#/markdown-basics?id=admonition)
- [x] [Math Typesetting](https://shd101wyy.github.io/markdown-preview-enhanced/#/math)(Katex only)
- [Diagrams](https://shd101wyy.github.io/markdown-preview-enhanced/#/diagrams)
  - [x] [Mermaid](https://shd101wyy.github.io/markdown-preview-enhanced/#/diagrams?id=mermaid)
  - [ ] [PlantUML](https://shd101wyy.github.io/markdown-preview-enhanced/#/diagrams?id=plantuml)
  - [ ] [WaveDrom](https://shd101wyy.github.io/markdown-preview-enhanced/#/diagrams?id=wavedrom)
  - [ ] [GraphViz](https://shd101wyy.github.io/markdown-preview-enhanced/#/diagrams?id=graphviz)
  - [ ] [Vega and Vega-lite](https://shd101wyy.github.io/markdown-preview-enhanced/#/diagrams?id=vega-and-vega-lite)
  - [ ] [Kroki](https://shd101wyy.github.io/markdown-preview-enhanced/#/diagrams?id=kroki)
- [x] [Table of Contents](https://shd101wyy.github.io/markdown-preview-enhanced/#/toc)(Sidebar not supported)
- [ ] [File Imports](https://shd101wyy.github.io/markdown-preview-enhanced/#/file-imports)
- [ ] [Code Chunk](https://shd101wyy.github.io/markdown-preview-enhanced/#/code-chunk)
- [ ] [Presentation](https://shd101wyy.github.io/markdown-preview-enhanced/#/presentation)
- [ ] [Pandoc](https://shd101wyy.github.io/markdown-preview-enhanced/#/pandoc)

## Extensions used

- [AbbreviationExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#abbreviation)
- [AdmonitionExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#admonition)
- [AnchorLinkExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#anchorlink)
- [EmojiExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#emoji) (Under investigation)
- [FootnoteExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#footnotes)
- [GitLabExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#gitlab-flavoured-markdown)
- [StrikethroughSubscriptExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#gfm-strikethroughsubscript)
- [SuperscriptExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#superscript)
- [TablesExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#tables)
- [TaskListExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#gfm-tasklist)
- [TocExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#table-of-contents)

### AbbreviationExtension

Allows to create abbreviations which will be replaced in plain text into <abbr></abbr> tags or optionally into <a></a> with titles for the abbreviation expansion.

```markdown
*[HTML]: Hyper Text Markup Language
*[W3C]: World Wide Web Consortium
The HTML specification
is maintained by the W3C.
```

to

*[HTML]: Hyper Text Markup Language
*[W3C]: World Wide Web Consortium
The HTML specification
is maintained by the W3C.

### AdmonitionExtension

To create block-styled side content. 

```markdown
!!! note This is the admonition title
    This is the admonition body
```

to

!!! note This is the admonition title
    This is the admonition body

### AnchorLinkExtension

Automatically adds anchor links to heading, using GitHub id generation algorithm.

### EmojiExtension

`:grinning:` -> :grinning:

### FootnoteExtension

Creates footnote references in the document.

```markdown
FootnoteExtension[^1] creates footnote references in the document.

[^1]: https://github.com/vsch/flexmark-java/wiki/Extensions#footnotes
```

FootnoteExtension[^1] creates footnote references in the document.

[^1]: https://github.com/vsch/flexmark-java/wiki/Extensions#footnotes

### GitLabExtension

Parses and renders [GitLab Flavoured Markdown](https://gitlab.com/gitlab-org/gitlab-ce/blob/master/doc/user/markdown.md).

#### Katex support

```markdown
a^2+b^2=c^2
```

to

```math
a^2+b^2=c^2
```

#### mermaid support

```markdown
graph TD;
  A-->B;
  A-->C;
  B-->D;
  C-->D;
```

to

```mermaid
graph TD;
  A-->B;
  A-->C;
  B-->D;
  C-->D;
```

### StrikethroughSubscriptExtension

`~~Strikethrough~~` -> ~~Strikethrough~~

`H~2~O` -> H~2~O

### SuperscriptExtension

`x^2^` -> x^2^

### TablesExtension

See [Tables Extension · vsch/flexmark-java Wiki](https://github.com/vsch/flexmark-java/wiki/Tables-Extension)

### TaskListExtension

```markdown
- [x] Katex support
- [x] mermaid support
- [ ] PlantUML support
```

to

- [x] Katex support
- [x] mermaid support
- [ ] PlantUML support

### TocExtension

`[TOC]` create table of contents.

[TOC]
