package controllers

import java.sql.Date
import java.time.LocalDate

import com.mohiva.play.silhouette
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.DeleteForm.deleteForm
import models.services.AuthenticateService
import models.{OrderStatus, Payment, UserRole}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.DefaultEnv
import utils.DoubleImplicits._
import utils.FormUtils.priceConstraint
import utils.auth.HasRole

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentController @Inject()(silhouette: Silhouette[DefaultEnv],
                                  authenticateService: AuthenticateService,
                                  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteInvoicesComponent._
  import dao.SQLiteOrdersComponent._
  import dao.SQLitePaymentsComponent._

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

  def payInvoice(invoiceId: Int) = silhouette.SecuredAction.async {
    InvoicesRepository.findById(invoiceId).flatMap({
      case Some(invoice) =>
        PaymentsRepository
          .insertWithReturn(Payment(0, invoiceId, Date.valueOf(LocalDate.now()), invoice.totalCost))
          .flatMap(payment => OrdersRepository.updateStatus(invoice.orderId, OrderStatus.Pack).map(
            _ => Ok(Json.toJsObject(payment))
          )
      )
      case None => Future.successful(NotFound("Invoice not found"))
    })
  }

  private def getPaymentFromRequest(request: Request[JsValue], id: Int = 0): Payment = {
    val invoiceId = (request.body \ "invoiceId").as[Int]
    val date = (request.body \ "date").as[Date]
    val sum = (request.body \ "sum").as[Double]

    Payment(id, invoiceId, date, sum)
  }

  def create_REST =
    silhouette.SecuredAction.async(parse.json) { implicit request: Request[JsValue] =>
      PaymentsRepository
        .insertWithReturn(getPaymentFromRequest(request))
        .map(payment => Ok(Json.toJson(payment)))
    }

  def read_REST(id: Int) =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      PaymentsRepository
        .findById(id)
        .map(payment => Ok(Json.toJson(payment)))
    }

  def readAll_REST =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      PaymentsRepository.all().map(payment => Ok(Json.toJson(payment)))
    }

  def update_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      PaymentsRepository
        .update(id, getPaymentFromRequest(request, id))
        .map(_ => Accepted)
    }

  def delete_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      PaymentsRepository.deleteById(id).map(_ => Accepted)
    }

}
