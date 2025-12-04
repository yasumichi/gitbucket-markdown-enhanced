package io.github.yasumichi.gme

import com.vladsch.flexmark.parser.{InlineParser, InlineParserExtension, InlineParserExtensionFactory, LightInlineParser}
import java.util
import java.util.regex.Pattern

class MarkInlineParserExtension() extends InlineParserExtension {
  override def finalizeDocument(inlineParser: InlineParser): Unit = {}
  override def finalizeBlock(inlineParser: InlineParser): Unit = {}
  override def parse(inlineParser: LightInlineParser): Boolean = {
    val patternText = """==(.+?)=="""
    val matches = inlineParser.matchWithGroups(Pattern.compile(patternText))
    if (matches != null) {
      inlineParser.flushTextNode()
      val markText = matches(1)
      inlineParser.getBlock.appendChild(new Mark(markText, matches(0)))
      return true
    }
    false
  }
}
object MarkInlineParserExtension {
  class Factory() extends InlineParserExtensionFactory {
    override def getCharacters: CharSequence = "="
    override def apply(inlineParser: LightInlineParser): InlineParserExtension = new MarkInlineParserExtension()
    override def getAfterDependents: util.Set[Class[_]] = null
    override def getBeforeDependents: util.Set[Class[_]] = null
    override def affectsGlobalScope(): Boolean = false
  }
}