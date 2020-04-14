package dao

import models.Shipment

object SQLiteShipmentsComponent
  extends ShipmentsComponent
    with SQLitePersistence

trait ShipmentsComponent extends Tables { this: DatabaseComponent with ProfileComponent =>
  import profile.api._

  object ShipmentsRepository extends Repository[ShipmentsTable, Int](profile, db) {
    val table = Shipments
    def getId(table: ShipmentsTable) = table.shipmentId
    def setId(product: Shipment, id: Id) = product.copy(shipmentId = id)
  }
}
