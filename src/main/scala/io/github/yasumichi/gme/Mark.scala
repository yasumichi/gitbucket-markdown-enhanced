package io.github.yasumichi.gme

import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence

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
class Mark(val text: BasedSequence, val source: BasedSequence) extends Node {

  /**
     * Get the segments making up the node's characters.
     * 
     * Used to get segments after the some of the node's elements were modified
     *
     * @return array of segments
     */
  override def getSegments: Array[BasedSequence] = Array(source)
}
