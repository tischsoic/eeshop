package dao

import javax.inject.Inject
import models.ProductType
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class ProductTypesDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[ProductType]] = db.run(ProductTypes.result)

  def insert(productType: ProductType): Future[ProductType] = {
    val insertQuery = ProductTypes returning ProductTypes.map(_.productTypeId) into ((productType, id) => productType.copy(productTypeId = id))
    db.run(insertQuery += productType)
  }



}
