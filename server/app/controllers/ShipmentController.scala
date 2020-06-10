package controllers

import java.sql.Date
import java.time.LocalDate

import com.mohiva.play.silhouette
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.DeleteForm.deleteForm
import models.services.AuthenticateService
import models.{OrderStatus, Shipment, UserRole}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.DefaultEnv
import utils.auth.HasRole

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ShipmentController @Inject()(silhouette: Silhouette[DefaultEnv],
                                   authenticateService: AuthenticateService,
                                   cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteShipmentsComponent._
  import dao.SQLiteOrdersComponent._

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


  private def getShipmentFromRequest(request: Request[JsValue], id: Int = 0): Shipment = {
    val orderId = (request.body \ "orderId").as[Int]
    val date = (request.body \ "date").as[Date]
    val trackingCode = (request.body \ "trackingCode").as[String]

    Shipment(id, orderId, date, trackingCode)
  }

  def createREST =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      ShipmentsRepository
        .insertWithReturn(getShipmentFromRequest(request))
        .map(shipment => Ok(Json.toJson(shipment)))
    }

  def createForOrderREST =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      val shipment = getShipmentFromRequest(request)
      ShipmentsRepository
        .insertWithReturn(shipment)
        .flatMap(_ =>
          OrdersRepository
            .updateStatus(shipment.orderId, OrderStatus.Sent)
            .flatMap(_ =>
              OrdersRepository
                .all()
                .map(orders => Ok(Json.toJson(orders)))
            )
        )
    }

  def readREST(id: Int) =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      ShipmentsRepository
        .findById(id)
        .map(shipment => Ok(Json.toJson(shipment)))
    }

  def readAllREST =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      ShipmentsRepository.all().map(shipment => Ok(Json.toJson(shipment)))
    }

  def updateREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      ShipmentsRepository
        .update(id, getShipmentFromRequest(request, id))
        .map(_ => Accepted)
    }

  def deleteREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      ShipmentsRepository.deleteById(id).map(_ => Accepted)
    }

}
