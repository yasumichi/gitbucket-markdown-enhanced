package io.github.yasumichi.gme

class MarkdownPreprocessor {}

object MarkdownPreprocessor {
  def devideYaml(markdown: String): (String, String) = {
    val pattern = """(?s)^---\s*(.*?)\s*---\s*(.*)""".r

    markdown match {
      case pattern(yamlPart, bodyPart) =>
        (yamlPart, bodyPart)
      case _ =>
        ("", markdown)
    }
  }
}
