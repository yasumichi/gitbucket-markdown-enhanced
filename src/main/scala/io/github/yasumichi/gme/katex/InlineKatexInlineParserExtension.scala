package io.github.yasumichi.gme.katex

import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.parser.{
  InlineParser,
  InlineParserExtension,
  InlineParserExtensionFactory,
  LightInlineParser
}
import java.util.regex.Pattern

import org.slf4j.LoggerFactory
import io.github.yasumichi.gme.katex.InlineKatex

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
    val patterns = List("""\\\[([^\\]+?)\\\]""", """\$\$([^$]+?)\$\$""", """\\\(([^\\]+?)\\\)""")
    val input = inlineParser.getInput()
    logger.debug("Input: " + input.toString())

    for (patternText <- patterns) {
      logger.debug(s"Trying pattern: $patternText")
      val matcher = inlineParser.matcher(Pattern.compile(patternText))
      if (matcher != null) {
        inlineParser.flushTextNode()

        logger.debug("matcher.group():" + matcher.group())
        val mathOpen = input.subSequence(matcher.start(), matcher.start(1))
        val mathClosed = input.subSequence(matcher.end(1), matcher.end())
        val inlineMath = new InlineKatex(
          mathOpen,
          mathOpen.baseSubSequence(mathOpen.getEndOffset(), mathClosed.getStartOffset()),
          mathClosed
        )
        inlineParser.getBlock().appendChild(inlineMath);
        return true
      }
    }
    val matcher = inlineParser.matcher(Pattern.compile("""\$([^\\]+?)\$"""))
    if (matcher != null) {
      inlineParser.flushTextNode()

      logger.debug("matcher.group():" + matcher.group())
      val mathOpen = input.subSequence(matcher.start())
      val mathClosed = input.subSequence(matcher.end())
      val inlineMath = new InlineKatex(mathOpen, input.subSequence(matcher.start(1), matcher.end(1)), mathClosed)
      inlineParser.getBlock().appendChild(inlineMath);
      return true
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
