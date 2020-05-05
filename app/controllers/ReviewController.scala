package controllers

import javax.inject._
import models.DeleteForm.deleteForm
import models.Review
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewController @Inject()(cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteReviewsComponent._
  import dao.SQLiteProductsComponent._
  import dao.SQLiteUserComponent._

//  case class Review(reviewId: Int, productId: Int, authorId: Int, content: String)
  val reviewForm: Form[Review] = Form {
    mapping(
      "reviewId" -> default(number, 0),
      "productId" -> number,
      "authorId" -> number,
      "content" -> nonEmptyText
    )(Review.apply)(Review.unapply)
  }

  def create() = Action.async { implicit request: MessagesRequest[AnyContent] =>
//    for (
//      products <- ProductsRepository.all();
//      users <- UserRepository.all()
//    ) yield Ok(views.html.review.create(reviewForm, products, users))
    ProductsRepository.all().flatMap(
      products =>
        UserRepository.all().map(
          users => Ok(views.html.review.create(reviewForm, products, users))
        ))
  }

  def handleCreate() = Action.async { implicit request =>
    reviewForm.bindFromRequest.fold(
      formWithErrors => ProductsRepository.all().flatMap(
        products =>
          UserRepository.all().map(
            users => BadRequest(views.html.review.create(formWithErrors, products, users))
        )),
      review => {
        ReviewsRepository.insertWithReturn(review).map(
          _ => Redirect(routes.ReviewController.create()).flashing("success" -> "review.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    ReviewsRepository.findById(id).flatMap {
      case None => Future.successful(
        NotFound(Json.obj({"error" -> "Reviews not found"}))
      )
      case Some(review) => ProductsRepository.all().flatMap(
        products =>
          UserRepository.all().map(
            users => Ok(views.html.review.update(reviewForm.fill(review), products, users))
          ))
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    reviewForm.bindFromRequest.fold(
      formWithErrors => ProductsRepository.all().flatMap(
        products =>
          UserRepository.all().map(
            users => BadRequest(views.html.review.update(formWithErrors, products, users))
        )),
      review => {
        ReviewsRepository.update(review.reviewId, review).map(
          _ => Redirect(routes.ReviewController.update(review.reviewId)).flashing("success" -> "review.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    ReviewsRepository.all().map(reviews => Ok(views.html.review.all(reviews, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        ReviewsRepository.all().map(reviews => BadRequest(views.html.review.all(reviews, deleteForm)))
      },
      form => {
        ReviewsRepository.deleteById(form.id).map(
          _ => Redirect(routes.ReviewController.all()).flashing("success" -> "review.deleted")
        )
      }
    )
  }

  /////////////////////////////////////////////////////////////////

  def index = Action.async {
    ReviewsRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def add() = Action.async {
    val newReviews = Review(0, 1, 1, "ccontent")
    ReviewsRepository.insertWithReturn(newReviews).map(r => Ok(Json.toJson(r)))
  }

  def delete(id: Int) = Action.async {
    ReviewsRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

//  def update(id: Int) = Action.async {
//    ReviewsRepository.update(id, Review(id, 1, 1, "ccontent2")).map(r => Ok(Json.toJson(r)))
//  }

}
