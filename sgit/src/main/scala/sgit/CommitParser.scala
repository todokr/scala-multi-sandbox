package sgit

import java.nio.ByteBuffer

import scala.util.matching.Regex

import sgit.models.{GitCommit, GitTree}

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

object TreeParser {

  /**
    * parse tree
    */
  def parse(data: Array[Byte]): Seq[GitTree] = {
    val size = data.length

    @scala.annotation.tailrec
    def _parse(data: Array[Byte],
               acc: List[GitTree] = Nil,
               pos: Int = 0): Seq[GitTree] =
      parseOne(data, pos) match {
        case (obj, p) if p >= size =>
          obj :: acc
        case (obj, p) =>
          _parse(data, obj :: acc, p)
      }

    _parse(data)
  }

  /**
    * single leaf or node format is `[mode] space [path] 0x00 [sha-1]`
    * - [mode] is up to six bytes and is an ASCII representation of a file mode. For example, 100644 is encoded with byte values 49 (ASCII “1”), 48 (ASCII “0”), 48, 54, 52, 52.
    * - It’s followed by 0x20, an ASCII space;
    * - Followed by the null-terminated (0x00) path;
    * - Followed by the object’s SHA-1 in binary encoding, on 20 bytes.
    */
  def parseOne(data: Array[Byte], from: Int = 0): (GitTree, Int) = {
    val mode = new String(data.slice(from, from + 6))
    val pathIndex = data.indexOf(0x00, from)
    val path = new String(data.slice(from + 7, pathIndex))
    val sha1Index = pathIndex + 21
    val sha1bin = data.slice(pathIndex + 1, sha1Index)
    val sha1 = ByteBuffer.wrap(sha1bin).getLong.toHexString
    (GitTree(mode, path, sha1), sha1Index)
  }
}
