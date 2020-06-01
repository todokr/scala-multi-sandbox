package sgit

import scala.util.matching.Regex

import sgit.models.GitCommit

object CommitParser {

  def parse(content: String): GitCommit = {

    val treeLine :: parentLine :: authorLine :: committerLine :: _ :: messageLines =
      content.split("\n").toList
    TreePattern.findFirstMatchIn(treeLine).map(_.group(1))
    GitCommit(
      tree = extract(treeLine, TreePattern),
      parent = extract(parentLine, ParentPattern),
      author = extract(authorLine, AuthorPattern),
      committer = extract(committerLine, CommitterPattern),
      message = messageLines.mkString("\n")
    )
  }

  private val TreePattern: Regex = """tree (.+)""".r
  private val ParentPattern: Regex = """parent (.+)""".r
  private val AuthorPattern: Regex = """author ([^<]+)""".r
  private val CommitterPattern: Regex = """committer ([^<]+)""".r

  private def extract(input: String, pattern: Regex): String =
    pattern
      .findFirstMatchIn(input)
      .map(_.group(1).trim)
      .getOrElse(throw new Exception(s"not match pattern. input: $input"))
}
