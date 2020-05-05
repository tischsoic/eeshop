package controllers

import javax.inject._
import models.Product
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ProductController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteProductsComponent._

  def index = Action.async {
    ProductsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def addProduct(productTypeId: Int, name: String) = Action.async {
    val newUser = Product(0, productTypeId, name, 11, "desc", 10)
    ProductsRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
  }

  def deleteProduct(id: Int) = Action.async {
    ProductsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def updateProduct(id: Int, newName: String) = Action.async {
    ProductsRepository.update(id, Product(id, 1, newName, 10, "ddd", 11)).map(r => Ok(Json.toJson(r)))
  }

}
