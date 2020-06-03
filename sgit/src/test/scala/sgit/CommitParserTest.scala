package sgit

import org.scalatest.EitherValues
import org.scalatest.funsuite.AnyFunSuite
import sgit.models.GitCommit

class CommitParserTest extends AnyFunSuite with EitherValues {

  test("CommitParser#parse parse commit content") {
    val commitContent =
      """tree e6459a6cee9fdbaf87519ebc92b8a1514e7fc04c
      |parent ad30b19eeef5ffb510b2a5773c4c572e9f509f8e
      |author Shunsuke Tadokoro <s.tadokoro0317@gmail.com> 1589105990 +0900
      |committer todokr <s.tadokoro0317@gmail.com> 1589105990 +0900
      |
      |refine cat-file command
      |""".stripMargin

    val actual = CommitParser.parse(commitContent);
    val expected = GitCommit(
      tree = "e6459a6cee9fdbaf87519ebc92b8a1514e7fc04c",
      parent = Some("ad30b19eeef5ffb510b2a5773c4c572e9f509f8e"),
      author = "Shunsuke Tadokoro",
      committer = "todokr",
      message = "refine cat-file command"
    )

    assert(actual == expected)
  }
}
