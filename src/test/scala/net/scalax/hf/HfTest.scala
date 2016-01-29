package net.scalax.hf.test

import net.scalax.hf._

import org.h2.jdbcx.JdbcDataSource
import org.scalatest._
import org.scalatest.concurrent._
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.driver.H2Driver.api._

/**
 * Created by djx314 on 16-1-30.
 */

class HfTest extends FlatSpec
    with ScalaFutures
    with Matchers
    with BeforeAndAfter
    with OneInstancePerTest {

  val logger = LoggerFactory.getLogger(getClass)

  val smallTq = TableQuery[SmallTable]
  val largeTq = TableQuery[LargeTable]

  lazy val db = {
    val datasource = new JdbcDataSource()
    datasource.setUrl(s"jdbc:h2:mem:hfTest;DB_CLOSE_DELAY=-1")
    Database.forDataSource(datasource)
  }

  val data = SmallModel(Some(2333L), 1, Some(2), "a3", 4, 5)
  val getQ = smallTq.filter(_.id === 2333L).result.head

  before {
    Await.result(db.run((smallTq.schema ++ largeTq.schema).create), Duration.Inf)
    Await.result(db.run(smallTq.hf.map(s => List(s.gen to data)).insert), Duration.Inf)
  }

  after {
    Await.result(db.run((smallTq.schema ++ largeTq.schema).drop), Duration.Inf)
  }

  "table" should "update some colunms" in {

    val updateQ =
      for {
        small <- smallTq.filter(_.id === 2333L).hf
      } yield {
        List(
          small.a1.gen to 2333,
          small.a2.gen to Some(2333),
          small.a3.gen to "wang"
        )
      }

    val updated = db.run(updateQ.update >> getQ).futureValue
    updated.a1 should be(2333)
    updated.a2 should be(Some(2333))
    updated.a3 should be("wang")

  }

  it should "update dynamic" in {

    val updateQ =
      for {
        small <- smallTq.filter(_.id === 2333L).hf
      } yield {
        List(
          small.a1.gen to 2333 need ("github" == "github"),
          small.a2.gen to Some(2333) need ("scala" == "china"),
          small.a3.gen to "wang" need ("archer" == "saber")
        )
      }

    val finalQ = updateQ.update >> getQ
    val updated = db.run(finalQ).futureValue
    updated.a1 should be(2333)
    updated.a2 should be(Some(2))
    updated.a3 should be("a3")
  }

  it should "update dynamic with dynamic columns" in {

    val updateQ =
      for {
        small <- smallTq.filter(_.id === 2333L).hf
      } yield {
        List(
          small.column[Int]("a1").gen to 2333 need ("github" == "github"),
          small.column[Option[Int]]("a2").gen to Some(2333) need ("scala" == "china"),
          small.column[String]("a3").gen to "wang",
          small.column[Int]("a4").gen to 5678 need ("archer" == "archer")
        )
      }

    val finalQ = updateQ.update.transactionally >> getQ
    val updated = db.run(finalQ).futureValue
    updated.a1 should be(2333)
    updated.a2 should be(Some(2))
    updated.a3 should be("wang")
    updated.a4 should be(5678)
  }

  "empty update list" should "update with out exception and return 0" in {
    val updateQ =
      for {
        small <- smallTq.filter(_.id === 2333L).hf
      } yield {
        Nil
      }

    val updated = db.run(updateQ.update).futureValue
    updated should be(0)
  }

}
