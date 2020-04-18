package controllers

import java.sql.Date
import java.time.LocalDate

import javax.inject._
import models.DeleteForm.deleteForm
import models.Payment
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import utils.FormUtils.priceConstraint
import utils.DoubleImplicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentController @Inject()(cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLitePaymentsComponent._
  import dao.SQLiteInvoicesComponent._

//  case class Payment(paymentId: Int, invoiceId: Int, date: Date, sum: Double)
  val paymentForm: Form[Payment] = Form {
    mapping(
      "paymentId" -> default(number, 0),
      "invoiceId" -> number,
      "date" -> sqlDate,
      "sum" -> Forms.of[Double].verifying(priceConstraint)
    )(Payment.apply)(Payment.unapply)
  }

  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    InvoicesRepository.all().map(
      invoices => Ok(views.html.payment.create(paymentForm, invoices))
    )
  }

  def handleCreate() = Action.async { implicit request =>
    paymentForm.bindFromRequest.fold(
      formWithErrors => InvoicesRepository.all().map(
        invoices => BadRequest(views.html.payment.create(formWithErrors, invoices))
        ),
      payment => {
        PaymentsRepository.insertWithReturn(payment).map(
          _ => Redirect(routes.PaymentController.create()).flashing("success" -> "payment.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    PaymentsRepository.findById(id).flatMap {
      case None => Future.successful(
        NotFound(Json.obj({"error" -> "Payments not found"}))
      )
      case Some(payment) => InvoicesRepository.all().map(
        invoices => Ok(views.html.payment.update(paymentForm.fill(payment), invoices))
      )
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    paymentForm.bindFromRequest.fold(
      formWithErrors => InvoicesRepository.all().map(
        invoices => BadRequest(views.html.payment.update(formWithErrors, invoices))
        ),
      payment => {
        PaymentsRepository.update(payment.paymentId, payment).map(
          _ => Redirect(routes.PaymentController.update(payment.paymentId)).flashing("success" -> "payment.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    PaymentsRepository.all().map(payments => Ok(views.html.payment.all(payments, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        PaymentsRepository.all().map(payments => BadRequest(views.html.payment.all(payments, deleteForm)))
      },
      form => {
        PaymentsRepository.deleteById(form.id).map(
          _ => Redirect(routes.PaymentController.all()).flashing("success" -> "payment.deleted")
        )
      }
    )
  }

  /////////////////////////////////////////////////////////////////

  def index = Action.async {
    PaymentsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newPayments = Payment(0, 1, Date.valueOf(LocalDate.now()), 11)
    PaymentsRepository.insertWithReturn(newPayments).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    PaymentsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

//  def update(id: Int) = Action.async {
//    PaymentsRepository.update(id, Payment(id, 1, Date.valueOf(LocalDate.now()), 14)).map(r => Ok(Json.toJson(r)))
//  }

}
