package controllers

import java.sql.Date
import java.time.LocalDate

import javax.inject._
import models.Shipment
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ShipmentController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteShipmentsComponent._

  def index = Action.async {
    ShipmentsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newUser = Shipment(0, 1, Date.valueOf(LocalDate.now()), "straa")
    ShipmentsRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    ShipmentsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def update(id: Int) = Action.async {
    ShipmentsRepository.update(id, Shipment(id, 1, Date.valueOf(LocalDate.now()), "straa 2")).map(r => Ok(Json.toJson(r)))
  }

}
