package io.github.yasumichi.gme

import com.vladsch.flexmark.html.LinkResolver
import com.vladsch.flexmark.html.LinkResolverFactory
import com.vladsch.flexmark.html.renderer.{LinkStatus, LinkResolverBasicContext, ResolvedLink}
import com.vladsch.flexmark.util.ast.Node
import java.{util => ju}

class MarkdownEnhancedLinkResolver extends LinkResolver {
    override def resolveLink(node: Node, context: LinkResolverBasicContext, link: ResolvedLink): ResolvedLink = {
        val url = link.getUrl()
        if (url.contains("://")) {
            link.withStatus(LinkStatus.VALID).withUrl(url)
        }
        else if (url.startsWith("/")) {
            link.withStatus(LinkStatus.VALID).withUrl(url)
        } else {
            val baseUrl = MarkdownEnhancedRenderer.BASE_URL.get(context.getOptions())
            val currentPath = MarkdownEnhancedRenderer.CURRENT_PATH.get(context.getOptions())
            val pathElems = currentPath.split("/")
            if (pathElems.length > 3 && pathElems(3).equals("blob")) {
                link.withStatus(LinkStatus.VALID).withUrl(url)
            } else if(pathElems.length > 3 && pathElems(3).equals("wiki")) {
                link.withStatus(LinkStatus.VALID).withUrl(url)
            } else if(pathElems.length > 3 && pathElems(3).equals("tree")) {
                pathElems(3) = "blob"
                link.withStatus(LinkStatus.VALID).withUrl(baseUrl + pathElems.mkString("/") + "/" + url)
            } else {
                link.withStatus(LinkStatus.VALID).withUrl(baseUrl +  currentPath + "/blob/main/" + url)
            }
        }
    }
}

object MarkdownEnhancedLinkResolver {
    class Factory() extends LinkResolverFactory {

        override def getAfterDependents(): ju.Set[Class[_ <: Object]] = {null}

        override def getBeforeDependents(): ju.Set[Class[_ <: Object]] = {null}

        override def affectsGlobalScope(): Boolean = {false}

        override def apply(context: LinkResolverBasicContext): LinkResolver = {
            new MarkdownEnhancedLinkResolver()
        }
    }
}