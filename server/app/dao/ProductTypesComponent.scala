package dao

import models.ProductType

object SQLiteProductTypesComponent
  extends ProductTypesComponent
    with SQLitePersistence

trait ProductTypesComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object ProductTypesRepository extends Repository[ProductTypesTable, Int](profile, db) {
    import profile.api._

    val table = ProductTypes
    def getId(table: ProductTypesTable) = table.productTypeId
    def setId(product: ProductType, id: Id) = product.copy(productTypeId = id)

    def getProductTypesWithProducts() = {
      val crossJoin = for {
        product <- Products
        productType <- ProductTypes if (product.productTypeId === productType.productTypeId)
      } yield (product.name, productType.name)

      db run crossJoin.result
    }
  }

}
