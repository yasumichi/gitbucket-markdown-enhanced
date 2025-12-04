package io.github.yasumichi.gme

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.Parser.ParserExtension
import com.vladsch.flexmark.util.data.MutableDataHolder

class MarkdownEnhancedExtention extends ParserExtension with HtmlRendererExtension {

    override def extend(parserBuilder: Parser.Builder): Unit = {
      parserBuilder.customInlineParserExtensionFactory(new MarkInlineParserExtension.Factory())
    }

    override def parserOptions(options: MutableDataHolder): Unit = {}

    override def rendererOptions(options: MutableDataHolder): Unit = {}

    override def extend(htmlRendererBuilder: HtmlRenderer.Builder, rendererType: String): Unit = {
        htmlRendererBuilder.nodeRendererFactory((new MarkdownEnhancedNodeRenderer.Factory()))
        htmlRendererBuilder.linkResolverFactory((new MarkdownEnhancedLinkResolver.Factory()))
    }
}

object MarkdownEnhancedExtention {
  def create(): MarkdownEnhancedExtention = new MarkdownEnhancedExtention()
}