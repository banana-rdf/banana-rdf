package org.w3.banana
package sesame

import org.openrdf.model.Statement
import org.openrdf.repository.sail.SailRepository
import org.openrdf.repository.{RepositoryConnection, RepositoryResult}
import org.openrdf.sail.memory.MemoryStore
import org.w3.banana.sesame.Sesame._
import org.w3.banana.util.tryInstances._

import scala.util.Try

class SesameTransactorTest extends TransactorTest[Sesame,Try,RepositoryConnection] {

  val repo = new SailRepository(new MemoryStore)
  repo.initialize()

  def newConnection() = repo.getConnection

  protected def successfulWrite(con: RepositoryConnection) = transactor.rw(con,con.add(this.expectedGraph))

  protected def successfulRead(con: RepositoryConnection): Try[Sesame#Graph] = {
    transactor.r(con,{
      val statements: RepositoryResult[Statement] = con.getStatements(betehess, null, null, true, null)
      import org.w3.banana.sesame.extensions._
      ops.makeGraph(statements.toIterable)
    })
  }

  protected def brokenRead(con: RepositoryConnection): Try[Sesame#Graph] = {
    transactor.r(con,{
      val statements: RepositoryResult[Statement] = con.getStatements(betehess, null, null, true, null)
      import org.w3.banana.sesame.extensions._
      ops.makeGraph(statements.toIterable)
      throw this.readException
    })
  }

  protected def brokenWrite(con: RepositoryConnection): Try[Unit] = {
    transactor.rw(con,{
      con.add(this.expectedGraph)
      throw this.writeException
    })
  }

}
