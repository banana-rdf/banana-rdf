
import java.io.{File, FileOutputStream, IOException}
import java.nio.file.*
import java.nio.file.attribute.*
import scala.collection.immutable.Map

trait Cache(var db: Map[Path, BasicFileAttributes]):
	lazy val mod: Map[Path, FileTime] = db.view.mapValues(_.lastModifiedTime.nn).toMap

val start = java.nio.file.Path.of(".")

def visitor(depth: Int) = new SimpleFileVisitor[Path] with Cache(Map[Path, BasicFileAttributes]()):
	var level = 0

	override
	def preVisitDirectory(f: Path, attrs: BasicFileAttributes): FileVisitResult =
		if level < depth then
			level = level + 1;
			db = db.updated(f, attrs)
			FileVisitResult.CONTINUE
		else FileVisitResult.SKIP_SUBTREE

	override
	def visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult =
		if attrs.isDirectory && level >= depth
		then FileVisitResult.SKIP_SUBTREE
		else
			db = db.updated(file, attrs)
			FileVisitResult.CONTINUE
end visitor

def diff[P](db1: Map[Path, P], db2: Map[Path, P]): Map[Path, P] =
	(db1.toSet diff db2.toSet).toMap

class Act(action: => Unit):
	def doit(): Unit = action

def write(file: File, txt: String): Unit =
	val out = new FileOutputStream(file)
	out.write(txt.getBytes)
	out.close

@main
def test =
	import java.io.*
	val visitors = LazyList.continually(visitor(3))
	val actions = LazyList(
		new Act(File("hello.txt").createNewFile()),
		new Act(File("hi.txt").createNewFile()),
		new Act(write(File("hi.txt"),"Hello World!")),
		new Act(File("blog/comments").mkdirs()),
		new Act(File("blog/blog1.html").createNewFile),
		new Act(write(File("blog/blog1.html"),"Great post")),
		new Act(File("blog/blog2.html").createNewFile),
		new Act(File("blog/comments/GreatPost").createNewFile),
		new Act(File("blog/comments/GreatPost").delete)
	)
	val history: List[Cache]  = actions.zip(visitors).map { (act, v) =>
		act.doit()
		Thread.sleep(2000) // to make it easier to see the time stamps
		Files.walkFileTree(start, v)
		v
	}.toList //we exec it now

	history.sliding(2, 1) foreach {
		case head :: tail :: Nil => println(diff(tail.mod, head.mod))
		case _ => println("end")
	}





