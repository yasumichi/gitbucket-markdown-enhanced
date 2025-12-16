package io.github.yasumichi.gme

import com.vladsch.flexmark.parser.{
  InlineParser,
  InlineParserExtension,
  InlineParserExtensionFactory,
  LightInlineParser
}
import java.util
import java.util.regex.Pattern

/**
  * MarkInlineParserExtension class
  *
  * Inline parser extension that parses the `==Marked Text==` syntax and creates Mark nodes.
  * 
  * If you write `==Marked Text==`, it will be converted to `&lt;mark&gt;Marked Text&lt;/mark&gt;
  * 
  * The conversion to HTML is handled by the MarkdownEnhancedNodeRenderer class.
  */
class MarkInlineParserExtension() extends InlineParserExtension {
  override def finalizeDocument(inlineParser: InlineParser): Unit = {}
  override def finalizeBlock(inlineParser: InlineParser): Unit = {}

  /**
    * parse method
    * 
    * Parses the inline content to find the `==Marked Text==` syntax.
    *
    * @param inlineParser the inline parser
    * @return true if the syntax is found and a Mark node is created, false otherwise
    */
  override def parse(inlineParser: LightInlineParser): Boolean = {
    val input = inlineParser.getInput()
    val patternText = """==(.+?)=="""
    val matcher = inlineParser.matcher(Pattern.compile(patternText))
    if (matcher != null) {
      inlineParser.flushTextNode()

      val markOpen = input.subSequence(matcher.start(), matcher.start(1))
      val markClosed = input.subSequence(matcher.end(1), matcher.end())
      val inlineMath = new Mark(markOpen, markOpen.baseSubSequence(markOpen.getEndOffset(), markClosed.getStartOffset()), markClosed)
      inlineParser.getBlock().appendChild(inlineMath);
      return true
    }
    false
  }
}

/**
  * Companion object for MarkInlineParserExtension
  * 
  * Factory class to create instances of MarkInlineParserExtension.
  */
object MarkInlineParserExtension {
  class Factory() extends InlineParserExtensionFactory {
    override def getCharacters: CharSequence = "="
    override def apply(inlineParser: LightInlineParser): InlineParserExtension = new MarkInlineParserExtension()
    override def getAfterDependents: util.Set[Class[_]] = null
    override def getBeforeDependents: util.Set[Class[_]] = null
    override def affectsGlobalScope(): Boolean = false
  }
}
