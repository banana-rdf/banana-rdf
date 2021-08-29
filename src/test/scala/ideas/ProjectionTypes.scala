package ideas

object LoggerPlay {
	trait Logger:
		def log(message: String): Unit

	class ConsoleLogger extends Logger:
		def log(message: String): Unit = println(s"log: $message")

	trait Service:
		type Log <: Logger
		val logger: Log

	class ConsoleService extends Service:
		type Log = ConsoleLogger
		val logger: ConsoleLogger = ConsoleLogger()
}

class ProjectionTypes extends munit.FunSuite {
	import LoggerPlay.*

	test("compile error") {
		assert(
			compileErrors("val l1: Service#Log = ConsoleLogger()").startsWith(
				"""error:
				  |Found:    ideas.LoggerPlay.ConsoleLogger
				  |Required: ideas.LoggerPlay.Service#Log
				  |""".stripMargin
			)
		)
	}

	test("works") {
		val l1: ConsoleService#Log = ConsoleLogger()
	}

}
