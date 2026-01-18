package io.github.yasumichi.gme

import com.vladsch.flexmark.ast.FencedCodeBlock
import com.vladsch.flexmark.html.HtmlRendererOptions
import com.vladsch.flexmark.html.HtmlWriter
import com.vladsch.flexmark.html.renderer.{NodeRenderer, NodeRendererContext, NodeRendererFactory, NodeRenderingHandler}
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.sequence.BasedSequence

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util
import java.nio.charset.Charset
import com.vladsch.flexmark.ext.wikilink.WikiLink

import org.slf4j.LoggerFactory
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.sequence.Escaping
import io.github.yasumichi.gme.katex.InlineKatex
import io.github.yasumichi.gme.mark.Mark
import io.github.yasumichi.gme.uri.InlineUri
import scala.util.matching.Regex

// for kroki support
import io.github.yasumichi.gme.service.PluginSettingsService
import io.github.yasumichi.gme.service.PluginSettingsService.KrokiParams
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.entity.ContentType
import org.apache.http.util.EntityUtils

/**
  * Enhanced Node Renderer for Markdown processing.
  *
  * This renderer adds support for:
  *  - PlantUML code blocks: Renders PlantUML diagrams from fenced code blocks labeled "plantuml".
  *  - WikiLinks: Renders wiki-style links.
  *  - Marked text: Renders text wrapped in <mark> tags.
  */
class MarkdownEnhancedNodeRenderer extends NodeRenderer with PluginSettingsService {
  private val logger = LoggerFactory.getLogger(classOf[MarkdownEnhancedNodeRenderer])
  private var vegaId: Int = 1;
  private val krokiUrl = loadSettings().krokiUrl;

  /**
  * Gets the set of node rendering handlers for custom nodes.
  *
  * @return A set of NodeRenderingHandler instances for custom nodes.
  */
  override def getNodeRenderingHandlers(): util.Set[NodeRenderingHandler[_ <: Object]] = {
    val set: util.HashSet[NodeRenderingHandler[_]] =
      new util.HashSet[NodeRenderingHandler[_]]
    set.add(
      new NodeRenderingHandler[FencedCodeBlock](
        classOf[FencedCodeBlock],
        this.renderFencedCodeBlock
      )
    )
    set.add(
      new NodeRenderingHandler[WikiLink](
        classOf[WikiLink],
        this.renderWikiLink
      )
    )
    set.add(
      new NodeRenderingHandler[Mark](
        classOf[Mark],
        this.renderMark
      )
    )
    set.add(
      new NodeRenderingHandler[InlineUri](
        classOf[InlineUri],
        this.renderInlineUri
      )
    )
    set.add(
      new NodeRenderingHandler[InlineKatex](
        classOf[InlineKatex],
        this.renderInlineKatex
      )
    )
    set
  }

  /**
  * Renders a fenced code block, specifically handling PlantUML code blocks.
  *
  * @param node
  * @param context
  * @param html
  */
  private def renderFencedCodeBlock(
      node: FencedCodeBlock,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    val htmlOptions: HtmlRendererOptions = context.getHtmlOptions()
    val language: BasedSequence =
      node.getInfoDelimitedByAny(htmlOptions.languageDelimiterSet)
    val info: String = node.getInfo().toString()

    logger.debug("FencedCodeBlock getInfo: " + node.getInfo().toString())

    // kroki code block
    val krokitrue = """.*\{.*kroki\s*=\s*true.*\}""".r
    val krokidia = """\{.*kroki\s*=\s*"(.*)".*\}""".r
    if (krokitrue.matches(info)) {
      return renderKroki(html, node, language.toString(), "svg")
    }
    krokidia.findFirstMatchIn(info) match {
      case Some(value) => return renderKroki(html, node, value.group(1), "svg")
      case None => None
    }

    // other code block
    if (language.equals("plantuml") || language.equals("puml")) {
      renderPlantUML(html, node)
    } else if (language.equals("wavedrom")) {
      renderWaveDrom(html, node)
    } else if (language.equals("dot") || language.equals("viz")) {
      renderDot(html, node, info)
    } else if (language.equals("math") || language.equals("mermaid")) {
      context.delegateRender()
    } else if (language.equals("vega") || language.equals("vega-lite")) {
      renderVega(html, node, context, language.toString())
    } else {
      renderPrittyPrint(html, node, context, info)
    }
  }

  /**
  * Renders a PlantUML diagram from a fenced code block.
  *
  * @param html HtmlWriter to write the output.
  * @param node FencedCodeBlock containing the PlantUML code.
  * @throws IOException if an I/O error occurs during rendering.
  */
  private def renderPlantUML(html: HtmlWriter, node: FencedCodeBlock): Unit = {
    var text = ""
    var seqs = node.getContentLines().toArray()
    for (i <- 0 to seqs.length - 1) text = text + seqs(i).toString + "\n"

    renderSVG(html, text)
  }

