package io.github.yasumichi.gme

import gitbucket.core.controller.Context
import gitbucket.core.plugin.{RenderRequest, Renderer}
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

/**
  * A renderer for Markdown Enhanced syntax using flexmark-java.
  */
class MarkdownEnhancedRenderer extends Renderer {
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
    Html(toHtml(fileContent)(context))
  }

  def shutdown(): Unit = {}

  /**
    * Converts Markdown content to HTML using flexmark-java with various extensions.
    *
    * @param content the Markdown content to convert
    * @param context the rendering context containing base URL and current path information
    * @return the converted HTML content as a string
    */
  def toHtml(content: String)(implicit context: Context): String = {
    val options = new MutableDataSet();
    val extension: Seq[Extension] = Seq(
      AbbreviationExtension.create(),
      AdmonitionExtension.create(),
      AnchorLinkExtension.create(),
      EmojiExtension.create(),
      FootnoteExtension.create(),
      GfmIssuesExtension.create(),
      GitLabExtension.create(),
      StrikethroughSubscriptExtension.create(),
      SuperscriptExtension.create(),
      TablesExtension.create(),
      TaskListExtension.create(),
      TocExtension.create(),
      WikiLinkExtension.create(),
      MarkdownEnhancedExtention.create()
    )
    options.setFrom(ParserEmulationProfile.GITHUB)
    options.set(
      EmojiExtension.USE_IMAGE_TYPE,
      EmojiImageType.UNICODE_FALLBACK_TO_IMAGE
    )

    // Determine current path for GitHub Issues links
    val pathElems = context.currentPath.split("/")
    if (pathElems.length > 2 && pathElems(1) != "admin") {
      var owner = pathElems(1)
      var repos = pathElems(2)

      options.set(GfmIssuesExtension.GIT_HUB_ISSUES_URL_ROOT, context.baseUrl + "/" + owner + "/" + repos + "/issues")
    }

    options.set(Parser.EXTENSIONS, extension.asJava)
    options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "title-anchor")
    options.set(MarkdownEnhancedRenderer.BASE_URL, context.baseUrl)
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
  val REFERER = new DataKey[String]("REFERER", "")
}
