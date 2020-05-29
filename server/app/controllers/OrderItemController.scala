package controllers

import com.mohiva.play.silhouette
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.DeleteForm.deleteForm
import models.services.AuthenticateService
import models.{Order, OrderItem, Product, UserRole}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.DefaultEnv
import utils.FormUtils.priceConstraint
import utils.DoubleImplicits._
import utils.auth.HasRole

import scala.collection.Seq
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderItemController @Inject()(silhouette: Silhouette[DefaultEnv],
                                    authenticateService: AuthenticateService,
                                    cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteOrderItemsComponent._
  import dao.SQLiteOrdersComponent._
  import dao.SQLiteProductsComponent._

//  case class OrderItem(orderItemId: Int, orderId: Int, productId: Int, quantity: Int, price: Double)
  val orderItemForm: Form[OrderItem] = Form {
    mapping(
      "orderItemId" -> default(number, 0),
      "orderId" -> number,
      "productId" -> number,
      "quantity" -> number,
      "price" -> Forms.of[Double].verifying(priceConstraint)
    )(OrderItem.apply)(OrderItem.unapply)
  }

  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    // TODO: interesting https://medium.com/@sderosiaux/are-scala-futures-the-past-69bd62b9c001#1cd0
    // https://stackoverflow.com/questions/29289538/call-2-futures-in-the-same-action-async-scala-play
//    Future.sequence(List(ProductsRepository.all(), OrdersRepository.all())).map(
//      { case List(products: Seq[Product], orders: Seq[Order with Serializable]) =>
//          Ok(views.html.orderItem.create(orderItemForm, orders, products)) }
//    )
    OrdersRepository.all().flatMap(
      orders =>
        ProductsRepository.all().map(
          products => Ok(views.html.orderItem.create(orderItemForm, orders, products))
        ))
  }

  def handleCreate() = Action.async { implicit request =>
    orderItemForm.bindFromRequest.fold(
      formWithErrors => OrdersRepository.all().flatMap(
        orders =>
          ProductsRepository.all().map(
            products => BadRequest(views.html.orderItem.create(formWithErrors, orders, products))
        )),
      orderItem => {
        OrderItemsRepository.insertWithReturn(orderItem).map(
          _ => Redirect(routes.OrderItemController.create()).flashing("success" -> "orderItem.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    OrderItemsRepository.findById(id).flatMap {
      case None =>
        Future.successful(
          NotFound(Json.obj({"error" -> "OrderItems not found"}))
        )
      case Some(orderItem) => OrdersRepository.all().flatMap(
        orders =>
          ProductsRepository.all().map(
            products => Ok(views.html.orderItem.update(orderItemForm.fill(orderItem), orders, products))
          ))
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    orderItemForm.bindFromRequest.fold(
      formWithErrors => OrdersRepository.all().flatMap(
        orders =>
          ProductsRepository.all().map(
            products => BadRequest(views.html.orderItem.update(formWithErrors, orders, products))
        )),
      orderItem => {
        OrderItemsRepository.update(orderItem.orderItemId, orderItem).map(
          _ => Redirect(routes.OrderItemController.update(orderItem.orderItemId)).flashing("success" -> "orderItem.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    OrderItemsRepository.all().map(orderItems => Ok(views.html.orderItem.all(orderItems, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        OrderItemsRepository.all().map(orderItems => BadRequest(views.html.orderItem.all(orderItems, deleteForm)))
      },
      form => {
        OrderItemsRepository.deleteById(form.id).map(
          _ => Redirect(routes.OrderItemController.all()).flashing("success" -> "orderItem.deleted")
        )
      }
    )
  }

  /////////////////////////////////////////////////////////////////

  private def getOrderItemFromRequest(request: Request[JsValue], id: Int = 0): OrderItem = {
    val orderId = (request.body \ "orderId").as[Int]
    val productId = (request.body \ "productId").as[Int]
    val quantity = (request.body \ "quantity").as[Int]
    val price = (request.body \ "price").as[Double]

    OrderItem(id, orderId, productId, quantity, price)
  }

  def create_REST =
    silhouette.SecuredAction.async(parse.json) { implicit request: Request[JsValue] =>
      OrderItemsRepository
        .insertWithReturn(getOrderItemFromRequest(request))
        .map(orderItem => Ok(Json.toJson(orderItem)))
    }

  def read_REST(id: Int) =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      OrderItemsRepository
        .findById(id)
        .map(orderItem => Ok(Json.toJson(orderItem)))
    }

  def readAll_REST =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      OrderItemsRepository.all().map(products => Ok(Json.toJson(products)))
    }

  def update_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      OrderItemsRepository
        .update(id, getOrderItemFromRequest(request, id))
        .map(_ => Accepted)
    }

  def delete_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      OrderItemsRepository.deleteById(id).map(_ => Accepted)
    }

}
