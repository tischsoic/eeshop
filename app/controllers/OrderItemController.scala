package controllers

import javax.inject._
import models.DeleteForm.deleteForm
import models.{Order, OrderItem}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import utils.FormUtils.priceConstraint
import utils.DoubleImplicits._
import scala.collection.Seq

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OrderItemController @Inject()(cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
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
//    Future.sequence(List(OrdersRepository.all(), ProductsRepository.all())).map(
//      { case List(orders: Seq[Order], products: Seq[Product]) =>
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

  def index = Action.async {
    OrderItemsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newOrderItems = OrderItem(0, 1, 1, 1, 222)
    OrderItemsRepository.insertWithReturn(newOrderItems).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    OrderItemsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

//  def update(id: Int) = Action.async {
//    OrderItemsRepository.update(id, OrderItem(id, 1, 1, 1, 222)).map(r => Ok(Json.toJson(r)))
//  }

}
