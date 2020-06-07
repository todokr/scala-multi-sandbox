package sgit

import java.util.zip.InflaterInputStream

import org.scalatest.funsuite.AnyFunSuite
import sgit.models.GitTree

class TreeParserTest extends AnyFunSuite {

  test("TreeParser#parseOne") {
    val is = Thread
      .currentThread()
      .getContextClassLoader
      .getResourceAsStream("objects/00/401588456c7c42bc3cb0e7f3abd972f3a2d38f")
    val commitObjectIs = new InflaterInputStream(is)
    val data = Iterator
      .continually(commitObjectIs.read())
      .takeWhile(_ != -1)
      .dropWhile(_ != 0x00)
      .drop(1)
      .map(_.toByte)
      .toArray

    val actual = TreeParser.parseOne(data)
    val expected =
      (GitTree("100644", "CatFileCommand.scala", "fd1341fa37db1a7b"), 48)
    assert(actual === expected)
  }

  test("TreeParser#parse") {
    val is = Thread
      .currentThread()
      .getContextClassLoader
      .getResourceAsStream("objects/00/401588456c7c42bc3cb0e7f3abd972f3a2d38f")
    val commitObjectIs = new InflaterInputStream(is)
    val data = Iterator
      .continually(commitObjectIs.read())
      .takeWhile(_ != -1)
      .dropWhile(_ != 0x00)
      .drop(1)
      .map(_.toByte)
      .toArray

    val actual = TreeParser.parse(data)
    val expected = Seq(
      GitTree("100644", "InitCommand.scala", "b1ab165c03c08d56"),
      GitTree("100644", "HashObjectCommand.scala", "afeb482ecd8b1103"),
      GitTree("100644", "CatFileCommand.scala", "fd1341fa37db1a7b")
    )

    assert(actual === expected)
  }
}
