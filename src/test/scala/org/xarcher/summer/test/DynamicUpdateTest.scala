package org.xarcher.summer.test

import org.h2.jdbcx.JdbcDataSource
import org.scalatest._
import org.slf4j.LoggerFactory
import org.xarcher.summer._
import slick.driver.H2Driver.api._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by djx314 on 15-6-22.
 */

class DynamicUpdateTest extends FlatSpec with Matchers with BeforeAndAfterAll {

  val logger = LoggerFactory.getLogger(getClass)

  val smallTq = TableQuery[SmallTable]
  val largeTq = TableQuery[LargeTable]

  lazy val db = {
    val datasource = new JdbcDataSource()
    datasource.setUrl(s"jdbc:h2:mem:summerTest;DB_CLOSE_DELAY=-1")
    Database.forDataSource(datasource)
  }

  override def beforeAll = {
    Await.result(db.run((smallTq.schema ++ largeTq.schema).create), Duration.Inf)
  }

  "Small table" should "update some colunms" in {

    val updateAction = DynUpdate.update(smallTq.filter(_.id === Option(2333.toLong)))(
      DynData(((s: SmallTable) => s.a1), 2333) ::
      DynData(((s: SmallTable) => s.a2), Option(2333)) ::
      DynData(((s: SmallTable) => s.a3), "wang") :: Nil
    )
    Await.result(db.run(updateAction), Duration.Inf)

  }

  "Small table" should "update dynamic" in {

    val aa = if ("github" == "github") Option(DynData(((s: SmallTable) => s.a1), 2333)) else None
    val bb = if ("scala" == "china") Option(DynData(((s: SmallTable) => s.a2), Option(2333))) else None
    val cc = if ("archer" == "saber") Option(DynData(((s: SmallTable) => s.a3), "wang")) else None

    val updateList = (aa :: bb :: cc :: Nil).collect { case Some(s) => s }

    val updateAction = DynUpdate.update(smallTq.filter(_.id === Option(2333.toLong)))(updateList)
    Await.result(db.run(updateAction), Duration.Inf)

  }

}