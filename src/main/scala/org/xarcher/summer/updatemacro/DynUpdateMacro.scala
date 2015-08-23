package org.xarcher.summer.updatemacro

import org.xarcher.summer.{UpdateBuilder, DynamicUpdateChange}

import slick.lifted._
import slick.dbio._
import scala.language.higherKinds
import scala.language.experimental.macros
import scala.reflect.macros.whitebox.Context

/**
 * Created by djx314 on 15-6-22.
 */

object UpdateAction {
  def tran[E <: AbstractTable[_], F[_]](updateBuilder: UpdateBuilder[E, F]): DBIOAction[Int, NoStream, Effect.Write] = macro tranImpl[E, F]
  def tranImpl[E <: AbstractTable[_], F[_]](c: Context)(updateBuilder: c.Expr[UpdateBuilder[E, F]]): c.Expr[DBIOAction[Int, NoStream, Effect.Write]] = {
    import c.universe._
    c.Expr[DBIOAction[Int, NoStream, Effect.Write]](
      q"""{
        val query = ${updateBuilder}.query
        val changesOpt = org.xarcher.summer.DynUpdate.update(${updateBuilder}.changes)
        lazy val zeroDBIO = DBIO.successful(0): slick.dbio.DBIOAction[Int, slick.dbio.NoStream, slick.dbio.Effect.Write]
        changesOpt.fold(zeroDBIO)(changes => {
          query.map(changes.col)(changes.shape).update(changes.data)
        })
      }""")
  }
}