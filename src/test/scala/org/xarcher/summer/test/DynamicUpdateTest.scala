package org.xarcher.summer.test

import org.h2.jdbcx.JdbcDataSource
import org.scalatest._
import org.scalatest.concurrent._
import org.slf4j.LoggerFactory
import org.xarcher.summer.UpdateQuery
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.language.higherKinds

/**
 * Created by djx314 on 15-6-22.
 */

class DynamicUpdateTest extends FlatSpec
    with ScalaFutures
    with Matchers
    with BeforeAndAfter
    with OneInstancePerTest {

  val logger = LoggerFactory.getLogger(getClass)

  val smallTq = TableQuery[SmallTable]
  val largeTq = TableQuery[LargeTable]

  lazy val db = {
    val datasource = new JdbcDataSource()
    datasource.setUrl(s"jdbc:h2:mem:summerTest;DB_CLOSE_DELAY=-1")
    Database.forDataSource(datasource)
  }

  val data = SmallModel(Some(2333L), 1, Some(2), "a3", 4, 5)
  val getQ = smallTq.filter(_.id === 2333L).result.head

  before {
    Await.result(db.run((smallTq.schema ++ largeTq.schema).create), Duration.Inf)
    Await.result(db.run(smallTq += data), Duration.Inf)
  }

  after {
    Await.result(db.run((smallTq.schema ++ largeTq.schema).drop), Duration.Inf)
  }

  "Small table" should "update some colunms" in {

    /*val updateQ = smallTq.filter(_.id === Option(2333.toLong))
      .change(_.a1, 2333)
      .change(_.a2, Some(2333))
      .change(_.a3, "wang")*/

    val updateQ = UpdateQuery(smallTq.filter(_.id === Option(2333.toLong)))
      .change(_.a1, 2333)
      .change(_.a2, Some(2333))
      .change(_.a3, "wang")
      .result

    val updated = db.run(updateQ >> getQ).futureValue
    updated.a1 should be(2333)
    updated.a2 should be(Some(2333))
    updated.a3 should be("wang")
  }

  "Small table" should "update dynamic" in {

    /*val updateQ = smallTq.filter(_.id === 2333L)
      .changeIf("github" == "github")(_.a1, 2333)
      .changeIf("scala" == "china")(_.a2, Some(2333))
      .changeIf("archer" == "saber")(_.a3, "wang")*/

    val updateQ = UpdateQuery(smallTq.filter(_.id === 2333L))
      .changeIf("github" == "github")(_.a1, 2333)
      .changeIf("scala" == "china")(_.a2, Some(2333))
      .changeIf("archer" == "saber")(_.a3, "wang")
      .result

    val finalQ = updateQ >> getQ
    val updated = db.run(finalQ).futureValue
    updated.a1 should be(2333)
    updated.a2 should be(Some(2))
    updated.a3 should be("a3")
  }
}