  /**
    * Renders a Graphviz dot diagram from a fenced code block.
    *
    * @param html HtmlWriter to write the output.
    * @param node FencedCodeBlock containing the dot code.
    */
  private def renderDot(html: HtmlWriter, node: FencedCodeBlock, info: String): Unit = {
    var text = "@startdot\n"
    var seqs = node.getContentLines().toArray()
    for (i <- 0 to seqs.length - 1) text = text + seqs(i).toString + "\n"
    text = text + "@enddot\n"

    val patternText = """\{\s*engine=\"(.*)\"\s*\}""".r
    val matches = patternText.findFirstMatchIn(info)
    var engine = ""
    matches match {
      case Some(m) =>
        engine = m.group(1)
      case None =>
        engine = "dot"
    }

    logger.debug("Graphviz engine: " + engine)

    if (!engine.equals("dot")) {
      text = text.replace(
        "{",
        s"""{
       |  graph [layout="${engine}"]; 
       |""".stripMargin
      )
    }

    renderSVG(html, text)
  }

  /**
    * Renders SVG from the given PlantUML or dot text.
    *
    * @param html HtmlWriter to write the output.
    * @param text The PlantUML or dot text to render.
    */
  private def renderSVG(html: HtmlWriter, text: String): Unit = {
    val reader = new SourceStringReader(text)
    val os = new ByteArrayOutputStream()
    reader.outputImage(os, new FileFormatOption(FileFormat.SVG))
    os.close()

    var svg = new String(os.toByteArray(), Charset.forName("UTF-8"))
    var re = "^<\\?xml [^<>]+?\\>".r
    svg = re.replaceFirstIn(svg, "")

    html.tag("div")
    html.append(svg)
    html.tag("/div")
  }

  /**
    * Renders WaveDrom code blocks. 
    * If you write a fenced code block with "wavedrom" as the language, it will be wrapped in a <script type="WaveDrom"> tag.
    *
    * @param html HtmlWriter to write the output.
    * @param node FencedCodeBlock containing the WaveDrom code.
    */
  private def renderWaveDrom(html: HtmlWriter, node: FencedCodeBlock): Unit = {
    var text = ""
    var seqs = node.getContentLines().toArray()
    for (i <- 0 to seqs.length - 1) text = text + seqs(i).toString()

    logger.debug(text)
    html
      .withAttr()
      .attr("type", "WaveDrom")
      .tag("script")
    html.append(text)
    html.tag("/script")
  }

  /**
    * Renders vega and vega-lite blocks
    *
    * @param html
    * @param node
    * @param context
    * @param language
    */
  private def renderVega(
      html: HtmlWriter,
      node: FencedCodeBlock,
      context: NodeRendererContext,
      language: String
  ): Unit = {
    html.withAttr().attr("id", s"vega-${vegaId}").tag("div")
    html.tag("/div")
    html.withAttr().attr("type", language).attr("class", "vega").attr("data-target", s"#vega-${vegaId}").tag("script")
    vegaId = vegaId + 1;
    html.rawIndentedPre(node.getContentChars())
    html.tag("/script")
  }
 
