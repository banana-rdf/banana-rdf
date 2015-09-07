package org.w3.banana.bigdata

import com.bigdata.rdf.sail.BigdataSailRepositoryConnection
import org.w3.banana.Transactor

import scala.util.Try

trait BigdataTransactor extends Transactor[Bigdata,BigdataSailRepositoryConnection]{
  /** Evaluates `body` in a read transaction. */
  override def r[T](con: BigdataSailRepositoryConnection, body: => T) = {
    val result: Try[T] = Try { body  }
    con.close()
    result
  }

  /** Evaluates `body` in a read/write transaction. */
  override def rw[T](con: BigdataSailRepositoryConnection, body: => T) = Try{
    con.setAutoCommit(false) //deprecated by sesame but used by Bigdata
    val result = body
    con.commit() //we have to commit the changes
    con.close()
    result
  }

}

