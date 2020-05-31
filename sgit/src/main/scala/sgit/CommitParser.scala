package sgit

import scala.util.matching.Regex

import sgit.models.GitCommit

object CommitParser {

  val GpgEndLine: String = " -----END PGP SIGNATURE-----"
  val TreePattern: Regex = """tree (.+)""".r
  val ParentPattern: Regex = """parent (.+)""".r
  val AuthorPattern: Regex = """author ([^<]+)""".r
  val CommitterPattern: Regex = """committer ([^<]+)""".r

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

  private def extract(input: String, pattern: Regex): String =
    pattern
      .findFirstMatchIn(input)
      .map(_.group(1).trim)
      .getOrElse(throw new Exception(s"not match pattern. input: $input"))

//  private def metadata: Parser[String ~ String ~ String ~ String] =
//    tree ~ parent ~ author ~ committer
//  private def tree: Parser[String] = "tree" ~> alnum
//  private def parent: Parser[String] = "parent" ~> alnum
//  private def author: Parser[String] = "author" ~> alnum
//  private def committer: Parser[String] = "committer" ~> alnum
//  private def alnum: Parser[String] = """\w+""".r
}
