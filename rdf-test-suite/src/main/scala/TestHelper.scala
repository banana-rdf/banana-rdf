package org.w3.banana

import org.scalatest._
import scala.concurrent._
import java.util.concurrent._

object TestHelper {

  def time[A](body: => A): A = {
    val start = System.currentTimeMillis
    val r = body
    val time = System.currentTimeMillis - start
    println("$$$ " + time)
    r
  }

}

trait TestHelper extends BeforeAndAfterAll { self: Suite =>

  val executorService: ExecutorService = Executors.newFixedThreadPool(2)

  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executorService)

  override def afterAll(): Unit = {
    super.afterAll()
    executorService.shutdown()
  }

}
