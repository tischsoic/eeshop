package controllers

import com.mohiva.play.silhouette
import com.mohiva.play.silhouette.api.{HandlerResult, Silhouette}
import javax.inject._
import models.DeleteForm.deleteForm
import models.OrderStatus.OrderStatus
import models.services.AuthenticateService
import models.{Order, OrderItem, OrderStatus, Product, ProductType, UserRole}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.DefaultEnv
import utils.auth.HasRole

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(silhouette: Silhouette[DefaultEnv],
                                authenticateService: AuthenticateService,
                                cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteOrdersComponent._
  import dao.SQLiteOrderItemsComponent._
  import dao.SQLiteProductsComponent._
  import dao.SQLiteUserComponent._

  val orderForm: Form[Order] = Form {
    mapping(
      "orderId" -> default(number, 0),
      "customerId" -> number,
      "status" -> Forms.of[OrderStatus]
    )(Order.apply)(Order.unapply)
  }

  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    UserRepository.all().map(
      users => Ok(views.html.order.create(orderForm, users))
    )
  }

  def handleCreate() = Action.async { implicit request =>
    orderForm.bindFromRequest.fold(
      formWithErrors => {
        UserRepository.all().map(
          users => BadRequest(views.html.order.create(formWithErrors, users))
        )
      },
      order => {
        OrdersRepository.insertWithReturn(order).map(
          _ => Redirect(routes.OrderController.create()).flashing("success" -> "order.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    OrdersRepository.findById(id).flatMap {
      case None => Future.successful(
          NotFound(Json.obj({"error" -> "Orders not found"}))
        )
      case Some(order) =>
        UserRepository.all().map(
          users => Ok(views.html.order.update(orderForm.fill(order), users))
        )
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    orderForm.bindFromRequest.fold(
      formWithErrors =>
        UserRepository.all().map(
          users => BadRequest(views.html.order.update(formWithErrors, users))
        ),
      order => {
        OrdersRepository.update(order.orderId, order).map(
          _ => Redirect(routes.OrderController.update(order.orderId)).flashing("success" -> "order.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    OrdersRepository.all().map(orders => Ok(views.html.order.all(orders, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        OrdersRepository.all().map(orders => BadRequest(views.html.order.all(orders, deleteForm)))
      },
      form => {
        OrdersRepository.deleteById(form.id).map(
          _ => Redirect(routes.OrderController.all()).flashing("success" -> "order.deleted")
        )
      }
    )
  }

  /////////////////////////////////////////////////////////////////

  def addItemREST() =
    silhouette.SecuredAction.async(parse.json) { implicit request: Request[JsValue] => {
      val productId = (request.body \ "productId").as[Int]
      val userId = (request.body \ "userId").as[Int]
      val quantity = (request.body \ "quantity").as[Int]

      OrdersRepository.getOrCreateCheckout(userId).flatMap({
        case Some(order) => ProductsRepository.findById(productId).flatMap({
          case Some(product) => OrderItemsRepository
            .insert(OrderItem(0, order.orderId, productId, quantity, product.price)).map(_ => Ok("Item added"))
          case None => Future.successful(NotFound("Product not found"))
        })
        case None => Future.successful(NotFound("Order not found"))
      })
    }
  }

  def getCheckoutREST(userId: Int) = silhouette.SecuredAction.async {
    OrdersRepository.getOrCreateCheckout(userId).flatMap({
      case Some(order) => OrderItemsRepository.getOrderItemsWithProduct(order.orderId).map(
        (orderItemsWithProduct: Seq[(OrderItem, Product, ProductType)]) =>
          Ok(Json.toJsObject(order) + ("items" -> orderItemsWithProductToJson(orderItemsWithProduct)))
      )
      case None => Future.successful(NotFound("Order not found"))
    })
  }

  def getUserOrdersREST = Action.async { implicit request =>
    silhouette.UserAwareRequestHandler { userAwareRequest =>
      Future.successful(HandlerResult(Ok, userAwareRequest.identity))
    }(request).flatMap {
      case HandlerResult(r, Some(user)) => {
        val userOrders = OrdersRepository.getUserOrders(user.userId)
        userOrders.map(userOrders => Ok(Json.toJson(userOrders)))
      }
      case HandlerResult(r, None) => Future.successful(Unauthorized)
    }
  }

  def orderItemsWithProductToJson(orderItemsWithProduct: Seq[(OrderItem, Product, ProductType)]) =
    Json.toJson(orderItemsWithProduct.map({
      case (orderItem: OrderItem, product: Product, productType: ProductType) =>
        Json.toJsObject(orderItem) +
          ("product" -> (Json.toJsObject(product) +
            ("productType" -> Json.toJsObject(productType))))
    }))

  private def getOrderFromRequest(request: Request[JsValue], id: Int = 0): Order = {
    val customerId = (request.body \ "customerId").as[Int]
    val status = (request.body \ "status").as[OrderStatus]

    Order(id, customerId, status)
  }

  def createREST =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      OrdersRepository
        .insertWithReturn(getOrderFromRequest(request))
        .map(product => Ok(Json.toJson(product)))
    }

  def readREST(id: Int) =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      OrdersRepository
        .findById(id)
        .map(order => Ok(Json.toJson(order)))
    }

  def readAllREST =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      OrdersRepository.all().map(orders => Ok(Json.toJson(orders)))
    }

  def updateREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      OrdersRepository
        .update(id, getOrderFromRequest(request, id))
        .map(_ => Accepted)
    }

  def updateDeliveredREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      OrdersRepository
        .updateStatus(id, OrderStatus.Delivered)
        .flatMap(_ =>
          OrdersRepository
            .all()
            .map(orders => Ok(Json.toJson(orders)))
        )
    }

  def deleteREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      OrdersRepository.deleteById(id).map(_ => Accepted)
    }

}
