package io.github.yasumichi.gme

import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.html.HtmlRendererOptions
import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.{NodeRenderer, NodeRendererContext, NodeRendererFactory, NodeRenderingHandler}
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util
import java.nio.charset.Charset

class MarkdownEnhancedNodeRenderer extends NodeRenderer {

  override def getNodeRenderingHandlers(): util.Set[NodeRenderingHandler[_ <: Object]] = {
    val set: util.HashSet[NodeRenderingHandler[_]] = new util.HashSet[NodeRenderingHandler[_]]
    set.add(new NodeRenderingHandler[FencedCodeBlock](classOf[FencedCodeBlock], this.render))
    set
  }

  private def render(node: FencedCodeBlock, context: NodeRendererContext, html: HtmlWriter): Unit = {
    val htmlOptions:HtmlRendererOptions  = context.getHtmlOptions()
    val language:BasedSequence  = node.getInfoDelimitedByAny(htmlOptions.languageDelimiterSet)

    if (language.equals("plantuml")) {
        var text = ""
        var seqs = node.getContentLines().toArray()
        for(i <- 0 to seqs.length - 1) text = text + seqs(i).toString + "\n"

      val reader = new SourceStringReader(text)
      val os = new ByteArrayOutputStream()
      reader.generateImage(os, new FileFormatOption(FileFormat.SVG))
      
      html.tag("div")
      html.append(new String(os.toByteArray(), Charset.forName("UTF-8")))
      html.tag("/div") 
      os.close()

    } else {
        context.delegateRender()
    }
  }
}

object MarkdownEnhancedNodeRenderer {
  class Factory() extends NodeRendererFactory {
    override def apply(options: DataHolder): NodeRenderer = new MarkdownEnhancedNodeRenderer()
  }
}