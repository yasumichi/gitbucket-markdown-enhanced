package io.github.yasumichi.gme.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.model.Session
import gitbucket.core.service.AccountService
import gitbucket.core.service.RepositoryService
import gitbucket.core.servlet.Database
import gitbucket.core.util.OwnerAuthenticator
import gitbucket.core.util.ReferrerAuthenticator

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
import io.github.yasumichi.gme.service.PresentationService
import gitbucket.core.model.CoreProfile

class PresentationController
    extends PresentationControllerBase
    with AccountService
    with OwnerAuthenticator
    with PluginSettingsService
    with ReferrerAuthenticator
    with RepositoryService
    with PresentationService
    with CoreProfile

trait PresentationControllerBase extends ControllerBase {
  self: AccountService
    with OwnerAuthenticator
    with PluginSettingsService
    with ReferrerAuthenticator
    with RepositoryService
    with PresentationService =>

  private val logger = LoggerFactory.getLogger(classOf[PresentationController])

  get("/:owner/:repository/presentation/*")(referrersOnly { repository =>
    implicit val session: Session = Database.getSession(context.request)
    val (id, path) = repository.splitPath(multiParams("splat").head)
    var theme: String = "white"
    if (!getTheme(repository.owner, repository.name).isEmpty) {
      theme = getTheme(repository.owner, repository.name).head.theme
    }
    if (path.isEmpty()) {
      html.presentation(repository, repository.repository.defaultBranch, "README.md", theme)
    } else {
      html.presentation(repository, id, path, theme)
    }
  })

  get("/:owner/:repository/settings/reveal")(ownerOnly { repository =>
    implicit val session: Session = Database.getSession(context.request)
    var theme: String = "white"
    if (!getTheme(repository.owner, repository.name).isEmpty) {
      theme = getTheme(repository.owner, repository.name).head.theme
    }
    html.options(repository, theme, flash.get("info"))
  })

  post("/:owner/:repository/settings/reveal")(ownerOnly { repository =>
    implicit val session: Session = Database.getSession(context.request)
    val theme = params("theme")
    insertTheme(repository.owner, repository.name, theme)
    flash.update("info", "Reveal theme saved")
    redirect(s"/${repository.owner}/${repository.name}/settings/reveal")
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
      val httpclient: CloseableHttpClient = HttpClients.createDefault();
      val httpPost = new HttpPost(krokiUrl)
      httpPost.addHeader("Content-Type", "application/json")
      val json = org.json4s.jackson.Serialization
        .write(PluginSettingsService.KrokiParams(diagram_source, diagram_type, output_format))

      logger.debug(json)

      httpPost.setEntity(EntityBuilder.create().setContentType(ContentType.APPLICATION_JSON).setText(json).build())
      val res = httpclient.execute(httpPost)

      EntityUtils.toString(res.getEntity(), "UTF-8")
    } catch {
      case e: Exception => e.getMessage()
    }
  })
}
