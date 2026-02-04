package io.github.yasumichi.gme.model

trait GmeRevealsComponent { self: gitbucket.core.model.Profile =>
    import profile.api._
    import self._

    lazy val GmeReveals = TableQuery[GmeReveals]

    class GmeReveals(tag: Tag) extends Table[GmeReveal](tag, "GME_REVEAL") {
        val userName = column[String]("USER_NAME")
        val repositoryName = column[String]("REPOSITORY_NAME")
        val theme = column[String]("THEME")
        def * = (
            userName,
            repositoryName,
            theme
        ).mapTo[GmeReveal]
    }
}

case class GmeReveal(
    userName: String,
    repositoryName: String,
    theme: String
)