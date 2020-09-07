import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.zip.GZIPOutputStream

fun main() {
  var path = System.getProperty("user.dir")
  println(path)
  println()

  path = System.getProperty("java.home")
  println(path)
  println()

  path = System.getProperty("sun.boot.library.path")
  println(path)
  println()
  File(path).walk().forEach { println(it) }

  val baos = ByteArrayOutputStream()
  val gos = GZIPOutputStream(baos)
  val os = PrintStream(gos)
  os.println("Yo.")
}
