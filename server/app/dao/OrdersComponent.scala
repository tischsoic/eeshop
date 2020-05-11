package dao

import models.Order

object SQLiteOrdersComponent
  extends OrdersComponent
    with SQLitePersistence

trait OrdersComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object OrdersRepository extends Repository[OrdersTable, Int](profile, db) {
    val table = Orders
    def getId(table: OrdersTable) = table.orderId
    def setId(product: Order, id: Id) = product.copy(orderId = id)
  }
}
