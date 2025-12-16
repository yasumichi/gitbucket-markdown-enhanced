package io.github.yasumichi.gme

import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.sequence.BasedSequence
import com.vladsch.flexmark.util.ast.DoNotDecorate

/**
  * InlineUri class
  * 
  * AST node that holds the inline URI syntax of flexmark-java.
  * 
  * `http://example.com` is represented as InlineUri node.
  * 
  * An instance is created by the InlineUriInlineParserExtension class.
  * 
  * The conversion to HTML is handled by the MarkdownEnhancedNodeRenderer class.
  *
  * @param text
  * @param source
  */
class InlineUri(val openingMarker: BasedSequence, val text: BasedSequence)
    extends Node(Node.spanningChars(openingMarker, text))
    with DoNotDecorate {
  override def getSegments: Array[BasedSequence] = Array(openingMarker, text)
}