  /**
    * Renders kroki diagrams
    *
    * @param html HtmlWriter to write output
    * @param diagram_source diagram source
    * @param diagram_type diagram type
    * @param output_format diagram format
    */
  private def renderKroki(html: HtmlWriter, node: FencedCodeBlock,  diagram_type: String, output_format: String): Unit = {
    implicit val formats = org.json4s.DefaultFormats
    var diagram_source: String = ""
    var seqs = node.getContentLines().toArray()
    for (i <- 0 to seqs.length - 1) diagram_source = diagram_source + seqs(i).toString()

    logger.debug(krokiUrl)

    try {
      val httpclient: CloseableHttpClient= HttpClients.createDefault();
      val httpPost = new HttpPost(krokiUrl)
      httpPost.addHeader("Content-Type", "application/json")
      val json = org.json4s.jackson.Serialization.write(KrokiParams(diagram_source, diagram_type, output_format))

      logger.debug(json)

      httpPost.setEntity(EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON).setText(json).build())
      val res = httpclient.execute(httpPost)

      html.tag("div") 
      html.append(EntityUtils.toString(res.getEntity(), "UTF-8"))
      html.tag("/div")
    } catch {
      case e: Exception => {
        html.tag("div") 
        html.append(e.getMessage())
        html.tag("/div")
      }
    }
  }

  /**
    * Renders a fenced code block with prettyprint classes.
    *
    * @param html HtmlWriter to write the output.
    * @param node FencedCodeBlock containing the code.
    * @param language The programming language of the code block.
    */
  private def renderPrittyPrint(
      html: HtmlWriter,
      node: FencedCodeBlock,
      context: NodeRendererContext,
      info: String
  ): Unit = {
    val langPattern: Regex = """([^:]+)(:[^\{]*){0,1}(\{[^\}]*\}){0,1}""".r
    var lang: String = "";
    var src = "";
    var addClasses = ""
    var classList = List("prettyprint")

    langPattern.findFirstMatchIn(info) match {
      case Some(value) => {
        lang = s"lang-${value.group(1)}"
        logger.debug(s"lang:'lang-${value.group(1)}'")
        if (value.group(2) != null) {
          src = value.group(2).replace(":", "")
          logger.debug(s"src: '${src}'")
        }
        if (value.group(3) != null) {
          addClasses = value.group(3).replace("{", "").replace("}", "")
          logger.debug(s"addClasses:'${addClasses}'")
        }
      }
      case None => {
        lang = s"lang-${info}"
      }
    }
    classList = classList :+ lang
    addClasses
      .split(" ")
      .foreach(cl => {
        logger.debug(s"class:'${cl}'")
        if (cl == ".line-numbers") {
          classList = classList :+ "linenums"
        } else {
          classList = classList :+ cl.replace(".", "")
        }
      })

    html.withAttr().attr("class", "codeblock-wrapper").tag("div")
    if (src != "") {
      html.tag("div")
      html.withAttr().attr("class", "filename").tag("span")
      html.append(src)
      html.tag("/span")
      html.tag("/div")
    }
    html
      .withAttr()
      .attr("class", classList.mkString(" "))
      .tag("pre")
    html.rawIndentedPre(Escaping.escapeHtml(node.getContentChars(), false))
    html.tag("/pre")
    html.tag("/div")
  }

  /**
  * Renders a wiki-style link.
  *
  * @param node WikiLink node representing the wiki link.
  * @param context NodeRendererContext for resolving links.
  * @param html HtmlWriter to write the output.
  */
  private def renderWikiLink(
      node: WikiLink,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    val link = node.getLink()
    val resolvedLink = context.resolveLink(
      com.vladsch.flexmark.html.renderer.LinkType.LINK,
      link,
      null
    )
    html
      .withAttr()
      .attr("href", resolvedLink.getUrl())
      .tag("a")
    html.text(node.getLink())
    html.tag("/a")
  }

  /**
    * Renders marked text wrapped in <mark> tags.
    *
    * If you write `==Marked Text==`, it will be converted to `&lt;mark&gt;Marked Text&lt;/mark&gt;`.
    * 
    * @param node Mark node representing the marked text.
    * @param context NodeRendererContext for rendering context.
    * @param html HtmlWriter to write the output.
    */
  private def renderMark(
      node: Mark,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    html.tag("mark")
    html.text(node.text.toString())
    html.tag("/mark")
  }

  /**
    * Renders an inline URI as a clickable link.
    *
    * If you write `http://example.com`, it will be converted to `<a href="http://example.com">http://example.com</a>`.
    * 
    * @param node InlineUri node representing the inline URI.
    * @param context NodeRendererContext for rendering context.
    * @param html HtmlWriter to write the output.
    */
  private def renderInlineUri(
      node: InlineUri,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    val uri = node.openingMarker.toString() + node.text.toString()
    html
      .withAttr()
      .attr("href", uri)
      .tag("a")
    html.text(uri)
    html.tag("/a")
  }

  /**
    * Renders an inline KaTeX node
    *
    * @param node inline KaTex node
    * @param context NodeRendererContext for rendering context.
    * @param html HtmlWriter to write the output.
    */
  private def renderInlineKatex(
      node: InlineKatex,
      context: NodeRendererContext,
      html: HtmlWriter
  ): Unit = {
    if (node.getOpeningMarker() == "$$" || node.getOpeningMarker() == "\\[") {
      // Display math
      html
        .withAttr()
        .attr("class", "katex")
        .tag("div")
      html.text(node.text.toString())
      html.tag("/div")
    } else {
      // Inline math
      html
        .withAttr()
        .attr("class", "katex")
        .tag("span")
      html.text(node.text.toString())
      html.tag("/span")
    }
  }
}

/**
  * Factory for creating instances of MarkdownEnhancedNodeRenderer.
  */
object MarkdownEnhancedNodeRenderer {
  class Factory() extends NodeRendererFactory {
    override def apply(options: DataHolder): NodeRenderer =
      new MarkdownEnhancedNodeRenderer()
  }
}
