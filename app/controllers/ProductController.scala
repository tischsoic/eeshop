package controllers

import javax.inject._
import models.{Product, Review}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ProductController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteProductsComponent._

  def index = Action.async {
    ProductsRepository.all().map(users => Ok(users.map(_.toString).mkString(" ;;; ")))
  }

  def addProduct(productTypeId: Int, name: String) = Action.async {
    val newUser = Product(0, productTypeId, name, 11, "desc", 10)
    ProductsRepository.insertWithReturn(newUser).map(review => Ok(s"review with id=${review.productId} added!"))
  }

}
