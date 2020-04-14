package controllers

import javax.inject._
import models.{Order, OrderStatus}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class OrderController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteOrdersComponent._

  def index = Action.async {
    OrdersRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newUser = Order(0, 1, OrderStatus.AwaitingPayment)
    OrdersRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    OrdersRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def update(id: Int) = Action.async {
    OrdersRepository.update(id, Order(id, 1, OrderStatus.Sent)).map(r => Ok(Json.toJson(r)))
  }

}
