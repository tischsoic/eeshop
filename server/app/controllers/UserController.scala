package controllers

import com.mohiva.play.silhouette
import com.mohiva.play.silhouette.api.Silhouette
import javax.inject._
import models.UserRole.UserRole
import models.{User, UserRole}
import models.DeleteForm.deleteForm
import models.services.AuthenticateService
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.DefaultEnv
import utils.auth.HasRole

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserController @Inject()(silhouette: Silhouette[DefaultEnv],
                               authenticateService: AuthenticateService,
                               cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
  extends MessagesAbstractController(cc) {
  import dao.SQLiteUserComponent._
  import dao.SQLiteUserComponent.profile.api._

  val userForm: Form[User] = Form {
    mapping(
      "userId" -> default(number, 0),
      "email" -> email,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "password" -> nonEmptyText,
      "role" -> Forms.of[UserRole]
    )(User.apply)(User.unapply)
  }

  def create() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.user.create(userForm))
  }

  def handleCreate() = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.user.create(formWithErrors))
        )
      },
      user => {
        UserRepository.insertWithReturn(user).map(
          _ => Redirect(routes.UserController.create()).flashing("success" -> "user.created")
        )
      }
    )
  }

  def update(id: Int) = Action.async { implicit request: MessagesRequest[AnyContent] =>
    UserRepository.findById(id).map {
      case None => NotFound(Json.obj({"error" -> "User not found"}))
      case Some(user) => Ok(views.html.user.update(userForm.fill(user)))

    }
  }

  def handleUpdate() = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.user.update(formWithErrors))
        )
      },
      user => {
        UserRepository.update(user.userId, user).map(
          _ => Redirect(routes.UserController.update(user.userId)).flashing("success" -> "user.updated")
        )
      }
    )
  }

  def all = Action.async { implicit request: MessagesRequest[AnyContent] =>
    UserRepository.all().map(users => Ok(views.html.user.all(users, deleteForm)))
  }

  def handleDelete() = Action.async { implicit request =>
    deleteForm.bindFromRequest.fold(
      _ => {
        UserRepository.all().map(users => BadRequest(views.html.user.all(users, deleteForm)))
      },
      form => {
        UserRepository.deleteById(form.id).map(
          _ => Redirect(routes.UserController.all()).flashing("success" -> "user.deleted")
        )
      }
    )
  }


  /////////////////////////////////////////////////////////////////


  private def getUserFromRequest(request: Request[JsValue], id: Int = 0): User = {
    val email = (request.body \ "email").as[String]
    val firstName = (request.body \ "firstName").as[String]
    val lastName = (request.body \ "lastName").as[String]
    val role = (request.body \ "role").as[UserRole]

    User(id, email, firstName, lastName, "", role)
  }

  def readREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      UserRepository
        .findById(id)
        .map(user => Ok(Json.toJson(user)))
    }

  def readAllREST =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      UserRepository.all().map(products => Ok(Json.toJson(products)))
    }

  def updateREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async(parse.json) { implicit request: Request[JsValue] =>
      UserRepository
        .update(id, getUserFromRequest(request, id))
        .map(_ => Accepted)
    }

  def deleteREST(id: Int) =
    silhouette.SecuredAction(HasRole(UserRole.Staff)).async { implicit request: Request[AnyContent] =>
      UserRepository.deleteById(id).map(_ => Accepted)
    }

}
