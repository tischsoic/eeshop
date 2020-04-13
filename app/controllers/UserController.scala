package controllers

import dao.UsersDAO
import javax.inject._
import models.{User, UserRole}
import play.api.mvc._

import scala.concurrent.ExecutionContext

@Singleton
class UserController @Inject()(usersDao: UsersDAO, cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  def index = Action.async {
    usersDao.all().map(users => Ok(users.map(_.toString).mkString(" ;;; ")))
  }

  def addUser(e: String) = Action.async {
    val newUser = User(0, e + "a@a.com", "Jfstname", "B", "pass", UserRole.Staff)
    usersDao.insert(newUser).map(user => Ok(s"user with id=${user.userId} added!"))
  }

  def getUserById(id: Int) = Action.async {
    usersDao.getById(id).map {
      case None => NotFound("User not found!")
      case Some(user) => Ok(user.toString)
    }
  }

}
