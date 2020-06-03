package sgit

import scala.util.matching.Regex

import sgit.models.GitCommit

object CommitParser {

  def parse(content: String): GitCommit = {

    val treeLine :: parentLine :: authorLine :: committerLine :: _ :: messageLines =
      content.split("\n").toList
    TreePattern.findFirstMatchIn(treeLine).map(_.group(1))
    GitCommit(
      tree = extractOption(treeLine, TreePattern).getOrElse(
        throw new Exception(s"Illegal commit format. tree not found")
      ),
      parent = extractOption(parentLine, ParentPattern),
      author = extractOption(authorLine, AuthorPattern).getOrElse(
        throw new Exception(s"Illegal commit format. author not found")
      ),
      committer = extractOption(committerLine, CommitterPattern).getOrElse(
        throw new Exception(s"Illegal commit format. committer not found")
      ),
      message = messageLines.mkString("\n")
    )
  }

  private val TreePattern: Regex = """tree (.+)""".r
  private val ParentPattern: Regex = """parent (.+)""".r
  private val AuthorPattern: Regex = """author ([^<]+)""".r
  private val CommitterPattern: Regex = """committer ([^<]+)""".r

  private def extractOption(input: String, pattern: Regex): Option[String] =
    pattern
      .findFirstMatchIn(input)
      .map(_.group(1).trim)
}
