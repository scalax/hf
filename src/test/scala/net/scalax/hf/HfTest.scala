package net.scalax.hf.test

import net.scalax.hf._
import net.scalax.hf.common.DataWrapImpl
import org.h2.jdbcx.JdbcDataSource
import org.scalatest._
import org.scalatest.concurrent._
import org.slf4j.LoggerFactory
import slick.ast.TypedType

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import slick.jdbc.H2Profile.api._
import slick.lifted.AbstractTable

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

  val data = SmallModel(Option(2333L), 1, Option(2), "a3", 4, 5)
  val getQ = smallTq.filter(_.id === 2333L).result.head

  before {
    Await.result(db.run((smallTq.schema ++ largeTq.schema).create), Duration.Inf)
    Await.result(db.run(smallTq.hf.map(s => List(s setTo data)).insert), Duration.Inf)
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
          small.a1 setTo 2333,
          small.a2 setTo Option(2333),
          small.a3 setTo "wang"
        )
      }

    val updated = db.run(updateQ.update >> getQ).futureValue
    updated.a1 should be(2333)
    updated.a2 should be(Option(2333))
    updated.a3 should be("wang")

  }

  it should "update dynamic" in {

    val updateQ =
      for {
        small <- smallTq.hf if small.id === 2333L
      } yield {
        List(
          small.a1 setTo 2333 when ("github" == "github"),
          small.a2 setTo Option(2333) when ("scala" == "china"),
          small.a3 setTo "wang" when ("archer" == "saber")
        )
      }

    val finalQ = updateQ.update >> getQ
    val updated = db.run(finalQ).futureValue
    updated.a1 should be(2333)
    updated.a2 should be(Option(2))
    updated.a3 should be("a3")
  }

  it should "update dynamic with dynamic columns" in {

    val updateQ =
      for {
        small <- smallTq.hf if small.id === 2333L
      } yield {
        List(
          small.column[Int]("a1") setTo 2333 when ("github" == "github"),
          small.column[Option[Int]]("a2") setTo Option(2333) when ("scala" == "china"),
          small.column[String]("a3") setTo "wang",
          small.column[Int]("a4") setTo 5678 when ("archer" == "archer")
        )
      }

    val finalQ = updateQ.update.transactionally >> getQ
    val updated = db.run(finalQ).futureValue
    updated.a1 should be(2333)
    updated.a2 should be(Option(2))
    updated.a3 should be("wang")
    updated.a4 should be(5678)
  }

  "empty update list" should "update with out exception and return 0" in {
    val updateQ =
      for {
        small <- smallTq.hf if small.id === 2333L
      } yield {
        Nil
      }

    val updated = db.run(updateQ.update).futureValue
    updated should be(0)
  }

  it should "insert dynamic" in {

    class IdTable(tag: slick.lifted.Tag, name: String) extends Table[Long](tag, name) {
      def id = column[Long]("id", O.PrimaryKey)
      def * = id
    }
    def tqOf(tableName: String): TableQuery[IdTable] = {
      new TableQuery(cons => new IdTable(cons, tableName))
    }
    
    implicit class slickHfRepExtensionMethod[T <: Table[_]](repLike: T) {
      def into[T1](colName: String, data1: T1)(implicit shape1: Shape[FlatShapeLevel, Rep[T1], T1, Rep[T1]], tpe: TypedType[T1]): DataWrapImpl[Rep[T1], T1, Rep[T1]] = {
        DataWrapImpl(
          repLike.column[T1](colName)(tpe),
          data1,
          true
        )(
          shape1
        )
      }
    }

    val insertQ =
      for {
        small <- tqOf("aabbbbbbb").hf
      } yield {
        List(
          small.into("id", 123456),
          small.into("a1", 2333),
          small.into("a2", Option(2)),
          small.into("a3", "wang"),
          small.into("a4", 5678),
          small.into("a5", 9101112)
        )
      }

    val finalQ = insertQ.insert.transactionally >> smallTq.filter(_.id === 123456L).result.head
    val inserted = db.run(finalQ).futureValue
    inserted.a1 should be(2333)
    inserted.a2 should be(Option(2))
    inserted.a3 should be("wang")
    inserted.a4 should be(5678)
  }

}
