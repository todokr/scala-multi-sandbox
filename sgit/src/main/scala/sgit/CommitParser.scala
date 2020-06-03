package sgit

import scala.util.matching.Regex

import sgit.models.GitCommit

object CommitParser {

  def parse(content: String): GitCommit = {

    val (metaLines, messageLines) = content.split('\n').span(_.nonEmpty) match {
      case (metas, msgs) =>
        (metas.mkString("\n"), msgs.filter(_.nonEmpty).mkString("\n"))
    }
    GitCommit(
      tree = extractOption(metaLines, TreePattern).getOrElse(
        throw new Exception(s"Illegal commit format. tree not found")
      ),
      parent = extractOption(metaLines, ParentPattern),
      author = extractOption(metaLines, AuthorPattern).getOrElse(
        throw new Exception(s"Illegal commit format. author not found")
      ),
      committer = extractOption(metaLines, CommitterPattern).getOrElse(
        throw new Exception(s"Illegal commit format. committer not found")
      ),
      message = messageLines
    )
  }

  private val TreePattern: Regex = """(?sm)^tree (.+?)$""".r
  private val ParentPattern: Regex = """(?sm).*^parent (.*?)$""".r
  private val AuthorPattern: Regex = """(?sm).*^author (.*?) <.*$""".r
  private val CommitterPattern: Regex = """(?sm).*^committer (.+?) <.*$""".r

  private def extractOption(input: String, pattern: Regex): Option[String] =
    pattern
      .findFirstMatchIn(input)
      .map(_.group(1).trim)
}
