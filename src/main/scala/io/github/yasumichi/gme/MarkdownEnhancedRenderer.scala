package io.github.yasumichi.gme

import gitbucket.core.controller.Context
import gitbucket.core.plugin.{RenderRequest, Renderer}
import gitbucket.core.service.AccountService
import gitbucket.core.service.RepositoryService
import play.twirl.api.Html
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.misc.Extension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.{Parser, ParserEmulationProfile}
import com.vladsch.flexmark.util.data.MutableDataSet
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
import com.vladsch.flexmark.ext.admonition.AdmonitionExtension
import com.vladsch.flexmark.ext.anchorlink.AnchorLinkExtension
import com.vladsch.flexmark.ext.gfm.issues.GfmIssuesExtension
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension
import com.vladsch.flexmark.ext.gitlab.GitLabExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.toc.TocExtension
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.emoji.{EmojiExtension, EmojiImageType}
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import scala.jdk.CollectionConverters._
import com.vladsch.flexmark.util.data.DataKey
import com.vladsch.flexmark.ext.wikilink.WikiLinkExtension
import org.slf4j.LoggerFactory

import io.circe.yaml
import io.github.yasumichi.gme.service.PresentationService
import gitbucket.core.model.CoreProfile
import gitbucket.core.model.Session
import gitbucket.core.servlet.Database

/**
  * A renderer for Markdown Enhanced syntax using flexmark-java.
  */
class MarkdownEnhancedRenderer extends Renderer with PresentationService with CoreProfile with AccountService with RepositoryService {
  private val logger = LoggerFactory.getLogger(classOf[MarkdownEnhancedRenderer])

  /**
    * Utility method to enable checkboxes
    */
  def enableCheckbox(html: String, enable: Boolean): String = {
    if (enable) {
      val re = "(<input\\s+[^<>]*type=\"checkbox\"\\s+[^<>]*)\\s+disabled[^<>]*>".r
      re.replaceAllIn(html, "$1>")
    } else {
      html
    }
  }

  /**
    * Renders the given Markdown content to HTML.
    *
    * @param request the render request containing file content and context information
    * @return the rendered HTML
    */
  def render(request: RenderRequest): Html = {
    import request._

    val (yamlPart, markdown) = MarkdownPreprocessor.devideYaml(fileContent)
    if (yamlPart.length() > 0) {
      val json = yaml.v12.parser.parse(yamlPart)
      logger.info(s"${json}")
      json match {
        case Left(value)  => Html(enableCheckbox(toHtml(markdown, false)(context), true))
        case Right(value) => {
          if (value.hcursor.downField("presentation").succeeded) {
            implicit val session: Session = Database.getSession(context.request)
            var theme: String = "white"
            if (!getTheme(repository.owner, repository.name).isEmpty) {
              theme = getTheme(repository.owner, repository.name).head.theme
            }
            var html = s"""
            |<link rel="stylesheet" href="${context.baseUrl}/plugin-assets/gme/reveal.js-5.2.1/reset.css">
            |<link rel="stylesheet" href="${context.baseUrl}/plugin-assets/gme/reveal.js-5.2.1/reveal.css">
            |<link rel="stylesheet" href="${context.baseUrl}/plugin-assets/gme/reveal.js-5.2.1/theme/${theme}.css">
            |<link rel="stylesheet" href="${context.baseUrl}/plugin-assets/gme/fontawesome-free-6.7.2/css/all.min.css">
            |<link rel="stylesheet" href="${context.baseUrl}/plugin-assets/gme/reveal.js-5.2.1/plugin/highlight/monokai.css">
            |
            |<script src="${context.baseUrl}/plugin-assets/gme/fontawesome-free-6.7.2/js/all.min.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/mermaid/mermaid.min.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/wavedrom/default.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/wavedrom/wavedrom.min.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/vega/vega-6.2.0.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/vega/vega-lite-6.4.1.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/vega/vega-embed-7.0.2.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/marked/marked.min.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/reveal.js-5.2.1/reveal.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/reveal.js-5.2.1/plugin/markdown/markdown.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/reveal.js-5.2.1/plugin/highlight/highlight.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/reveal.js-5.2.1/plugin/math/math.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/emoji.js"></script>
            |<script src="${context.baseUrl}/plugin-assets/gme/renderer.js"></script>
            """.stripMargin
            val separator = raw"(?m)^---$$".r
            val parts = separator.split(markdown).map(_.trim).filter(_.nonEmpty)
            parts.foreach { p =>
              html = html + s"""
              |<div class="reveal">
              |  <div class="slides">
              |    <section data-markdown>${p}</section>
              |  </div>
              |</div>
              """.stripMargin
            }
            html = html + s"""
            |<script>
            |  const pumlUrl = "${context.baseUrl}/${repository.owner}/${repository.name}/puml";
            |  const krokiUrl = "${context.baseUrl}/${repository.owner}/${repository.name}/kroki";
            |  const katexUrl = "${context.baseUrl}/plugin-assets/gme/katex";
            |  const relativePathBase = "${context.baseUrl}/@repository.owner/@repository.name/raw/@id/@filePath";
            |</script>
            |<script src="${context.baseUrl}/plugin-assets/gme/slidelist.js"></script>
            """.stripMargin
            Html(html)
          } else {
            Html(enableCheckbox(toHtml(markdown, false)(context), true))
          }
        }
      }
    } else {
      Html(enableCheckbox(toHtml(markdown, false)(context), true))
    }
  }

