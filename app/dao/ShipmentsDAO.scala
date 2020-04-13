package dao

import javax.inject.Inject
import models.Shipment
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class ShipmentsDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext)
  extends Dao {
  import profile.api._

  def all(): Future[Seq[Shipment]] = db.run(Shipments.result)

  def insert(shipment: Shipment): Future[Shipment] = {
    val insertQuery = Shipments returning Shipments.map(_.shipmentId) into ((shipment, id) => shipment.copy(shipmentId = id))
    db.run(insertQuery += shipment)
  }

}

