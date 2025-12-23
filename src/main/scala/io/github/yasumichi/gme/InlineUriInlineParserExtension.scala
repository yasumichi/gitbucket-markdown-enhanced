package io.github.yasumichi.gme

import com.vladsch.flexmark.ast.Text
import com.vladsch.flexmark.parser.{
  InlineParser,
  InlineParserExtension,
  InlineParserExtensionFactory,
  LightInlineParser
}
import com.vladsch.flexmark.util.ast.ContentNode
import com.vladsch.flexmark.util.sequence.BasedSequence

import java.util
import java.util.regex.Pattern

/**
  * InlineUriInlineParserExtension class
  *
  * Inline parser extension that parses inline URIs like `http://example.com` and creates InlineUri nodes.
  * 
  * If you write `http://example.com`, it will be converted to an InlineUri node.
  * 
  * The conversion to HTML is handled by the MarkdownEnhancedNodeRenderer class.
  */
class InlineUriInlineParserExtension() extends InlineParserExtension {
  override def finalizeDocument(inlineParser: InlineParser): Unit = {}
  override def finalizeBlock(inlineParser: InlineParser): Unit = {}
  override def parse(inlineParser: LightInlineParser): Boolean = {
    val input = inlineParser.getInput
    val index = inlineParser.getIndex

    // Define a regex pattern to match inline URIs
    val pattern = """(^|[^\("\{])(http)(s?:\/\/[\w\/:%#\$&\?\(\)~\.=\+\-]+)"""
    val matches = inlineParser.matchWithGroups(Pattern.compile(pattern))
    if (matches != null) {
      if ((index > 0 && input.charAt(index - 1) == '{') || (index > 0 && input.charAt(index - 1) == '(') || (index > 0 && input.charAt(index - 1) == '"')) {
        inlineParser.appendNode(new Text(matches(0)))
        return false
      }
      inlineParser.flushTextNode()
      val openingMarker = matches(2)
      val text = matches(3)
      inlineParser.getBlock.appendChild(new InlineUri(openingMarker, text))
      return true
    }
    false
  }
}

/**
  * Companion object for InlineUriInlineParserExtension
  * 
  * Factory class to create instances of InlineUriInlineParserExtension.
  * 
  * This factory is used to register the extension with the flexmark parser.
  * 
  * @return InlineUriInlineParserExtension.Factory
  */
object InlineUriInlineParserExtension {
  class Factory() extends InlineParserExtensionFactory {
    override def getCharacters: CharSequence = "h"
    override def apply(inlineParser: LightInlineParser): InlineParserExtension = new InlineUriInlineParserExtension()
    override def getAfterDependents: util.Set[Class[_]] = null
    override def getBeforeDependents: util.Set[Class[_]] = null
    override def affectsGlobalScope(): Boolean = false
  }
}
