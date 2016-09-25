package net.scalax.hf.common

import slick.lifted._

import scala.annotation.tailrec
import scala.reflect.ClassTag

final class ListAnyShape[Level <: ShapeLevel](override val shapes: Seq[Shape[_ <: ShapeLevel, _, _, _]])
  extends MappedProductShape[Level, Seq[Any], Seq[Any], Seq[Any], Seq[Any]] {
  override def getIterator(value: Seq[Any]) = value.toIterator
  override def getElement(value: Seq[Any], idx: Int) = value(idx)
  override def buildValue(elems: IndexedSeq[Any]) = elems
  override def copy(shapes: Seq[Shape[_ <: ShapeLevel, _, _, _]]) = new ListAnyShape(shapes)
  override val classTag = implicitly[ClassTag[Seq[Any]]]
}

object SlickUtils {

  @tailrec
  def countColumns(columns: Seq[RepShape.type], shapes: Seq[Shape[_ <: ShapeLevel, _, _, _]]): Seq[RepShape.type] = {
    if (shapes.isEmpty) {
      columns
    } else {
      val repShapes = shapes.filter(s => s == RepShape).map(_ => RepShape)
      val hShapes = shapes.collect { case s: ProductNodeShape[ShapeLevel @unchecked, _, _, _, _] =>
        s.shapes.map(_.asInstanceOf[Shape[ShapeLevel, _, _, _]])
      }.flatten
      countColumns(repShapes ++: columns, hShapes)
    }
  }

  def shapeLength(shape: Shape[_ <: ShapeLevel, _, _, _]): Int = {
    countColumns(Nil, List(shape)).size
  }

  def isShapeEmpty(shape: Shape[_ <: ShapeLevel, _, _, _]): Boolean = {
    countColumns(Nil, List(shape)).isEmpty
  }

}