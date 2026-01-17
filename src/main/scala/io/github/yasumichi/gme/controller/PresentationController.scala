package io.github.yasumichi.gme.controller

import gitbucket.core.controller.ControllerBase
import gitbucket.core.util.ReferrerAuthenticator
import gitbucket.core.service.AccountService
import gitbucket.core.service.RepositoryService

import gme.html

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
}
