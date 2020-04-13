package dao

import javax.inject.Inject
import models.Order
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class OrdersDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[Order]] = db.run(Orders.result)

  def insert(order: Order): Future[Order] = {
    val insertQuery = Orders returning Orders.map(_.orderId) into ((order, id) => order.copy(orderId = id))
    db.run(insertQuery += order)
  }
}
