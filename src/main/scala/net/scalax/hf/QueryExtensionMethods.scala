package net.scalax.hf

import slick.ast.{AnonSymbol, Bind, Ref}
import slick.lifted._

class QueryToUQueryExtensionMethods[E, U](query1: Query[E, U, Seq]) {

  def flatMap(f: E => HQuery)
  : HQuery = {
    val generator = new AnonSymbol
    val aliased = query1.shaped.encodeRef(Ref(generator)).value
    val fv = f(aliased)
    val fvQuery = fv.query
    val query2 = new WrappingQuery[fv.E, fv.U, Seq](new Bind(generator, query1.toNode, fvQuery.toNode), fvQuery.shaped)
    new HQuery {
      override type E = fv.E
      override type U = fv.U
      override val query = query2
      override val value = fv.value
    }
  }

  def map(f: E => List[HConverter]): HQuery = {
    flatMap(s => {
      f(s).dropWhile(_.isNeed == false) match {
        case list@(head :: tail) =>
          val selectRep: HConverter = list.reduce(_ append _)
          val query2: Query[selectRep.writer.TargetType, selectRep.writer.DataType, Seq] = Query(selectRep.writer.col)(selectRep.writer.writer)
          new HQuery {
            override type E = selectRep.writer.TargetType
            override type U = selectRep.writer.DataType
            override val value: U = selectRep.convert(selectRep.reader.reader)
            override val query = query2
          }
        case _ =>
          new HQuery {
            override type E = Unit
            override type U = Unit
            override val value: U = ()
            override val query = Query.empty
          }
      }

    })
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

    def setTo(data1: T1): HConverter = {
      setToDiff(data1, (s: T1) => s)
    }

    def setToDiff[T2](data1: T2, convert1: T2 => T1): HConverter = {
      new HConverter {
        override val reader = HReader(data1)
        override val writer = HWriter(repLike, shape1)
        override val convert = convert1
        override val isNeed = true
      }
    }

  }

}