  def shutdown(): Unit = {}

  /**
    * Converts Markdown content to HTML using flexmark-java with various extensions.
    *
    * @param content the Markdown content to convert
    * @param context the rendering context containing base URL and current path information
    * @return the converted HTML content as a string
    */
  def toHtml(content: String, presentation: Boolean)(implicit context: Context): String = {
    implicit val s: Session = Database.getSession(context.request)
    val options = new MutableDataSet();
    var extension: Seq[Extension] = Seq(
      AbbreviationExtension.create(),
      AdmonitionExtension.create(),
      EmojiExtension.create(),
      FootnoteExtension.create(),
      GfmIssuesExtension.create(),
      GitLabExtension.create(),
      StrikethroughSubscriptExtension.create(),
      SuperscriptExtension.create(),
      TablesExtension.create(),
      TaskListExtension.create(),
      WikiLinkExtension.create(),
      MarkdownEnhancedExtention.create()
    )
    if (!presentation) {
      extension = extension :+ AnchorLinkExtension.create()
      extension = extension :+ TocExtension.create()
    }
    options.setFrom(ParserEmulationProfile.GITHUB)
    options.set(
      EmojiExtension.USE_IMAGE_TYPE,
      EmojiImageType.UNICODE_FALLBACK_TO_IMAGE
    )
    options.set(TocExtension.LIST_CLASS, "toc")

    // Determine current path for GitHub Issues links
    val pathElems = context.currentPath.split("/")
    if (pathElems.length > 2 && pathElems(1) != "admin") {
      var owner = pathElems(1)
      var repos = pathElems(2)
      var defaultBranch = ""
      val info = getRepository(owner, repos)

      info.foreach{repo =>
        options.set(MarkdownEnhancedRenderer.DEFAULT_BRANCH, repo.repository.defaultBranch)
      }
      options.set(GfmIssuesExtension.GIT_HUB_ISSUES_URL_ROOT, context.baseUrl + "/" + owner + "/" + repos + "/issues")
    }

    options.set(Parser.EXTENSIONS, extension.asJava)
    options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "title-anchor")
    options.set(MarkdownEnhancedRenderer.BASE_URL, context.baseUrl)
    options.set(MarkdownEnhancedRenderer.EMOJI_BASE, s"${context.baseUrl}/plugin-assets/emoji/")
    options.set(MarkdownEnhancedRenderer.CURRENT_PATH, context.currentPath)
    options.set(MarkdownEnhancedRenderer.REFERER, context.request.getHeader("Referer"))

    val parser = Parser.builder(options).build()
    val renderer = HtmlRenderer.builder(options).build()

    val document = parser.parse(content)
    renderer.render(document)
  }
}

/**
  * Data keys for passing context information to the Markdown renderer.
  */
object MarkdownEnhancedRenderer {
  val BASE_URL = new DataKey[String]("BASE_URL", "")
  val CURRENT_PATH = new DataKey[String]("CURRENT_PATH", "")
  val DEFAULT_BRANCH = new DataKey[String]("DEFAULT_BRANCH", "main")
  val EMOJI_BASE = new DataKey[String]("EMOJI_BASE", "")
  val REFERER = new DataKey[String]("REFERER", "")
}
