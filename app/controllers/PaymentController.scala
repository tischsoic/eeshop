package controllers

import java.sql.Date
import java.time.LocalDate

import javax.inject._
import models.Payment
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class PaymentController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLitePaymentsComponent._

  def index = Action.async {
    PaymentsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newUser = Payment(0, 1, Date.valueOf(LocalDate.now()), 11)
    PaymentsRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    PaymentsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def update(id: Int) = Action.async {
    PaymentsRepository.update(id, Payment(id, 1, Date.valueOf(LocalDate.now()), 14)).map(r => Ok(Json.toJson(r)))
  }

}
