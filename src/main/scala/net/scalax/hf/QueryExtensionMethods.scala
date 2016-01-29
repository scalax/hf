package net.scalax.hf

import slick.ast.{AnonSymbol, Bind, Ref}
import slick.lifted._

class QueryToUQueryExtensionMethods[E, U](query1: Query[E, U, Seq]) {

  private trait Change {
    type ColType <: Product
    type ValType <: Product
    type TargetType <: Product
    val col: ColType
    val data: ValType
    val shape:  Shape[_ <: FlatShapeLevel, ColType, ValType, TargetType]

    def append(change: HData) = {
      type NewColType = (ColType, change.ColType)
      type NewValType = (ValType, change.DataType)
      type NewTargetType = (TargetType, change.TargetType)
      val colunm: NewColType = col -> change.col
      val dynTuple2Shape = new TupleShape[FlatShapeLevel, (ColType, change.ColType), (ValType, change.DataType), (TargetType, change.TargetType)](shape, change.shape)
      val value = data -> change.data
      new Change {
        override type ColType = NewColType
        override type ValType = NewValType
        override type TargetType = NewTargetType
        override val col = colunm
        override val data = value
        override val shape = dynTuple2Shape
      }
    }

  }

  private object Change {

    def head(change: HData): Change = {
      val colunm: Tuple1[change.ColType] = Tuple1(change.col)
      val value =  Tuple1(change.data)
      implicit val dynTuple1Shape = new TupleShape[FlatShapeLevel, Tuple1[change.ColType], Tuple1[change.DataType], Tuple1[change.TargetType]](change.shape)
      new Change {
        override type ColType = Tuple1[change.ColType]
        override type ValType = Tuple1[change.DataType]
        override type TargetType = Tuple1[change.TargetType]
        override val col = colunm
        override val data = value
        override val shape = dynTuple1Shape
      }
    }

  }

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

  def map(f: E => List[HData]): HQuery = {
    flatMap(s => {
      f(s).filter(_.isNeed == true) match {
        case head :: tail =>
          val selectRep = tail.foldLeft(Change.head(head))((eachRep, toAppend) => {
            eachRep.append(toAppend)
          })
          val query2: Query[selectRep.TargetType, selectRep.ValType, Seq] = Query(selectRep.col)(selectRep.shape)
          new HQuery {
            override type E = selectRep.TargetType
            override type U = selectRep.ValType
            override val value: U = selectRep.data
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

  implicit class slickHfRepExtensionMethod[R1](repLike: R1) {

    class HDataGen[R1, T1, G1](val inRep: R1, val shape1: Shape[_ <: ShapeLevel, R1, T1, G1]) {
      def to(data1: T1): HData = {
        new HData {
          override type ColType = R1
          override type DataType = T1
          override type TargetType = G1
          override val shape = shape1
          override val col = inRep
          override val data = data1
          override val isNeed = true
        }
      }
    }

    def gen[T1, G1]()(implicit shape1: Shape[_ <: ShapeLevel, R1, T1, G1]): HDataGen[R1, T1, G1] = {
      new HDataGen(repLike, shape1)
    }

  }

}