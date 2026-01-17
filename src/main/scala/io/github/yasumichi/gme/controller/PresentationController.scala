package io.github.yasumichi.gme.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.ReferrerAuthenticator
import gitbucket.core.service.AccountService
import gitbucket.core.service.RepositoryService

import gme.html

import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader

class PresentationController
    extends PresentationControllerBase
    with AccountService
    with ReferrerAuthenticator
    with RepositoryService

trait PresentationControllerBase extends ControllerBase {
  self: AccountService with ReferrerAuthenticator with RepositoryService =>

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
}
