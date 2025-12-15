package io.github.yasumichi.gme

import com.vladsch.flexmark.html.renderer.LinkType
import com.vladsch.flexmark.html.LinkResolver
import com.vladsch.flexmark.html.LinkResolverFactory
import com.vladsch.flexmark.html.renderer.{LinkStatus, LinkResolverBasicContext, ResolvedLink}
import com.vladsch.flexmark.util.ast.Node
import java.{util => ju}

import org.slf4j.LoggerFactory

/**
  * Enhanced link resolver for Markdown links.
  */
class MarkdownEnhancedLinkResolver extends LinkResolver {
  private val logger = LoggerFactory.getLogger(classOf[MarkdownEnhancedLinkResolver])
  /**
    * Resolves a link based on its format and the current document context.
    *
    * This resolver modifies links based on their format:
    *  - Absolute URLs (containing "://") are considered valid and unchanged.
    *  - Root-relative URLs (starting with "/") are considered valid and unchanged.
    *  - Other URLs are resolved based on the current document's path and a base URL.
    *  - If the current path indicates a "blob", the link is treated as valid.
    *  - If the current path indicates a "wiki", the link is resolved relative to the wiki page.
    *  - If the current path indicates a "tree", it is converted to "blob" for link resolution.
    *  - For other cases, the link is resolved to point to the "main" branch of the repository.
    * 
    * @param node The AST node containing the link.
    * @param context The link resolver context.
    * @param link The resolved link to be processed.
    * @return A ResolvedLink object with updated URL and status.
    */
  override def resolveLink(node: Node, context: LinkResolverBasicContext, link: ResolvedLink): ResolvedLink = {
    var url = link.getUrl()
    if (url.startsWith("./")) {
      url = url.substring(2)
    }
    if (url.startsWith("../")) {
      var up = 0
      while (url.startsWith("../")) {
        up += 1
        url = url.substring(3)
      }
    }
    val baseUrl = MarkdownEnhancedRenderer.BASE_URL.get(context.getOptions())
    var currentPath = MarkdownEnhancedRenderer.CURRENT_PATH.get(context.getOptions())
    val pathElems = currentPath.split("/")

    logger.info(link.getLinkType().toString() + ": " + url)

    if (url.contains("://")) {
      link.withStatus(LinkStatus.VALID).withUrl(url)
    } else if (url.startsWith("/")) {
      link.withStatus(LinkStatus.VALID).withUrl(url)
    } else if (link.getLinkType() == LinkType.IMAGE || link.getLinkType() == LinkType.IMAGE_REF) {
      val user = pathElems(1)
      val repo = pathElems(2)
      val func = pathElems(3)
      var branch = "main"
      if (pathElems.length > 4) {
        branch = pathElems(4)
      }
      if (func == "wiki") {
        val imageUrl = s"${baseUrl}/${user}/${repo}/wiki/_blob/${url}"
        link.withStatus(LinkStatus.VALID).withUrl(imageUrl)
      } else {
        val imageUrl = s"${baseUrl}/${user}/${repo}/raw/${branch}/${url}"
        link.withStatus(LinkStatus.VALID).withUrl(imageUrl)
      }
    } else {
      if (pathElems.length > 3 && pathElems(3).equals("blob")) {
        link.withStatus(LinkStatus.VALID).withUrl(url)
      } else if (pathElems.length > 3 && pathElems(3).equals("wiki")) {
        if (pathElems.length == 4) {
          link.withStatus(LinkStatus.VALID).withUrl(baseUrl + currentPath + "/" + url)
        } else {
          link.withStatus(LinkStatus.VALID).withUrl(url)
        }
      } else if (pathElems.length > 3 && pathElems(3).equals("tree")) {
        pathElems(3) = "blob"
        link.withStatus(LinkStatus.VALID).withUrl(baseUrl + pathElems.mkString("/") + "/" + url)
      } else if (pathElems.length > 3 && pathElems(3).equals("_preview")) {
        val referer = MarkdownEnhancedRenderer.REFERER.get(context.getOptions())
        currentPath = referer.substring(baseUrl.length()) 
        var branch = "main"
        logger.info(s"CurrentPath: ${currentPath}")
        if (pathElems.length > 4) {
          branch = pathElems(4)
        }
        link.withStatus(LinkStatus.VALID).withUrl(baseUrl + currentPath.replace("/_preview", s"/blob/${branch}") + "/" + url)
      } else {
        link.withStatus(LinkStatus.VALID).withUrl(baseUrl + currentPath + "/blob/main/" + url)
      }
    }
  }
}

/**
  * Factory for creating instances of MarkdownEnhancedLinkResolver.
  */
object MarkdownEnhancedLinkResolver {
  class Factory() extends LinkResolverFactory {

    override def getAfterDependents(): ju.Set[Class[_ <: Object]] = { null }

    override def getBeforeDependents(): ju.Set[Class[_ <: Object]] = { null }

    override def affectsGlobalScope(): Boolean = { false }

    override def apply(context: LinkResolverBasicContext): LinkResolver = {
      new MarkdownEnhancedLinkResolver()
    }
  }
}
