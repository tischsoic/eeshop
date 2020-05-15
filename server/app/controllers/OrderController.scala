package controllers

import javax.inject._
import models.DeleteForm.deleteForm
import models.OrderStatus.OrderStatus
import models.{Order, OrderItem, OrderStatus, Product, ProductType}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderController @Inject()(cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteOrdersComponent._
  import dao.SQLiteOrderItemsComponent._
  import dao.SQLiteProductsComponent._
  import dao.SQLiteUserComponent._

//  case class Order(orderId: Int, customerId: Int, status: OrderStatus)
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

  def addItem(productId: Int, quantity: Int, userId: Int) = Action.async {
    OrdersRepository.getOrCreateCheckout(userId).flatMap({
      case Some(order) => ProductsRepository.findById(productId).flatMap({
        case Some(product) => OrderItemsRepository
          .insert(OrderItem(0, order.orderId, productId, quantity, product.price)).map(_ => Ok("Item added"))
        case None => Future.successful(NotFound("Product not found"))
      })
      case None => Future.successful(NotFound("Order not found"))
    })
  }

  def getCheckout(userId: Int) = Action.async {
    OrdersRepository.getOrCreateCheckout(userId).flatMap({
      case Some(order) => OrderItemsRepository.getOrderItemsWithProduct(order.orderId).map(
        (orderItemsWithProduct: Seq[(OrderItem, Product, ProductType)]) =>
          Ok(Json.toJsObject(order) + ("items" -> orderItemsWithProductToJson(orderItemsWithProduct)))
      )
      case None => Future.successful(NotFound("Order not found"))
    })
  }

  def orderItemsWithProductToJson(orderItemsWithProduct: Seq[(OrderItem, Product, ProductType)]) =
    Json.toJson(orderItemsWithProduct.map({
      case (orderItem: OrderItem, product: Product, productType: ProductType) =>
        Json.toJsObject(orderItem) +
          ("product" -> (Json.toJsObject(product) +
            ("productType" -> Json.toJsObject(productType))))
    }))

  def index = Action.async {
    OrdersRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newOrders = Order(0, 1, OrderStatus.AwaitingPayment)
    OrdersRepository.insertWithReturn(newOrders).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    OrdersRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

//  def update(id: Int) = Action.async {
//    OrdersRepository.update(id, Order(id, 1, OrderStatus.Sent)).map(r => Ok(Json.toJson(r)))
//  }

}
