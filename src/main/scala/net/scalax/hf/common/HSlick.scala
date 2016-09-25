package net.scalax.hf.common

import slick.ast.{AnonSymbol, Bind, Ref}
import slick.dbio.DBIO
import slick.jdbc.JdbcActionComponent
import slick.lifted._

import scala.reflect.ClassTag

trait DataWrap {

  type DataType
  type SourceType
  type TargetType

  val shape: Shape[_ <: FlatShapeLevel, SourceType, DataType, TargetType]
  val data: DataType
  val col: SourceType
  val isNeed: Boolean

}

case class DataWrapImpl[S, D, T](override val col: S, override val data: D, override val isNeed: Boolean)(override implicit val shape: Shape[_ <: FlatShapeLevel, S, D, T])
  extends DataWrap {
  override type DataType = D
  override type SourceType = S
  override type TargetType = T

  def when(boolean: Boolean): DataWrapImpl[S, D, T] = this.copy(isNeed = boolean)(shape)
}

trait QueryBind {
  def bind[E, U](query: Query[E, U, Seq]): Query[E, U, Seq]
}

trait HSlick {
  val bind: QueryBind
  val wraps: List[DataWrap]

  lazy val (updateQuery, updateData, isEmpty) = {
    val itemToUpdate = wraps.filter(_.isNeed == true)
    val lShape = new ListAnyShape(itemToUpdate.map(_.shape))
    (bind.bind(Query(itemToUpdate.map(_.col))(lShape)), itemToUpdate.map(_.data), SlickUtils.isShapeEmpty(lShape))
  }

  def update(implicit convert: Query[Seq[Any], Seq[Any], Seq] => JdbcActionComponent#UpdateActionExtensionMethods[Seq[Any]]): DBIO[Int] = {
    if (isEmpty)
      DBIO.successful(0)
    else {
      updateQuery.update(updateData)
    }
  }

  def insert(implicit convert: Query[Seq[Any], Seq[Any], Seq] => JdbcActionComponent#InsertActionExtensionMethods[Seq[Any]]): DBIO[Int] = {
    if (isEmpty)
      DBIO.successful(0)
    else {
      updateQuery.+=(updateData)
    }
  }
}

class QueryToUQueryExtensionMethods[E, U](query1: Query[E, U, Seq]) {

  def flatMap(f: E => HSlick)
  : HSlick = {
    val generator = new AnonSymbol
    val aliased = query1.shaped.encodeRef(Ref(generator)).value
    val fv = f(aliased)
    val bind1 = new QueryBind {
      override def bind[E, U](query: Query[E, U, Seq]): Query[E, U, Seq] = {
        val bindQuery = fv.bind.bind(query1)
        new WrappingQuery[E, U, Seq](new Bind(generator, bindQuery.toNode, query.toNode), query.shaped)
      }
    }

    new HSlick {
      override val bind = bind1
      override val wraps = fv.wraps
    }
  }

  def map(f: E => List[DataWrap]): HSlick = {
    val generator = new AnonSymbol
    val aliased = query1.shaped.encodeRef(Ref(generator)).value
    val fv = f(aliased)
    val bind1 = new QueryBind {
      override def bind[E, U](query: Query[E, U, Seq]): Query[E, U, Seq] = {
        new WrappingQuery[E, U, Seq](new Bind(generator, query1.toNode, query.toNode), query.shaped)
      }
    }
    new HSlick {
      override val bind = bind1
      override val wraps = fv
    }
  }

  def filter[T <: Rep[_] : CanBeQueryCondition](f: E => T): QueryToUQueryExtensionMethods[E, U] = {
    val cv = implicitly[CanBeQueryCondition[T]]
    new QueryToUQueryExtensionMethods(query1.filter(f)(cv))
  }

  def withFilter[T : CanBeQueryCondition](f: E => T): QueryToUQueryExtensionMethods[E, U] = {
    val cv = implicitly[CanBeQueryCondition[T]]
    new QueryToUQueryExtensionMethods(query1.withFilter(f)(cv))
  }

  def filterNot[T <: Rep[_] : CanBeQueryCondition](f: E => T): QueryToUQueryExtensionMethods[E, U] = {
    val cv = implicitly[CanBeQueryCondition[T]]
    new QueryToUQueryExtensionMethods(query1.filterNot(f)(cv))
  }

  def groupBy[K, T, G, P](f: E => K)(implicit kshape: Shape[_ <: FlatShapeLevel, K, T, G], vshape: Shape[_ <: FlatShapeLevel, E, _, P]): QueryToUQueryExtensionMethods[(G, Query[P, U, Seq]), (T, Query[P, U, Seq])] = {
    val newQuery = query1.groupBy(f)(kshape, vshape)
    new QueryToUQueryExtensionMethods(newQuery)
  }

}

trait QueryExtensionMethods {

  implicit class queryToUQueryExtendsionMethodGen[E, U](query: Query[E, U, Seq]) {

    def hf = new QueryToUQueryExtensionMethods[E, U](query)

  }

  implicit class slickHfRepExtensionMethod[R1, T1, G1](repLike: R1)(implicit shape1: Shape[_ <: FlatShapeLevel, R1, T1, G1]) {

    def setTo(data1: T1): DataWrapImpl[R1, T1, G1] = {
      setToDiff(data1, (s: T1) => s)
    }

    def setToDiff[T2](data1: T2, convert1: T2 => T1): DataWrapImpl[R1, T1, G1] = {
      DataWrapImpl(
        repLike,
        convert1(data1),
        true
      )(
        shape1
      )
    }

  }

}