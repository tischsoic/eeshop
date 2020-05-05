package controllers

import javax.inject._
import models.OrderItem
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class OrderItemController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteOrderItemsComponent._

  def index = Action.async {
    OrderItemsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newUser = OrderItem(0, 1, 1, 1, 222)
    OrderItemsRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    OrderItemsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def update(id: Int) = Action.async {
    OrderItemsRepository.update(id, OrderItem(id, 1, 1, 1, 222)).map(r => Ok(Json.toJson(r)))
  }

}
