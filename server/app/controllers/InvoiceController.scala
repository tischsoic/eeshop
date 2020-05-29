package controllers

import java.sql.Date
import java.time.LocalDate

import com.mohiva.play.silhouette
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.DeleteForm.deleteForm
import models.services.AuthenticateService
import models.{Invoice, OrderItem, ProductType, UserRole}
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
class InvoiceController @Inject()(silhouette: Silhouette[DefaultEnv],
                                  authenticateService: AuthenticateService,
                                  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteInvoicesComponent._
  import dao.SQLiteOrdersComponent._
  import dao.SQLiteOrderItemsComponent._

  val invoiceForm: Form[Invoice] = Form {
    mapping(
      "invoiceId" -> default(number, 0),
      "orderId" -> number,
      "totalCost" -> Forms.of[Double].verifying(priceConstraint),
      "date" -> sqlDate
    )(Invoice.apply)(Invoice.unapply)
  }

  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    OrdersRepository.all().map(
      orders => Ok(views.html.invoice.create(invoiceForm, orders))
    )
  }

  def handleCreate() = Action.async { implicit request =>
    invoiceForm.bindFromRequest.fold(
      formWithErrors =>
        OrdersRepository.all().map(
          orders => BadRequest(views.html.invoice.create(formWithErrors, orders))
        ),
      invoice => {
        InvoicesRepository.insertWithReturn(invoice).map(
          _ => Redirect(routes.InvoiceController.create()).flashing("success" -> "invoice.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    InvoicesRepository.findById(id).flatMap {
      case None => Future.successful(
        NotFound(Json.obj({"error" -> "Invoices not found"}))
      )
      case Some(invoice) =>
        OrdersRepository.all().map(
          orders => Ok(views.html.invoice.update(invoiceForm.fill(invoice), orders))
        )
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    invoiceForm.bindFromRequest.fold(
      formWithErrors => {
        OrdersRepository.all().map(
          orders => BadRequest(views.html.invoice.update(formWithErrors, orders))
        )
      },
      invoice => {
        InvoicesRepository.update(invoice.invoiceId, invoice).map(
          _ => Redirect(routes.InvoiceController.update(invoice.invoiceId)).flashing("success" -> "invoice.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    InvoicesRepository.all().map(invoices => Ok(views.html.invoice.all(invoices, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        InvoicesRepository.all().map(invoices => BadRequest(views.html.invoice.all(invoices, deleteForm)))
      },
      form => {
        InvoicesRepository.deleteById(form.id).map(
          _ => Redirect(routes.InvoiceController.all()).flashing("success" -> "invoice.deleted")
        )
      }
    )
  }

  /////////////////////////////////////////////////////////////////

  def createForOrder(orderId: Int) = Action.async {
    OrdersRepository.findById(orderId).flatMap({
      case Some(order) => OrderItemsRepository.getOrderItemsWithProduct(order.orderId).flatMap(
        (orderItemsWithProduct: Seq[(OrderItem, Product, ProductType)]) => {
          val totalPrice = orderItemsWithProduct.foldLeft(0.0)(
            { case (totalPrice, (orderItem, _, _)) => totalPrice + orderItem.quantity * orderItem.price})

          InvoicesRepository.insertWithReturn(Invoice(0, orderId, totalPrice, Date.valueOf(LocalDate.now()))).map(
            invoice => Ok(Json.toJsObject(invoice))
          )
        }
      )
      case None => Future.successful(NotFound("Order not found"))
    })
  }

  private def getInvoiceFromRequest(request: Request[JsValue], id: Int = 0): Invoice = {
    val orderId = (request.body \ "orderId").as[Int]
    val totalCost = (request.body \ "totalCost").as[Double]
    val date = (request.body \ "date").as[Date]

    Invoice(id, orderId, totalCost, date)
  }

  def createREST =
    silhouette.SecuredAction.async(parse.json) { implicit request: Request[JsValue] =>
      InvoicesRepository
        .insertWithReturn(getInvoiceFromRequest(request))
        .map(invoice => Ok(Json.toJson(invoice)))
    }

  def readREST(id: Int) =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      InvoicesRepository
        .findById(id)
        .map(invoice => Ok(Json.toJson(invoice)))
    }

  def readAllREST =
    silhouette.SecuredAction.async { implicit request: Request[AnyContent] =>
      InvoicesRepository.all().map(products => Ok(Json.toJson(products)))
    }

  def updateREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      InvoicesRepository
        .update(id, getInvoiceFromRequest(request, id))
        .map(_ => Accepted)
    }

  def deleteREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      InvoicesRepository.deleteById(id).map(_ => Accepted)
    }

}
