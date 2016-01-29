package net.scalax.hf

import slick.lifted.{Shape, ShapeLevel}

trait HData {

  type ColType
  type DataType
  type TargetType

  val shape: Shape[_ <: ShapeLevel, ColType, DataType, TargetType]

  val col: ColType
  val data: DataType

  val isNeed: Boolean

  def need(isNeed: Boolean) = {
    this.copy(isNeed = isNeed)
  }

  def copy(col: ColType = this.col, data: DataType = this.data, isNeed: Boolean = this.isNeed) = {
    type ColType1 = ColType
    type DataType1 = DataType
    type TargetType1 = TargetType
    val shape1 = shape
    val col1 = col
    val data1 = data
    val isNeed1 = isNeed
    new HData {
      override type ColType = ColType1
      override type DataType = DataType1
      override type TargetType = TargetType1
      override val shape = shape1
      override val col = col1
      override val data = data1
      override val isNeed = isNeed1
    }
  }

}