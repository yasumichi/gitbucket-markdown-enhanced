package io.github.yasumichi.gme

import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence

class Mark(val text: BasedSequence, val source: BasedSequence) extends Node {
  override def getSegments: Array[BasedSequence] = Array(source)
}
