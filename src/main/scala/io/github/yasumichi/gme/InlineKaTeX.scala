package io.github.yasumichi.gme

import com.vladsch.flexmark.util.ast.DelimitedNode;
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence

/**
  * InlineKatex class
  *
  * AST node that holds the inline KaTeX syntax of flexmark-java.
  * 
  * `$...$` is represented as InlineKatex node.
  * An instance is created by the InlineKatexInlineParserExtension class.
  *
  * @param text
  * @param source
  */
class InlineKatex(var openingMarker: BasedSequence, var text: BasedSequence, var closingMarker: BasedSequence)
    extends Node(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()))
    with DelimitedNode {

  override def getOpeningMarker(): BasedSequence = openingMarker

  override def setOpeningMarker(openingMarker: BasedSequence): Unit = { this.openingMarker = openingMarker }

  override def getText(): BasedSequence = text

  override def setText(text: BasedSequence): Unit = { this.text = text }

  override def getClosingMarker(): BasedSequence = closingMarker

  override def setClosingMarker(closingMarker: BasedSequence): Unit = { this.closingMarker = closingMarker }

  override def getSegments: Array[BasedSequence] = Array(openingMarker, text, closingMarker)
}
