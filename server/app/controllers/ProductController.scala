package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.DeleteForm.deleteForm
import models.{Product, UserRole}
import models.services.AuthenticateService
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.DefaultEnv
import utils.FormUtils.priceConstraint
import utils.DoubleImplicits._
import utils.auth.HasRole

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductController @Inject()(silhouette: Silhouette[DefaultEnv],
                                  authenticateService: AuthenticateService,
                                  cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {

  import dao.SQLiteProductsComponent._
  import dao.SQLiteProductTypesComponent._

//  case class Product(productId: Int, productTypeId: Int, name: String, price: Double, description: String, quantity: Int)
  val productForm: Form[Product] = Form {
    mapping(
      "productId" -> default(number, 0),
      "productTypeId" -> number,
      "name" -> nonEmptyText,
      "price" -> Forms.of[Double].verifying(priceConstraint),
      "description" -> nonEmptyText,
      "quantity" -> number
    )(Product.apply)(Product.unapply)
  }

  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
    ProductTypesRepository.all().map(
      productTypes => Ok(views.html.product.create(productForm, productTypes))
    )
  }

  def handleCreate() = Action.async { implicit request =>
    productForm.bindFromRequest.fold(
      formWithErrors => ProductTypesRepository.all().map(
        productTypes => BadRequest(views.html.product.create(formWithErrors, productTypes))
      ),
      product => {
        ProductsRepository.insertWithReturn(product).map(
          _ => Redirect(routes.ProductController.create()).flashing("success" -> "product.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    ProductsRepository.findById(id).flatMap {
      case None => Future.successful(
        NotFound(Json.obj({"error" -> "Products not found"}))
      )
      case Some(product) => ProductTypesRepository.all().map(
        productTypes => Ok(views.html.product.update(productForm.fill(product), productTypes))
      )
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    productForm.bindFromRequest.fold(
      formWithErrors => ProductTypesRepository.all().map(
        productTypes => BadRequest(views.html.product.update(formWithErrors, productTypes))
      ),
      product => {
        ProductsRepository.update(product.productId, product).map(
          _ => Redirect(routes.ProductController.update(product.productId)).flashing("success" -> "product.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    ProductsRepository.all().map(products => Ok(views.html.product.all(products, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        ProductsRepository.all().map(products => BadRequest(views.html.product.all(products, deleteForm)))
      },
      form => {
        ProductsRepository.deleteById(form.id).map(
          _ => Redirect(routes.ProductController.all()).flashing("success" -> "product.deleted")
        )
      }
    )
  }

  /////////////////////////////////////////////////////////////////

  private def getProductFromRequest(request: Request[JsValue], id: Int = 0): Product = {
    val productTypeId = (request.body \ "productTypeId").as[Int]
    val name = (request.body \ "name").as[String]
    val price = (request.body \ "price").as[Double]
    val description = (request.body \ "description").as[String]
    val quantity = (request.body \ "quantity").as[Int]

    Product(id, productTypeId, name, price, description, quantity)
  }

  def create_REST =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      ProductsRepository
        .insertWithReturn(getProductFromRequest(request))
        .map(product => Ok(Json.toJson(product)))
    }

  def read_REST(id: Int) =
    Action.async { implicit request: Request[AnyContent] =>
      ProductsRepository.getProduct(id).map {
        case Some((product, Some(productType))) => Ok(Json.toJsObject(product) + ("productType" -> Json.toJson(productType)))
        case None => NotFound("No such product")
      }
    }

  def readAll_REST =
    Action.async { implicit request: Request[AnyContent] =>
      ProductsRepository.all().map(products => Ok(Json.toJson(products)))
    }

  def update_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      ProductsRepository
        .update(id, getProductFromRequest(request, id))
        .map(_ => Accepted)
    }

  def delete_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      ProductsRepository.deleteById(id).map(_ => Accepted)
    }

}
