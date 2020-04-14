package controllers

import javax.inject._
import models.{User, UserRole}
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {
  import dao.SQLiteUserComponent._
  import dao.SQLiteUserComponent.profile.api._

  def index = Action.async {
    UserRepository.all().map(r => Ok(Json.toJson(r)))
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
