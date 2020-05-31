package sgit.models

import java.util.zip.InflaterInputStream

import org.scalatest.funsuite.AnyFunSuite

class GitObjectTest extends AnyFunSuite {

  test("GitObject#deserializeはcommitオブジェクトをdeserializeする") {

    val is = Thread
      .currentThread()
      .getContextClassLoader
      .getResourceAsStream("objects/67/4d3b47975f8b85e953fc4f6d2e193e15eb9f8c")
    val commitObjectIs = new InflaterInputStream(is)
    val data = Iterator.continually(commitObjectIs.read()).map(_.toByte)

    val actual = GitObject.deserialize(data)
    val expected = GitCommit(
      tree = "e6459a6cee9fdbaf87519ebc92b8a1514e7fc04c",
      parent = "ad30b19eeef5ffb510b2a5773c4c572e9f509f8e",
      author = "todokr",
      committer = "todokr",
      message = "refine cat-file command"
    )

    assert(actual == expected)
    commitObjectIs.close()
  }
}
