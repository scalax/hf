package org.xarcher.summer.test

import slick.collection.heterogeneous.HNil
import slick.driver.H2Driver.api._
/**
 * Created by djx314 on 15-6-22.
 */

case class SmallModel(
  id: Option[Long],
  a1: Int,
  a2: Option[Int],
  a3: String,
  a4: Int,
  a5: Int
)

class SmallTable(tag: Tag) extends Table[SmallModel](tag, "aabbbbbbb") {
  def id = column[Option[Long]]("id", O.PrimaryKey)
  def a1 = column[Int]("a1")
  def a2 = column[Option[Int]]("a2")
  def a3 = column[String]("a3")
  def a4 = column[Int]("a4")
  def a5 = column[Int]("a5")

  def * =
    (id ::
      a1 ::
      a2 ::
      a3 ::
      a4 ::
      a5 ::
      HNil
      ).shaped <> (
      { case x => SmallModel(
        x(0),
        x(1),
        x(2),
        x(3),
        x(4),
        x(5)
      )}, ({ x: SmallModel =>
      Option((
        x.id ::
          x.a1 ::
          x.a2 ::
          x.a3 ::
          x.a4 ::
          x.a5 ::
          HNil
        ))
    })
      )
}

case class LargeModel(
  id: Option[Long],
  a1: Option[Int],
  a2: Option[Int],
  a3: Option[Int],
  a4: Option[Int],
  a5: Int,
  a6: Int,
  a7: Int,
  a8: Int,
  a9: Int,
  a10: Option[Int],
  a11: Option[Int],
  a12: Option[Int],
  a13: Option[Int],
  a14: Option[Int],
  a15: Option[Int],
  a16: Option[Int],
  a17: Option[Int],
  a18: Option[Int],
  a19: Option[Int],
  a20: Option[Int],
  a21: Option[Int],
  a22: Option[Int],
  a23: Option[String],
  a24: Option[String]
)

class LargeTable(tag: Tag) extends Table[LargeModel](tag, "test_aabb") {
  def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
  def a1 = column[Option[Int]]("a1")
  def a2 = column[Option[Int]]("a2")
  def a3 = column[Option[Int]]("a3")
  def a4 = column[Option[Int]]("a4")
  def a5 = column[Int]("a5")
  def a6 = column[Int]("a6")
  def a7 = column[Int]("a7")
  def a8 = column[Int]("a8")
  def a9 = column[Int]("a9")
  def a10 = column[Option[Int]]("a10")
  def a11 = column[Option[Int]]("a11")
  def a12 = column[Option[Int]]("a12")
  def a13 = column[Option[Int]]("a13")
  def a14 = column[Option[Int]]("a14")
  def a15 = column[Option[Int]]("a15")
  def a16 = column[Option[Int]]("a16")
  def a17 = column[Option[Int]]("a17")
  def a18 = column[Option[Int]]("a18")
  def a19 = column[Option[Int]]("a19")
  def a20 = column[Option[Int]]("a20")
  def a21 = column[Option[Int]]("a21")
  def a22 = column[Option[Int]]("a22")
  def a23 = column[Option[String]]("a23")
  def a24 = column[Option[String]]("a24")

  def * =
    (
      id ::
        a1 ::
        a2 ::
        a3 ::
        a4 ::
        a5 ::
        a6 ::
        a7 ::
        a8 ::
        a9 ::
        a10 ::
        a11 ::
        a12 ::
        a13 ::
        a14 ::
        a15 ::
        a16 ::
        a17 ::
        a18 ::
        a19 ::
        a20 ::
        a21 ::
        a22 ::
        a23 ::
        a24 ::
        HNil
      ).shaped <> (
      { case x => LargeModel(
        x(0),
        x(1),
        x(2),
        x(3),
        x(4),
        x(5),
        x(6),
        x(7),
        x(8),
        x(9),
        x(10),
        x(11),
        x(12),
        x(13),
        x(14),
        x(15),
        x(16),
        x(17),
        x(18),
        x(19),
        x(20),
        x(21),
        x(22),
        x(23),
        x(24)
      )}, ({ x: LargeModel =>
      Option((
        x.id ::
          x.a1 ::
          x.a2 ::
          x.a3 ::
          x.a4 ::
          x.a5 ::
          x.a6 ::
          x.a7 ::
          x.a8 ::
          x.a9 ::
          x.a10 ::
          x.a11 ::
          x.a12 ::
          x.a13 ::
          x.a14 ::
          x.a15 ::
          x.a16 ::
          x.a17 ::
          x.a18 ::
          x.a19 ::
          x.a20 ::
          x.a21 ::
          x.a22 ::
          x.a23 ::
          x.a24 ::
          HNil
        ))
    })
      )
}
