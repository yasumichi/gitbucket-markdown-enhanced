package io.github.yasumichi.gme.mark

import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.util.ast.DelimitedNode

/**
  * Mark class
  * 
  * AST node that holds the inline syntax of flexmark-java.
  * 
  * `==Marked Text==` is represented as Mark node.
  * 
  * If you write `==Marked Text==`, it will be converted to `&lt;mark&gt;Marked Text&lt;/mark&gt;`.
  * 
  * An instance is created by the MarkInlineParserExtension class.
  * 
  * The conversion to HTML is handled by the MarkdownEnhancedNodeRenderer class.
  *
  * @param text node text content(ex. `Marked Text`)
  * @param source source of node(ex. `==Marked Text==`)
  */
class Mark(var openingMarker: BasedSequence, var text: BasedSequence, var closingMarker: BasedSequence)
    extends Node(openingMarker.baseSubSequence(openingMarker.getStartOffset(), closingMarker.getEndOffset()))
    with DelimitedNode {

  override def getOpeningMarker(): BasedSequence = openingMarker

  override def setOpeningMarker(openingMarker: BasedSequence): Unit = { this.openingMarker = openingMarker }

  override def getText(): BasedSequence = text

  override def setText(text: BasedSequence): Unit = { this.text = text }

  override def getClosingMarker(): BasedSequence = closingMarker

  override def setClosingMarker(closingMarker: BasedSequence): Unit = { this.closingMarker = closingMarker }

  /**
     * Get the segments making up the node's characters.
     * 
     * Used to get segments after the some of the node's elements were modified
     *
     * @return array of segments
     */
  override def getSegments: Array[BasedSequence] = Array(openingMarker, text, closingMarker)
}
