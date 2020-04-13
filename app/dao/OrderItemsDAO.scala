package dao

import javax.inject.Inject
import models.OrderItem
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class OrderItemsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[OrderItem]] = db.run(OrderItems.result)

  def insert(orderItem: OrderItem): Future[OrderItem] = {
    val insertQuery = OrderItems returning OrderItems.map(_.orderItemId) into ((orderItem, id) => orderItem.copy(orderItemId = id))
    db.run(insertQuery += orderItem)
  }

}

