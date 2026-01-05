package io.github.yasumichi.gme.critic

import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.util.ast.DelimitedNode

class CriticSubstitutions(
    var openingMarker: BasedSequence,
    var preText: BasedSequence,
    var postText: BasedSequence,
    var closingMarker: BasedSequence
) extends Node(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()))
    with DelimitedNode {

  override def getOpeningMarker(): BasedSequence = openingMarker

  override def setOpeningMarker(openingMarker: BasedSequence): Unit = { this.openingMarker = openingMarker }

  override def getClosingMarker(): BasedSequence = closingMarker

  override def setClosingMarker(closingMarker: BasedSequence): Unit = { this.closingMarker = closingMarker }

  override def getText(): BasedSequence = preText

  override def setText(text: BasedSequence): Unit = { this.preText = text }

  override def getSegments(): Array[BasedSequence] = Array(openingMarker, preText, postText, closingMarker)

  def getPostText(): BasedSequence = postText

  def setPostText(postText: BasedSequence): Unit = { this.postText = postText }
}
