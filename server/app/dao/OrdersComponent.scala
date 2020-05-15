package dao

import scala.concurrent._
import ExecutionContext.Implicits.global

import models.OrderStatus.OrderStatus
import models.{Order, OrderStatus}

object SQLiteOrdersComponent
  extends OrdersComponent
    with SQLitePersistence

trait OrdersComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._
//  import scala.concurrent.ExecutionContext

  object OrdersRepository extends Repository[OrdersTable, Int](profile, db) {
    // TODO: remove repetition
    implicit val orderStatusMapper = MappedColumnType.base[OrderStatus, String](
      _.toString,
      OrderStatus.withName
    )

    val table = Orders
    def getId(table: OrdersTable) = table.orderId
    def setId(product: Order, id: Id) = product.copy(orderId = id)

    def updateStatus(orderId: Id, newStatus: OrderStatus) = {
      val q = for { order <- Orders if order.orderId === orderId } yield order.status
      val updateAction = q.update(newStatus)

      db.run(updateAction)
    }

    def getOrCreateCheckout(userId: Int) = {
      val insertIfNotExists = table.filter(
          order => order.customerId === userId && order.status === OrderStatus.AwaitingPayment
        ).exists.result.flatMap(exists => {
          if (!exists) table += Order(0, userId, OrderStatus.AwaitingPayment)
          else DBIO.successful(None)
        })
        .flatMap(_ => table.filter(
          order => order.customerId === userId && order.status === OrderStatus.AwaitingPayment).result.headOption
        )

      db.run(insertIfNotExists.transactionally)
//      val r2 = r.map(_ => db.run(
//        table.filter(order => order.customerId === userId && order.status === OrderStatus.AwaitingPayment).result.headOption
//      ))
//      r2
//      val exists = table.filter(
//        order => order.customerId === userId && order.status === OrderStatus.AwaitingPayment
//      ).exists
//      val insert = Order(0, userId, OrderStatus.AwaitingPayment)
//      for (order <- Query(insert) if !exists) yield order
    }
  }
}
