package dao

import models.Product

object SQLiteProductsComponent
  extends ProductsComponent
    with SQLitePersistence

trait ProductsComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object ProductsRepository extends Repository[ProductsTable, Int](profile, db) {
    val table = Products
    def getId(table: ProductsTable) = table.productId
    def setId(product: Product, id: Id) = product.copy(productId = id)

    def getProduct(productId: Id) = {
      val query = for {
        (product, productType) <- table filter (getId(_) === productId) joinLeft ProductTypes on (_.productTypeId === _.productTypeId)
      } yield (product, productType)

      db.run(query.result.headOption)
    }
  }
}
