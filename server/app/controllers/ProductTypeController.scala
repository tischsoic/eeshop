package controllers

import javax.inject._
import models.DeleteForm.deleteForm
import models.ProductType
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductTypeController @Inject()(cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteProductTypesComponent._

//  case class ProductType(productTypeId: Int, name: String, description: String)
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
      case Some(productType) => {
        val u = productType
        Ok(views.html.productType.update(productTypeForm.fill(productType)))
      }
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

  def index = Action.async {
    ProductTypesRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def addProductType(name: String) = Action.async {
    val newProductTypes = ProductType(0, name, "desc")
    ProductTypesRepository.insertWithReturn(newProductTypes).map(r => Ok(Json.toJson(r)))
  }

  def deleteProductType(id: Int) = Action.async {
    ProductTypesRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def updateProductType(id: Int) = Action.async {
    ProductTypesRepository.update(id, ProductType(id, "aaaa", "desc")).map(r => Ok(Json.toJson(r)))
  }

  def getProductTypesWithProducts() = Action.async {
    ProductTypesRepository
      .getProductTypesWithProducts()
      .map(res => Ok(res.map({ case (ptName, pName) => s"($ptName, $pName)"}).mkString(" ;; ")))
  }

}
