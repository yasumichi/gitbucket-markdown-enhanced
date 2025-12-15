package io.github.yasumichi.gme

import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.parser.{
  InlineParser,
  InlineParserExtension,
  InlineParserExtensionFactory,
  LightInlineParser
}

import org.slf4j.LoggerFactory

/**
  * InlineKatexInlineParserExtension class
  * 
  * Inline parser extension that parses the inline KaTeX syntax.
  * 
  * It recognizes the following patterns:
  * - `\[...\]` 
  * - `$$...$$`
  * - `$...$`
  * - `\(...\)`
  */
class InlineKatexInlineParserExtension extends InlineParserExtension {
  private val logger = LoggerFactory.getLogger(classOf[InlineKatexInlineParserExtension])

  override def finalizeDocument(inlineParser: InlineParser): Unit = {}

  override def finalizeBlock(inlineParser: InlineParser): Unit = {}

  override def parse(inlineParser: LightInlineParser): Boolean = {
    val patterns = List("""\\\[(.+?)\\\]""", """\$\$(.+?)\$\$""", """\$(.+?)\$""", """\\\((.+?)\\\)""")
    logger.debug("Input: " + inlineParser.getInput().toString())

    for (patternText <- patterns) {
      logger.debug(s"Trying pattern: $patternText")
      val matches = inlineParser.matchWithGroups(java.util.regex.Pattern.compile(patternText))
      if (matches != null) {
        logger.debug(s"Matched pattern: $patternText with content: ${matches(1)}")
        inlineParser.flushTextNode()
        val katexText = matches(1)
        inlineParser.getBlock.appendChild(new InlineKatex(katexText, matches(0)))
        return true
      }
    }
    false
  }
}

object InlineKatexInlineParserExtension {
  class Factory() extends InlineParserExtensionFactory {
    override def getCharacters: CharSequence = "$\\"
    override def apply(inlineParser: LightInlineParser): InlineParserExtension = new InlineKatexInlineParserExtension()
    override def getAfterDependents: java.util.Set[Class[_]] = null
    override def getBeforeDependents: java.util.Set[Class[_]] = null
    override def affectsGlobalScope(): Boolean = false
  }
}
