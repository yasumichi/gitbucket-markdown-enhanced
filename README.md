# GitBucket Markdown Enhanced Plugin

Powered by [flexmark-java](https://github.com/vsch/flexmark-java)

[TOC]

## The goal

[Visual Studio Code](https://code.visualstudio.com/) extension [Markdown Preview Enhanced](https://shd101wyy.github.io/markdown-preview-enhanced/#/)

## Extensions used

- [AnchorLinkExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#anchorlink)
- [EmojiExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#emoji) (Under investigation)
- [FootnoteExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#footnotes)
- [GitLabExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#gitlab-flavoured-markdown)
- [StrikethroughSubscriptExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#gfm-strikethroughsubscript)
- [TablesExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#tables)
- [TaskListExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#gfm-tasklist)
- [TocExtension](https://github.com/vsch/flexmark-java/wiki/Extensions#table-of-contents)

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
