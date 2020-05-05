package controllers

import java.time.LocalDate
import java.sql.Date

import javax.inject._
import models.Invoice
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class InvoiceController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteInvoicesComponent._

  def index = Action.async {
    InvoicesRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newUser = Invoice(0, 1, 100, Date.valueOf(LocalDate.now()))
    InvoicesRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    InvoicesRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def update(id: Int) = Action.async {
    InvoicesRepository.update(id, Invoice(id, 1, 111, Date.valueOf(LocalDate.now()))).map(r => Ok(Json.toJson(r)))
  }

}
