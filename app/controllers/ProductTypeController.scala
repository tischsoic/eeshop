package controllers

import javax.inject._
import models.{Product, ProductType, Review}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ProductTypeController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteProductTypesComponent._

  def index = Action.async {
    ProductTypesRepository.all().map(users => Ok(users.map(_.toString).mkString(" ;;; ")))
  }

  def addProductType(name: String) = Action.async {
    val newUser = ProductType(0, name, "desc")
    ProductTypesRepository.insertWithReturn(newUser).map(review => Ok(s"review with id=${review.productTypeId} added!"))
  }

  def getProductTypesWithProducts() = Action.async {
    ProductTypesRepository
      .getProductTypesWithProducts()
      .map(res => Ok(res.map({ case (ptName, pName) => s"($ptName, $pName)"}).mkString(" ;; ")))
  }

}
