package io.github.yasumichi.gme.critic

import com.vladsch.flexmark.parser.InlineParser
import com.vladsch.flexmark.parser.InlineParserExtension
import com.vladsch.flexmark.parser.LightInlineParser
import com.vladsch.flexmark.parser.InlineParserFactory
import com.vladsch.flexmark.parser.InlineParserExtensionFactory
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor
import com.vladsch.flexmark.parser.internal.LinkRefProcessorData
import com.vladsch.flexmark.util.data.DataHolder
import java.util
import java.util.BitSet
import java.util.regex.Pattern
import com.vladsch.flexmark.util.sequence.BasedSequence

class CriticMarkupParserExtension extends InlineParserExtension {

  override def finalizeDocument(inlineParser: InlineParser): Unit = {}

  override def finalizeBlock(inlineParser: InlineParser): Unit = {}

  override def parse(inlineParser: LightInlineParser): Boolean = {
    val input = inlineParser.getInput()
    val patternList =
      List("""\{\+\+([^\+\}]*)\+\+}""", """\{>>([^>\}]*)<<\}""", """\{--([^-\}]*)--\}""", """\{==([^=}]*)==\}""")
    val factoryList = List(
      (openingMarker: BasedSequence, text: BasedSequence, closingMarker: BasedSequence) =>
        new CriticAdditions(openingMarker, text, closingMarker),
      (openingMarker: BasedSequence, text: BasedSequence, closingMarker: BasedSequence) =>
        new CriticComments(openingMarker, text, closingMarker),
      (openingMarker: BasedSequence, text: BasedSequence, closingMarker: BasedSequence) =>
        new CriticDeletions(openingMarker, text, closingMarker),
      (openingMarker: BasedSequence, text: BasedSequence, closingMarker: BasedSequence) =>
        new CriticHighlighting(openingMarker, text, closingMarker)
    )
    for ((pattern, index) <- patternList.zipWithIndex) {
      val matcher = inlineParser.matcher(Pattern.compile(pattern))
      if (matcher != null) {
        inlineParser.flushTextNode()
        val criticOpen = input.subSequence(matcher.start(), matcher.start(1))
        val criticClosed = input.subSequence(matcher.end(1), matcher.end())
        val criticMarkup = factoryList(index)(
          criticOpen,
          criticOpen.baseSubSequence(criticOpen.getEndOffset(), criticClosed.getStartOffset()),
          criticClosed
        )
        inlineParser.getBlock().appendChild(criticMarkup)
        return true
      }
    }
    val substitutionsMatcher = inlineParser.matchWithGroups(Pattern.compile("""(\{~~)([^~>}]*)~>([^~>\}]*)(~~\})"""))
    if (substitutionsMatcher != null) {
      inlineParser.flushTextNode()
      val criticOpen = substitutionsMatcher(1)
      val criticClosed = substitutionsMatcher(4)
      val criticMarkup =
        new CriticSubstitutions(criticOpen, substitutionsMatcher(2), substitutionsMatcher(3), criticClosed)
      inlineParser.getBlock().appendChild(criticMarkup)
      return true
    }
    false
  }
}

object CriticMarkupParserExtension {
  class Factory() extends InlineParserExtensionFactory {

    override def getAfterDependents(): util.Set[Class[_ <: Object]] = null

    override def getBeforeDependents(): util.Set[Class[_ <: Object]] = null

    override def affectsGlobalScope(): Boolean = false

    override def getCharacters(): CharSequence = "{"

    override def apply(inlineParser: LightInlineParser): InlineParserExtension = new CriticMarkupParserExtension()
  }
}
