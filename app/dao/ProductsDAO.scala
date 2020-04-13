package dao

import javax.inject.Inject
import models.Product
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class ProductsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[Product]] = db.run(Products.result)

  def insert(product: Product): Future[Product] = {
    val insertQuery = Products returning Products.map(_.productId) into ((product, id) => product.copy(productId = id))
    db.run(insertQuery += product)
  }

}
