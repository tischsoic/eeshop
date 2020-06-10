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

    def getOrderItems(orderId: Id) =
      db.run(table.filter(_.orderId === orderId).result)

    def getOrderItemsWithProduct(orderId: Id) = {
      val orderItemsWithProducts = for {
        orderItem <- OrderItems if orderItem.orderId === orderId
        product <- Products if product.productId === orderItem.productId
        productType <- ProductTypes if (productType.productTypeId === product.productTypeId)
      } yield (orderItem, product, productType)

      db.run(orderItemsWithProducts.result)
    }
  }
}
