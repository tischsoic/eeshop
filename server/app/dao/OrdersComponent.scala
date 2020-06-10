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

  object OrdersRepository extends Repository[OrdersTable, Int](profile, db) {
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

    def getUserOrders(userId: Int) = {
      val query = Orders.filter(_.customerId === userId)
      db.run(query.result)
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
    }
  }
}
