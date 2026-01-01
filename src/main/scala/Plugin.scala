import javax.servlet.ServletContext

import gitbucket.core.plugin.Link
import gitbucket.core.plugin.PluginRegistry
import gitbucket.core.controller.Context
import gitbucket.core.service.SystemSettingsService
import gitbucket.core.service.SystemSettingsService.SystemSettings
import io.github.gitbucket.solidbase.model.Version

import io.github.yasumichi.gme.MarkdownEnhancedRenderer

import scala.util.Try

/**
  * GitBucket Markdown Enhanced Plugin
  */
class Plugin extends gitbucket.core.plugin.Plugin {
  // Plugin metadata
  override val pluginId: String = "gitbucket-markdown-enhanced"
  // Plugin name
  override val pluginName: String = "GitBucket Markdown Enhanced Plugin"
  // Plugin description
  override val description: String = "Rendering markdown files."
  // Plugin versions
  override val versions: List[Version] = List(
    new Version("0.1.0"),
    new Version("0.1.1"),
    new Version("0.2.0"),
    new Version("0.3.0"),
    new Version("0.3.1"),
    new Version("0.3.2"),
    new Version("0.3.3"),
    new Version("0.3.4"),
    new Version("0.4.0"),
    new Version("0.4.1"),
    new Version("0.5.0"),
    new Version("0.5.1"),
    new Version("0.5.2"),
    new Version("0.5.3"),
    new Version("0.5.4"),
    new Version("0.5.5"),
    new Version("0.5.6"),
    new Version("0.5.7"),
    new Version("0.5.8"),
    new Version("0.5.9"),
    new Version("0.5.10"),
    new Version("0.5.11"),
    new Version("0.5.12"),
    new Version("0.6.0"),
    new Version("0.6.1")
  )

  // Renderer instance
  private[this] var renderer: Option[MarkdownEnhancedRenderer] = None

  /**
    * Initialize Plugin
    *
    * @param registry Plugin Registry
    * @param context Servlet Context
    * @param settings System Settings
    */
  override def initialize(
      registry: PluginRegistry,
      context: ServletContext,
      settings: SystemSettingsService.SystemSettings
  ): Unit = {
    val test = Try { new MarkdownEnhancedRenderer() }
    val mer = test.get
    registry.addRenderer("md", mer)
    registry.addRenderer("markdown", mer)
    renderer = Option(mer)
    super.initialize(registry, context, settings)
  }

  /**
    * Shutdown Plugin
    *
    * @param registry Plugin Registry
    * @param context Servlet Context
    * @param settings System Settings
    */
  override def shutdown(registry: PluginRegistry, context: ServletContext, settings: SystemSettings): Unit = {
    renderer.foreach(r => r.shutdown())
  }

  /**
    * Add JavaScript and CSS files to HTML head.
    *
    * @param registry Plugin Registry
    * @param context Servlet Context
    * @param settings System Settings
    * @return
    */
  override def javaScripts(
      registry: PluginRegistry,
      context: ServletContext,
      settings: SystemSettingsService.SystemSettings
  ): Seq[(String, String)] = {
    val jsPath = settings.baseUrl.getOrElse(context.getContextPath) + "/plugin-assets/gme"
    Seq(".*" -> s"""
      |</script>
      |<link rel='stylesheet' href='$jsPath/admonition.css'>
      |<link rel='stylesheet' href='$jsPath/katex.min.css'>
      |<link rel='stylesheet' href='$jsPath/gme.css'>
      |<script src="${jsPath}/admonition.js" type="text/javascript">
      |</script>
      |<script src="${jsPath}/katex.min.js" type="text/javascript">
      |</script>
      |<script src="${jsPath}/mermaid.min.js" type="text/javascript">
      |</script>
      |<script src="${jsPath}/wavedrom/default.js" type="text/javascript">
      |</script>
      |<script src="${jsPath}/wavedrom/wavedrom.min.js" type="text/javascript">
      |</script>
      |<script src="${jsPath}/vega/vega-6.2.0.js" type="text/javascript">
      |</script>
      |<script src="${jsPath}/vega/vega-lite-6.4.1.js" type="text/javascript">
      |</script>
      |<script src="${jsPath}/vega/vega-embed-7.0.2.js" type="text/javascript">
      |</script>
      |<script src="${jsPath}/gme.js" type="text/javascript">
      |""".stripMargin)
  }

  // Asset mappings
  override val assetsMappings = Seq("/gme" -> "/gme/assets")

}
