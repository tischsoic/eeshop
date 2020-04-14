package controllers

import javax.inject._
import models.ProductType
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ProductTypeController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteProductTypesComponent._

  def index = Action.async {
    ProductTypesRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def addProductType(name: String) = Action.async {
    val newUser = ProductType(0, name, "desc")
    ProductTypesRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
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
