package io.github.yasumichi.gme.critic

import com.vladsch.flexmark.html.renderer.NodeRenderer
import com.vladsch.flexmark.html.renderer.NodeRenderingHandler
import java.util
import com.vladsch.flexmark.html.renderer.NodeRendererContext
import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.NodeRendererFactory
import com.vladsch.flexmark.util.data.DataHolder

class CriticMarkupRenderer extends NodeRenderer {

  override def getNodeRenderingHandlers(): util.Set[NodeRenderingHandler[_ <: Object]] = {
    val set: util.HashSet[NodeRenderingHandler[_]] = new util.HashSet[NodeRenderingHandler[_]]
    set.add(new NodeRenderingHandler[CriticAdditions](classOf[CriticAdditions], this.renderCriticAdditions))
    set.add(new NodeRenderingHandler[CriticComments](classOf[CriticComments], this.renderCriticComments))
    set.add(new NodeRenderingHandler[CriticDeletions](classOf[CriticDeletions], this.renderCriticDeletions))
    set.add(new NodeRenderingHandler[CriticHighlighting](classOf[CriticHighlighting], this.renderCriticHighlighting))
    set.add(new NodeRenderingHandler[CriticSubstitutions](classOf[CriticSubstitutions], this.renderCriticSubstitutions))
    set
  }

  private def renderCriticAdditions(node: CriticAdditions, context: NodeRendererContext, html: HtmlWriter): Unit = {
    html.tag("ins")
    html.append(node.getText())
    html.tag("/ins")
  }

  private def renderCriticComments(node: CriticComments, context: NodeRendererContext, html: HtmlWriter): Unit = {
    html.withAttr().attr("class", "critic comment").tag("span")
    html.append(s"${node.openingMarker}${node.getText().toString()}${node.closingMarker}")
    html.tag("/span")
  }

  private def renderCriticDeletions(node: CriticDeletions, context: NodeRendererContext, html: HtmlWriter): Unit = {
    html.tag("del")
    html.append(node.getText())
    html.tag("/del")
  }

  private def renderCriticHighlighting(
      node: CriticHighlighting,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    html.tag("mark")
    html.append(node.getText())
    html.tag("/mark")
  }

  private def renderCriticSubstitutions(
      node: CriticSubstitutions,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    html.tag("del")
    html.append(node.getText())
    html.tag("/del")
    html.tag("ins")
    html.append(node.getPostText())
    html.tag("/ins")
  }
}

object CriticMarkupRenderer {
  class Factory() extends NodeRendererFactory {
    override def apply(options: DataHolder): NodeRenderer = new CriticMarkupRenderer()
  }
}
