package io.github.yasumichi.gme.critic

import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.util.ast.DelimitedNode

class CriticDeletions(var openingMarker: BasedSequence, var text: BasedSequence, var closingMarker: BasedSequence)
    extends Node(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()))
    with DelimitedNode {

  override def getOpeningMarker(): BasedSequence = openingMarker

  override def setOpeningMarker(openingMarker: BasedSequence): Unit = { this.openingMarker = openingMarker }

  override def getClosingMarker(): BasedSequence = closingMarker

  override def setClosingMarker(closingMarker: BasedSequence): Unit = { this.closingMarker = closingMarker }

  override def getText(): BasedSequence = text

  override def setText(text: BasedSequence): Unit = { this.text = text }

  override def getSegments(): Array[BasedSequence] = Array(openingMarker, text, closingMarker)
}
