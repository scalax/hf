package net.scalax.hf

import net.scalax.fsn.core.{FReader, FWriter}
import slick.lifted.{FlatShapeLevel, Shape, TupleShape}

import scala.language.existentials

trait HfReader extends FReader {

  override type DataType

  override type Reader[T] = T

  override val reader: Reader[DataType]

  def toH: HReader[DataType] = HReader(reader)

}

case class HReader[D](
  override val reader: D
) extends HfReader {

  override type DataType = D

  def append[E](appended: HReader[E]): HReader[(D, E)] = {
    HReader(
      reader = this.reader -> appended.reader
    )

  }

}

trait HfWriter extends FWriter {
  type ColType
  override type DataType
  type TargetType

  override type Writer[S] = Shape[_ <: FlatShapeLevel, ColType, DataType, TargetType]

  val col: ColType
  override val writer: Shape[_ <: FlatShapeLevel, ColType, DataType, TargetType]

  def toH: HWriter[ColType, DataType, TargetType] = {
    HWriter(col, writer)
  }

}

case class HWriter[C, D, T](
  override val col: C,
  override val writer: Shape[_ <: FlatShapeLevel, C, D, T]
) extends HfWriter {

  type ColType = C
  override type DataType = D
  type TargetType = T

  def append[E, F, G](appended: HWriter[E, F, G]): HWriter[(C, E), (D ,F), (T, G)] = {
    HWriter(
      col = this.col -> appended.col,
      writer = new TupleShape[FlatShapeLevel, (this.ColType, appended.ColType), (this.DataType, appended.DataType), (this.TargetType, appended.TargetType)](this.writer, appended.writer)
    )

  }

}

trait HConverter {

  type Reader = HfReader
  type Writer = HfWriter

  val reader: HfReader

  val writer: HfWriter

  val isNeed: Boolean

  val convert: reader.DataType => writer.DataType

  def append(appended: HConverter): HConverter = {
    if (appended.isNeed) {
      val newReader = this.reader.toH.append(appended.reader.toH)
      val newWriter = this.writer.toH.append(appended.writer.toH)
      val newConvert = (newReaderData: newReader.DataType) => {
        val (oldData, newData) = newReaderData
        this.convert(oldData) -> appended.convert(newData)
      }
      new HConverter {
        override val reader = newReader
        override val writer = newWriter
        override val isNeed = true
        override val convert = newConvert
      }
    } else
      this
  }

  def when(isNeed1: Boolean): HConverter = {
    val self = this
    new HConverter {
      override val reader = self.reader
      override val writer = self.writer
      override val isNeed = isNeed1
      override val convert = self.convert.asInstanceOf[this.reader.DataType => this.writer.DataType]
    }
  }

}