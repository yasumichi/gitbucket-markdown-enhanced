package io.github.yasumichi.gme

import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.html.HtmlRendererOptions
import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.{
  NodeRenderer,
  NodeRendererContext,
  NodeRendererFactory,
  NodeRenderingHandler
}
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util
import java.nio.charset.Charset
import com.vladsch.flexmark.ext.wikilink.WikiLink

class MarkdownEnhancedNodeRenderer extends NodeRenderer {

  override def getNodeRenderingHandlers()
      : util.Set[NodeRenderingHandler[_ <: Object]] = {
    val set: util.HashSet[NodeRenderingHandler[_]] =
      new util.HashSet[NodeRenderingHandler[_]]
    set.add(
      new NodeRenderingHandler[FencedCodeBlock](
        classOf[FencedCodeBlock],
        this.render
      )
    )
    set.add(new NodeRenderingHandler[WikiLink](
      classOf[WikiLink],
      this.renderWikiLink
    ))
    set.add(
      new NodeRenderingHandler[Mark](
        classOf[Mark],
        this.renderMark
      )
    )
    set
  }

  private def render(
      node: FencedCodeBlock,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    val htmlOptions: HtmlRendererOptions = context.getHtmlOptions()
    val language: BasedSequence =
      node.getInfoDelimitedByAny(htmlOptions.languageDelimiterSet)

    if (language.equals("plantuml")) {
      var text = ""
      var seqs = node.getContentLines().toArray()
      for (i <- 0 to seqs.length - 1) text = text + seqs(i).toString + "\n"

      val reader = new SourceStringReader(text)
      val os = new ByteArrayOutputStream()
      reader.generateImage(os, new FileFormatOption(FileFormat.SVG))

      var svg = new String(os.toByteArray(), Charset.forName("UTF-8"))
      var re = "^<\\?xml [^<>]+?\\>".r
      svg = re.replaceFirstIn(svg, "")

      html.tag("div")
      html.append(svg)
      html.tag("/div")
      os.close()

    } else {
      context.delegateRender()
    }
  }

  private def renderWikiLink(
      node: WikiLink,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    val link = node.getLink()
    val resolvedLink = context.resolveLink(
      com.vladsch.flexmark.html.renderer.LinkType.LINK,
      link,
      null
    )
    html
      .withAttr()
      .attr("href", resolvedLink.getUrl())
      .tag("a")
    html.text(node.getLink())
    html.tag("/a")
  }

  private def renderMark(
      node: Mark,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    html.tag("mark")
    html.text(node.text.toString())
    html.tag("/mark")
  }
}

object MarkdownEnhancedNodeRenderer {
  class Factory() extends NodeRendererFactory {
    override def apply(options: DataHolder): NodeRenderer =
      new MarkdownEnhancedNodeRenderer()
  }
}
