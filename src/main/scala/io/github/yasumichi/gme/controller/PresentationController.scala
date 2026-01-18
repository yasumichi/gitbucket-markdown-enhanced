package io.github.yasumichi.gme.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.ReferrerAuthenticator
import gitbucket.core.service.AccountService
import gitbucket.core.service.RepositoryService

import gme.html

// for PlantUML
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader

// for kroki
import io.github.yasumichi.gme.service.PluginSettingsService
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.http.client.entity.EntityBuilder
import org.apache.http.entity.ContentType
import org.slf4j.LoggerFactory

class PresentationController
    extends PresentationControllerBase
    with AccountService
    with PluginSettingsService
    with ReferrerAuthenticator
    with RepositoryService

trait PresentationControllerBase extends ControllerBase {
  self: AccountService with PluginSettingsService with ReferrerAuthenticator with RepositoryService =>

  private val logger = LoggerFactory.getLogger(classOf[PresentationController])

  get("/:owner/:repository/presentation/*")(referrersOnly { repository =>
    val (id, path) = repository.splitPath(multiParams("splat").head)
    if (path.isEmpty()) {
      html.presentation(repository, id, "README.md")
    } else {
      html.presentation(repository, id, path)
    }
  })

  ajaxPost("/:owner/:repository/puml")(referrersOnly { repository =>
    val content = params("content")
    val reader = new SourceStringReader(content)
    val os = new ByteArrayOutputStream()
    reader.outputImage(os, new FileFormatOption(FileFormat.SVG))
    os.close()

    var svg = new String(os.toByteArray(), Charset.forName("UTF-8"))
    svg
  })

  ajaxPost("/:owner/:repository/kroki")(referrersOnly { repository =>
    implicit val formats = org.json4s.DefaultFormats
    val krokiUrl: String = loadSettings().krokiUrl
    val diagram_source: String = params("diagram_source")
    val diagram_type: String = params("diagram_type")
    val output_format: String = params("output_format")

    logger.debug(diagram_source)
    logger.debug(diagram_type)
    logger.debug(output_format)
    logger.debug(krokiUrl)

    try {
      val httpclient: CloseableHttpClient= HttpClients.createDefault();
      val httpPost = new HttpPost(krokiUrl)
      httpPost.addHeader("Content-Type", "application/json")
      val json = org.json4s.jackson.Serialization.write(PluginSettingsService.KrokiParams(diagram_source, diagram_type, output_format))

      logger.debug(json)

      httpPost.setEntity(EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON).setText(json).build())
      val res = httpclient.execute(httpPost)

      EntityUtils.toString(res.getEntity(), "UTF-8")
    } catch {
      case e: Exception => e.getMessage()
    }
  })
}
