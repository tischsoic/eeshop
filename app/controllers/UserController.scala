package controllers

import javax.inject._
import models.UserRole.UserRole
import models.{User, UserRole}
import models.DeleteForm.deleteForm
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

// TODO: injecting UserRepository ???
@Singleton
class UserController @Inject()(cc: MessagesControllerComponents)(implicit ec: ExecutionContext)
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

  def index = Action.async {
    UserRepository.all().map(r => Ok(Json.toJson(r)))
  }

  def create() = Action { implicit request: MessagesRequest[AnyContent] =>
    Ok(views.html.user.create(userForm))
  }

  def handleCreate() = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.user.create(formWithErrors)) // TODO: it opens POST route, is it correct behaviour???
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
      case Some(user) => {
        val u = user
        Ok(views.html.user.update(userForm.fill(user)))
      }
    }
  }

  def handleUpdate() = Action.async { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(
          BadRequest(views.html.user.update(formWithErrors)) // TODO: it opens POST route, is it correct behaviour???
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

  def addUser(e: String) = Action.async {
    val newUser = User(0, e + "a@a.com", "Jfstname", "B", "pass", UserRole.Staff)
    UserRepository.insertWithReturn(newUser).map(r => Ok(Json.toJson(r)))
  }

  def deleteUser(id: Int) = Action.async {
    UserRepository.deleteById(id).map(r => Ok(Json.toJson(r)))
  }

  def updateUser(id: Int) = Action.async {
    UserRepository.update(id, User(id, "ss44a@a.com", "Jfstname", "B", "pass", UserRole.Staff)).map(r => Ok(Json.toJson(r)))
  }

  def getUserById(id: Int) = Action.async {
    UserRepository.findById(id).map {
      case None => NotFound("User not found!")
      case Some(user) => Ok(user.toString)
    }
  }

}
