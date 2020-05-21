package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.DeleteForm.deleteForm
import models.services.AuthenticateService
import models.{Review, UserRole}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.DefaultEnv
import utils.auth.HasRole

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReviewController @Inject()(silhouette: Silhouette[DefaultEnv],
                                 authenticateService: AuthenticateService,
                                 cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
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


  private def getReviewFromRequest(request: Request[JsValue], id: Int = 0): Review = {
    val productId = (request.body \ "productId").as[Int]
    val authorId = (request.body \ "authorId").as[Int]
    val content = (request.body \ "content").as[String]

    Review(id, productId, authorId, content)
  }

  def create_REST =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      ReviewsRepository
        .insertWithReturn(getReviewFromRequest(request))
        .map(product => Ok(Json.toJson(product)))
    }

  def read_REST(id: Int) =
    Action.async { implicit request: Request[AnyContent] =>
      ReviewsRepository.getReview(id).map {
        case Some((review, Some(user))) => Ok(Json.toJsObject(review) + ("user" -> Json.toJson(user)))
        case None => NotFound("No such product")
      }
    }

  def readAll_REST =
    Action.async { implicit request: Request[AnyContent] =>
      ReviewsRepository
        .getAllReviews()
        .map(reviews => Ok(Json.toJson(
          reviews.map { case (review, Some(user)) => Json.toJsObject(review) + ("user" -> Json.toJson(user)) }
        )))
    }

  def update_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      ReviewsRepository
        .update(id, getReviewFromRequest(request, id))
        .map(_ => Accepted)
    }

  def delete_REST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      ReviewsRepository.deleteById(id).map(_ => Accepted)
    }

}
