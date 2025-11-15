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
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughSubscriptExtension
import com.vladsch.flexmark.ext.superscript.SuperscriptExtension
import com.vladsch.flexmark.ext.gitlab.GitLabExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.toc.TocExtension
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.emoji.{EmojiExtension, EmojiImageType}
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension
import collection.JavaConverters._
import com.vladsch.flexmark.util.data.DataKey

class MarkdownEnhancedRenderer extends Renderer {
  def render(request: RenderRequest): Html = {
    import request._
    Html(toHtml(fileContent)(context))
  }

  def shutdown(): Unit = {}

  def toHtml(content: String)(implicit context: Context): String = {
    val options = new MutableDataSet();
    val extension: Seq[Extension] = Seq(
      AbbreviationExtension.create(),
      AdmonitionExtension.create(),
      AnchorLinkExtension.create(),
      EmojiExtension.create(),
      FootnoteExtension.create(),
      GitLabExtension.create(),
      StrikethroughSubscriptExtension.create(),
      SuperscriptExtension.create(),
      TablesExtension.create(),
      TaskListExtension.create(),
      TocExtension.create(),
      MarkdownEnhancedExtention.create()
    )
    options.setFrom(ParserEmulationProfile.GITHUB)
    options.set(
      EmojiExtension.USE_IMAGE_TYPE,
      EmojiImageType.UNICODE_FALLBACK_TO_IMAGE
    )
    options.set(Parser.EXTENSIONS, extension.asJava)
    options.set(AnchorLinkExtension.ANCHORLINKS_ANCHOR_CLASS, "title-anchor")
    options.set(MarkdownEnhancedRenderer.BASE_URL, context.baseUrl)
    options.set(MarkdownEnhancedRenderer.CURRENT_PATH, context.currentPath)

    val parser = Parser.builder(options).build()
    val renderer = HtmlRenderer.builder(options).build()

    val document = parser.parse(content)
    renderer.render(document)
  }
}

object MarkdownEnhancedRenderer {
  val BASE_URL = new DataKey[String]("BASE_URL", "")
  val CURRENT_PATH = new DataKey[String]("CURRENT_PATH", "")
}