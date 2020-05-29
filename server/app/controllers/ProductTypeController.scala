package controllers

import com.mohiva.play.silhouette
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.DeleteForm.deleteForm
import models.services.AuthenticateService
import models.{ProductType, UserRole}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.DefaultEnv
import utils.auth.HasRole

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductTypeController @Inject()(silhouette: Silhouette[DefaultEnv],
                                      authenticateService: AuthenticateService,
                                      cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  import dao.SQLiteProductTypesComponent._

  val productTypeForm: Form[ProductType] = Form {
    mapping(
      "productTypeId" -> default(number, 0),
      "name" -> nonEmptyText,
      "description" -> nonEmptyText
    )(ProductType.apply)(ProductType.unapply)
  }

  def create() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.productType.create(productTypeForm))
  }

  def handleCreate() = Action.async { implicit request =>
    productTypeForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.productType.create(formWithErrors))
        )
      },
      productType => {
        ProductTypesRepository.insertWithReturn(productType).map(
          _ => Redirect(routes.ProductTypeController.create()).flashing("success" -> "productType.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    ProductTypesRepository.findById(id).map {
      case None => NotFound(Json.obj({"error" -> "ProductTypes not found"}))
      case Some(productType) => Ok(views.html.productType.update(productTypeForm.fill(productType)))
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    productTypeForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.productType.update(formWithErrors))
        )
      },
      productType => {
        ProductTypesRepository.update(productType.productTypeId, productType).map(
          _ => Redirect(routes.ProductTypeController.update(productType.productTypeId)).flashing("success" -> "productType.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    ProductTypesRepository.all().map(productTypes => Ok(views.html.productType.all(productTypes, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        ProductTypesRepository.all().map(productTypes => BadRequest(views.html.productType.all(productTypes, deleteForm)))
      },
      form => {
        ProductTypesRepository.deleteById(form.id).map(
          _ => Redirect(routes.ProductTypeController.all()).flashing("success" -> "productType.deleted")
        )
      }
    )
  }

  /////////////////////////////////////////////////////////////////

  private def getProductTypeFromRequest(request: Request[JsValue], id: Int = 0): ProductType = {
    val name = (request.body \ "name").as[String]
    val description = (request.body \ "description").as[String]

    ProductType(id, name, description)
  }

  def createREST =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      ProductTypesRepository
        .insertWithReturn(getProductTypeFromRequest(request))
        .map(faqNote => Ok(Json.toJson(faqNote)))
    }

  def readREST(id: Int) =
    Action.async { implicit request: Request[AnyContent] =>
      ProductTypesRepository.findById(id).map(faqNote => Ok(Json.toJson(faqNote)))
    }

  def readAllREST =
    Action.async { implicit request: Request[AnyContent] =>
      ProductTypesRepository.all().map(productTypes => Ok(Json.toJson(productTypes)))
    }

  def updateREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      ProductTypesRepository
        .update(id, getProductTypeFromRequest(request, id))
        .map(_ => Accepted)
    }

  def deleteREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      ProductTypesRepository.deleteById(id).map(_ => Accepted)
    }

}
