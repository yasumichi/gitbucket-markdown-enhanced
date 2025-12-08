package io.github.yasumichi.gme

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.html.HtmlRenderer.HtmlRendererExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.Parser.ParserExtension
import com.vladsch.flexmark.util.data.MutableDataHolder

/**
  * Markdown Enhanced Extension for flexmark-java
  */
class MarkdownEnhancedExtention extends ParserExtension with HtmlRendererExtension {

  /**
  * Extend the parser with custom inline parser extension
  *
  * @param parserBuilder The parser builder to extend
  */
  override def extend(parserBuilder: Parser.Builder): Unit = {
    parserBuilder.customInlineParserExtensionFactory(new MarkInlineParserExtension.Factory())
    parserBuilder.customInlineParserExtensionFactory(new InlineUriInlineParserExtension.Factory())
  }

  override def parserOptions(options: MutableDataHolder): Unit = {}

  override def rendererOptions(options: MutableDataHolder): Unit = {}

  /**
  * Extend the HTML renderer with custom node renderer and link resolver
  *
  * @param htmlRendererBuilder The HTML renderer builder to extend
  * @param rendererType The type of renderer being built (usually "HTML")
  */
  override def extend(htmlRendererBuilder: HtmlRenderer.Builder, rendererType: String): Unit = {
    htmlRendererBuilder.nodeRendererFactory((new MarkdownEnhancedNodeRenderer.Factory()))
    htmlRendererBuilder.linkResolverFactory((new MarkdownEnhancedLinkResolver.Factory()))
  }
}

/**
  * Companion object for MarkdownEnhancedExtention
  * Provides a factory method to create an instance of the extension
  * @return A new instance of MarkdownEnhancedExtention
  */
object MarkdownEnhancedExtention {
  def create(): MarkdownEnhancedExtention = new MarkdownEnhancedExtention()
}
