package controllers

import javax.inject._
import models.Review
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class ReviewController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteReviewsComponent._

  def index = Action.async {
    ReviewsRepository.all().map(users => Ok(users.map(_.toString).mkString(" ;;; ")))
  }

  def addReview(productId: Int, authorId: Int, content: String) = Action.async {
    val newUser = Review(0, productId, authorId, content)
    ReviewsRepository.insertWithReturn(newUser).map(review => Ok(s"review with id=${review.reviewId} added!"))
  }

}
