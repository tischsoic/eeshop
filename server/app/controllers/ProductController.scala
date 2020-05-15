package controllers

import javax.inject._
import models.DeleteForm.deleteForm
import models.Product
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._
import utils.FormUtils.priceConstraint
import utils.DoubleImplicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductController @Inject()(cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
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

  def getProduct(productId: Int) = Action.async {
    ProductsRepository.getProduct(productId).map {
      case Some((product, Some(productType))) => Ok(Json.toJsObject(product) + ("productType" -> Json.toJson(productType)))
      case None => NotFound("No such product")
    }
  }

  def getProducts = Action.async {
    ProductsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def addProduct(productTypeId: Int, name: String) = Action.async {
    val newProducts = Product(0, productTypeId, name, 11, "desc", 10)
    ProductsRepository.insertWithReturn(newProducts).map(r => Ok(Json.toJson(r)))
  }

  def deleteProduct(id: Int) = Action.async {
    ProductsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def updateProduct(id: Int, newName: String) = Action.async {
    ProductsRepository.update(id, Product(id, 1, newName, 10, "ddd", 11)).map(r => Ok(Json.toJson(r)))
  }

}
