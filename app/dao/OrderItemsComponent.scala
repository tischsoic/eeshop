package dao

import models.OrderItem

object SQLiteOrderItemsComponent
  extends OrderItemsComponent
    with SQLitePersistence

trait OrderItemsComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object OrderItemsRepository extends Repository[OrderItemsTable, Int](profile, db) {
    val table = OrderItems
    def getId(table: OrderItemsTable) = table.orderItemId
    def setId(product: OrderItem, id: Id) = product.copy(orderItemId = id)
  }
}
