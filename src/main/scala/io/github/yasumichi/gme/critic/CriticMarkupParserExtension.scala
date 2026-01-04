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

class CriticMarkupParserExtension extends InlineParserExtension {

  override def finalizeDocument(inlineParser: InlineParser): Unit = {}

  override def finalizeBlock(inlineParser: InlineParser): Unit = {}

  override def parse(inlineParser: LightInlineParser): Boolean = {
    val input = inlineParser.getInput()
    val additionMatcher = inlineParser.matcher(Pattern.compile("""\{\+\+([^\+\}]*)\+\+}"""))
    if (additionMatcher != null) {
        inlineParser.flushTextNode()
        val criticOpen = input.subSequence(additionMatcher.start(), additionMatcher.start(1))
        val criticClosed = input.subSequence(additionMatcher.end(1), additionMatcher.end())
        val criticMarkup = new CriticAdditions(criticOpen, criticOpen.baseSubSequence(criticOpen.getEndOffset(), criticClosed.getStartOffset()), criticClosed)
        inlineParser.getBlock().appendChild(criticMarkup)
        return true
    }
    val commentsMatcher = inlineParser.matcher(Pattern.compile("""\{>>([^>\}]*)<<\}"""))
    if (commentsMatcher != null) {
        inlineParser.flushTextNode()
        val criticOpen = input.subSequence(commentsMatcher.start(), commentsMatcher.start(1))
        val criticClosed = input.subSequence(commentsMatcher.end(1), commentsMatcher.end())
        val criticMarkup = new CriticComments(criticOpen, criticOpen.baseSubSequence(criticOpen.getEndOffset(), criticClosed.getStartOffset()), criticClosed)
        inlineParser.getBlock().appendChild(criticMarkup)
        return true
    }
    val deletionsMatcher = inlineParser.matcher(Pattern.compile("""\{--([^-\}]*)--\}"""))
    if (deletionsMatcher != null) {
        inlineParser.flushTextNode()
        val criticOpen = input.subSequence(deletionsMatcher.start(), deletionsMatcher.start(1))
        val criticClosed = input.subSequence(deletionsMatcher.end(1), deletionsMatcher.end())
        val criticMarkup = new CriticDeletions(criticOpen, criticOpen.baseSubSequence(criticOpen.getEndOffset(), criticClosed.getStartOffset()), criticClosed)
        inlineParser.getBlock().appendChild(criticMarkup)
        return true
    }
    val highlightingMatcher = inlineParser.matcher(Pattern.compile("""\{==([^=}]*)==\}"""))
    if (highlightingMatcher != null) {
        inlineParser.flushTextNode()
        val criticOpen = input.subSequence(highlightingMatcher.start(), highlightingMatcher.start(1))
        val criticClosed = input.subSequence(highlightingMatcher.end(1), highlightingMatcher.end())
        val criticMarkup = new CriticHighlighting(criticOpen, criticOpen.baseSubSequence(criticOpen.getEndOffset(), criticClosed.getStartOffset()), criticClosed)
        inlineParser.getBlock().appendChild(criticMarkup)
        return true
    }
    val substitutionsMatcher = inlineParser.matchWithGroups(Pattern.compile("""(\{~~)([^~>}]*)~>([^~>\}]*)(~~\})"""))
    if (substitutionsMatcher != null) {
        inlineParser.flushTextNode()
        val criticOpen = substitutionsMatcher(1)
        val criticClosed = substitutionsMatcher(4)
        val criticMarkup = new CriticSubstitutions(criticOpen, substitutionsMatcher(2), substitutionsMatcher(3), criticClosed)
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