import javax.servlet.ServletContext

import gitbucket.core.plugin.Link
import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.controller.Context
import gitbucket.core.service.SystemSettingsService
import gitbucket.core.service.SystemSettingsService.SystemSettings
import io.github.gitbucket.solidbase.model.Version

import io.github.yasumichi.gme.MarkdownEnhancedRenderer

import scala.util.Try

class Plugin extends gitbucket.core.plugin.Plugin {
  override val pluginId: String = "gitbucket-markdown-enhanced"
  override val pluginName: String = "GitBucket Markdown Enhanced Plugin"
  override val description: String = "Rendering markdown files."
  override val versions: List[Version] = List(
    new Version("0.1.0"),
    new Version("0.1.1"),
    new Version("0.2.0"),
    new Version("0.3.0")
  )

  private[this] var renderer: Option[MarkdownEnhancedRenderer] = None

  override def initialize(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Unit = {
    val test = Try{ new MarkdownEnhancedRenderer() }
    val mer = test.get
    registry.addRenderer("md", mer)
    renderer = Option(mer)
    super.initialize(registry, context, settings)
  }

  override def shutdown(registry: PluginRegistry, context: ServletContext, settings: SystemSettings): Unit = {
    renderer.foreach(r => r.shutdown())
  }

  override def javaScripts(registry: PluginRegistry, context: ServletContext, settings: SystemSettingsService.SystemSettings): Seq[(String, String)] = {
    val jsPath = settings.baseUrl.getOrElse(context.getContextPath) + "/plugin-assets/gme"
    Seq(".*" -> s"""
      |</script>
      |<link rel='stylesheet' href='$jsPath/admonition.css'>
      |<link rel='stylesheet' href='$jsPath/katex.min.css'>
      |<script src="${jsPath}/admonition.js">
      |</script>
      |<script src="${jsPath}/katex.min.js">
      |</script>
      |<script src="${jsPath}/mermaid.min.js">
      |</script>
      |<script src="${jsPath}/gme.js">>
      |""".stripMargin)
  }

  override val assetsMappings = Seq("/gme" -> "/gme/assets")

}
