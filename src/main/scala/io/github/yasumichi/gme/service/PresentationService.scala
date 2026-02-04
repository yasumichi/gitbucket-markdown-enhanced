package io.github.yasumichi.gme.service

import gitbucket.core.model.CoreProfile
import io.github.yasumichi.gme.model.GmeReveal
import io.github.yasumichi.gme.model.Profile._
import gitbucket.core.model.Profile.profile.blockingApi._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.jdk.CollectionConverters._

trait PresentationService {
    self: CoreProfile =>
    import self.profile.api

    def getTheme(userName: String, repositoryName: String)(implicit session: Session): List[GmeReveal] = {
       GmeReveals.filter(i => i.userName === userName && i.repositoryName === repositoryName).list
    }

    def insertTheme(userName: String, repositoryName: String, theme: String)(implicit session: Session): Int = {
        if (getTheme(userName, repositoryName).isEmpty) {
            GmeReveals.insert(GmeReveal(userName = userName, repositoryName = repositoryName, theme = theme))
        } else {
            GmeReveals.filter(i => i.userName === userName && i.repositoryName === repositoryName).map(t => (t.userName, t.repositoryName, t.theme)).update((userName, repositoryName, theme))
        }
    }
}