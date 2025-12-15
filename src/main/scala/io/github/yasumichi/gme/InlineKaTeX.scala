package io.github.yasumichi.gme

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
class InlineKatex(val text: BasedSequence, val source: BasedSequence) extends Node {
  override def getSegments: Array[BasedSequence] = Array(source)
}
