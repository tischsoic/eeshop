package controllers

import java.sql.Date
import java.time.LocalDate

import javax.inject._
import models.DeleteForm.deleteForm
import models.Shipment
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ShipmentController @Inject()(cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteShipmentsComponent._
  import dao.SQLiteOrdersComponent._

//  case class Shipment(shipmentId: Int, orderId: Int, date: Date, trackingCode: String)
  val shipmentForm: Form[Shipment] = Form {
    mapping(
      "shipmentId" -> default(number, 0),
      "orderId" -> number,
      "date" -> sqlDate,
      "trackingCode" -> nonEmptyText
    )(Shipment.apply)(Shipment.unapply)
  }

  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    OrdersRepository.all().map(
      orders => Ok(views.html.shipment.create(shipmentForm, orders))
    )
  }

  def handleCreate() = Action.async { implicit request =>
    shipmentForm.bindFromRequest.fold(
      formWithErrors => OrdersRepository.all().map(
        orders => BadRequest(views.html.shipment.create(formWithErrors, orders))
      ),
      shipment => {
        ShipmentsRepository.insertWithReturn(shipment).map(
          _ => Redirect(routes.ShipmentController.create()).flashing("success" -> "shipment.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    ShipmentsRepository.findById(id).flatMap {
      case None => Future.successful(
        NotFound(Json.obj({"error" -> "Shipments not found"}))
      )
      case Some(shipment) => OrdersRepository.all().map(
        orders => Ok(views.html.shipment.update(shipmentForm.fill(shipment), orders))
      )
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    shipmentForm.bindFromRequest.fold(
      formWithErrors => OrdersRepository.all().map(
        orders => BadRequest(views.html.shipment.update(formWithErrors, orders))
      ),
      shipment => {
        ShipmentsRepository.update(shipment.shipmentId, shipment).map(
          _ => Redirect(routes.ShipmentController.update(shipment.shipmentId)).flashing("success" -> "shipment.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    ShipmentsRepository.all().map(shipments => Ok(views.html.shipment.all(shipments, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        ShipmentsRepository.all().map(shipments => BadRequest(views.html.shipment.all(shipments, deleteForm)))
      },
      form => {
        ShipmentsRepository.deleteById(form.id).map(
          _ => Redirect(routes.ShipmentController.all()).flashing("success" -> "shipment.deleted")
        )
      }
    )
  }

  /////////////////////////////////////////////////////////////////

  def index = Action.async {
    ShipmentsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newShipments = Shipment(0, 1, Date.valueOf(LocalDate.now()), "straa")
    ShipmentsRepository.insertWithReturn(newShipments).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    ShipmentsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

//  def update(id: Int) = Action.async {
//    ShipmentsRepository.update(id, Shipment(id, 1, Date.valueOf(LocalDate.now()), "straa 2")).map(r => Ok(Json.toJson(r)))
//  }

}
