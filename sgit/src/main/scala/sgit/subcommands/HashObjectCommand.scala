package sgit.subcommands

import java.io.ByteArrayInputStream
import java.nio.file.{Files, Path}
import java.util.zip.DeflaterInputStream

import org.apache.commons.codec.digest.DigestUtils
import sgit.models.Repository

object HashObjectCommand {

  def run(isWrite: Boolean,
          objectType: String,
          file: Path,
          repository: Repository): Unit = {
    val fileBytes = Files.readAllBytes(file)

    // hash = sha1("<object_type><space><object_size>0x00<object_content>")
    val data =
      s"$objectType ${fileBytes.length}".getBytes.toBuffer
        .appended(0x00.toByte)
        .appendAll(fileBytes)
        .toArray
    val hash = DigestUtils.sha1Hex(data)

    if (isWrite) {
      val (dir, fileName) = hash.splitAt(2)
      val dirPath = repository.objectsDir.resolve(dir)
      val is = new DeflaterInputStream(new ByteArrayInputStream(data))
      val compressed =
        Iterator.continually(is.read()).takeWhile(_ != -1).map(_.toByte).toArray
      Files.createDirectories(dirPath)
      Files.write(dirPath.resolve(fileName), compressed)
      is.close()
    }
    println(hash)
  }
}
