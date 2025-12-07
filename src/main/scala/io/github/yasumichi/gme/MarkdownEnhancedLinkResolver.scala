package io.github.yasumichi.gme

import com.vladsch.flexmark.html.LinkResolver
import com.vladsch.flexmark.html.LinkResolverFactory
import com.vladsch.flexmark.html.renderer.{LinkStatus, LinkResolverBasicContext, ResolvedLink}
import com.vladsch.flexmark.util.ast.Node
import java.{util => ju}

/**
  * Enhanced link resolver for Markdown links.
  */
class MarkdownEnhancedLinkResolver extends LinkResolver {

  /**
    * Resolves a link based on its format and the current document context.
    *
    * This resolver modifies links based on their format:
    * - Absolute URLs (containing "://") are considered valid and unchanged.
    * - Root-relative URLs (starting with "/") are considered valid and unchanged.
    * - Other URLs are resolved based on the current document's path and a base URL.
    * - If the current path indicates a "blob", the link is treated as valid.
    * - If the current path indicates a "wiki", the link is resolved relative to the wiki page.
    * - If the current path indicates a "tree", it is converted to "blob" for link resolution.
    * - For other cases, the link is resolved to point to the "main" branch of the repository.
    * 
    * @param node The AST node containing the link.
    * @param context The link resolver context.
    * @param link The resolved link to be processed.
    * @return A ResolvedLink object with updated URL and status.
    */
  override def resolveLink(node: Node, context: LinkResolverBasicContext, link: ResolvedLink): ResolvedLink = {
    val url = link.getUrl()
    if (url.contains("://")) {
      link.withStatus(LinkStatus.VALID).withUrl(url)
    } else if (url.startsWith("/")) {
      link.withStatus(LinkStatus.VALID).withUrl(url)
    } else {
      val baseUrl = MarkdownEnhancedRenderer.BASE_URL.get(context.getOptions())
      val currentPath = MarkdownEnhancedRenderer.CURRENT_PATH.get(context.getOptions())
      val pathElems = currentPath.split("/")
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
