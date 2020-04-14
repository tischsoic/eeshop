package controllers

import javax.inject._
import models.Review
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ReviewController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteReviewsComponent._

  def index = Action.async {
    ReviewsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newUser = Review(0, 1, 1, "ccontent")
    ReviewsRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    ReviewsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def update(id: Int) = Action.async {
    ReviewsRepository.update(id, Review(id, 1, 1, "ccontent2")).map(r => Ok(Json.toJson(r)))
  }

}
