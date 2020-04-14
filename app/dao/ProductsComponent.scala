package dao

import models.Product

object SQLiteProductsComponent
  extends ProductsComponent
    with SQLitePersistence

trait ProductsComponent extends Tables { this: DatabaseComponent with ProfileComponent =>

  import slick.lifted.Tag
  import profile.api._

  object ProductsRepository extends Repository[ProductsTable, Int](profile, db) {
    import this.profile.api._

    val table = Products
    def getId(table: ProductsTable) = table.productId
    def setId(product: Product, id: Id) = product.copy(productId = id)
  }

}
